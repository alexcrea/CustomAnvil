package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.unitRepair
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_RIGHT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import xyz.alexcrea.cuanvil.util.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import kotlin.math.min

class AnvilResultListener: Listener {

    companion object {
        // static slot container
        private val NO_SLOT = SlotContainer(SlotType.NO_SLOT, 0)
        private val CURSOR_SLOT = SlotContainer(SlotType.CURSOR, 0)
    }

    /**
     * Event handler logic for when a player is trying to pull an item out of the anvil
     */
    @EventHandler(ignoreCancelled = true)
    fun anvilExtractionCheck(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return
        val inventory = event.inventory as? AnvilInventory ?: return

        if (event.rawSlot != ANVIL_OUTPUT_SLOT) {
            return
        }

        // Test if the event should bypass custom anvil.
        if(DependencyManager.tryClickAnvilResultBypass(event, inventory)) return

        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        val leftItem = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val rightItem = inventory.getItem(ANVIL_INPUT_RIGHT)

        if(GameMode.CREATIVE != player.gameMode && inventory.repairCost >= inventory.maximumRepairCost) {
            event.result = Event.Result.DENY
            return
        }

        // Test custom recipe
        val recipe = CustomRecipeUtil.getCustomRecipe(leftItem, rightItem)
        if(recipe != null){
            event.result = Event.Result.ALLOW
            onCustomCraft(
                event, recipe, player,
                leftItem, rightItem, output, inventory)
            return
        }

        val canMerge = leftItem.canMergeWith(rightItem)
        val unitRepairResult = leftItem.getRepair(rightItem)
        val allowed = (rightItem == null)
                || (canMerge)
                || (unitRepairResult != null)

        // True if there was no change or not allowed
        if ((output == inventory.getItem(ANVIL_INPUT_LEFT))
            || !allowed
        ) {
            event.result = Event.Result.DENY
            return
        }
        if (rightItem == null) {
            event.result = Event.Result.ALLOW
            return
        }
        if (canMerge) {
            event.result = Event.Result.ALLOW
        } else if (unitRepairResult != null) {
            onUnitRepairExtract(
                leftItem, rightItem, output,
                unitRepairResult, event, player, inventory
            )

            return
        }
    }

    private fun onCustomCraft(event: InventoryClickEvent,
                              recipe: AnvilCustomRecipe,
                              player: Player,
                              leftItem: ItemStack,
                              rightItem: ItemStack?,
                              output: ItemStack,
                              inventory: AnvilInventory
    ) {
        event.result = Event.Result.DENY

        if(recipe.leftItem == null) return // in case it changed

        val amount = CustomRecipeUtil.getCustomRecipeAmount(recipe, leftItem, rightItem)
        val xpCost = amount * recipe.xpCostPerCraft

        CustomAnvil.log("gamemode: ${player.gameMode != GameMode.CREATIVE}, cost: $xpCost, level: ${player.level}, result: ${player.level < xpCost}")
        if ((player.gameMode != GameMode.CREATIVE) && (player.level < xpCost)) return

        // We give the item manually
        // But first we check if we should give the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return

        // Handle not creative middle click...
        if (event.click != ClickType.MIDDLE &&
            !handleCustomCraftClick(event, recipe, inventory, player, leftItem, rightItem, amount, xpCost)) return

        // Finally, we add the item to the player
        if (slotDestination.type == SlotType.CURSOR) {
            player.setItemOnCursor(output)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, output)
        }
    }

    private fun handleCustomCraftClick(event: InventoryClickEvent, recipe: AnvilCustomRecipe,
                                       inventory: AnvilInventory, player: Player,
                                       leftItem: ItemStack, rightItem: ItemStack?,
                                       amount: Int, xpCost: Int): Boolean {
        // We remove what should be removed
        if(rightItem != null){
            if(recipe.rightItem == null) return false// in case it changed

            rightItem.amount -= amount * recipe.rightItem!!.amount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)
        }

        leftItem.amount -= amount * recipe.leftItem!!.amount
        inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

        if(player.gameMode != GameMode.CREATIVE){
            player.level -= xpCost
        }

        // Then we try to find the new values for the anvil
        val newAmount = CustomRecipeUtil.getCustomRecipeAmount(recipe, leftItem, rightItem)

        CustomAnvil.verboseLog("new amount is $newAmount")
        if(newAmount <= 0 || recipe.exactCount){
            inventory.setItem(ANVIL_OUTPUT_SLOT, null)
        }else{
            val resultItem: ItemStack = recipe.resultItem!!.clone()
            resultItem.amount *= newAmount

            val newXp = newAmount * newAmount

            inventory.repairCost = newXp
            event.view.setProperty(InventoryView.Property.REPAIR_COST, newXp)

            inventory.setItem(ANVIL_OUTPUT_SLOT, resultItem)

            player.updateInventory()
        }
        return true
    }

    private fun onUnitRepairExtract(
        leftItem: ItemStack,
        rightItem: ItemStack,
        output: ItemStack,
        unitRepairResult: Double,
        event: InventoryClickEvent,
        player: Player,
        inventory: AnvilInventory
    ) {
        val resultCopy = leftItem.clone()
        val resultAmount = resultCopy.unitRepair(
            rightItem.amount, unitRepairResult
        )

        // To avoid vanilla, we cancel the event for unit repair
        event.result = Event.Result.DENY
        event.isCancelled = true
        // And we give the item manually
        // But first we check if we should give the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return

        // Test repair cost
        val repairCost = getUnitRepairCost(inventory, player, leftItem, output, resultCopy, resultAmount)
        if(repairCost == Int.MIN_VALUE) return

        // If not creative middle click...
        if (event.click != ClickType.MIDDLE) {
            // We remove what should be removed
            inventory.setItem(ANVIL_INPUT_LEFT, null)
            rightItem.amount -= resultAmount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)
            inventory.setItem(ANVIL_OUTPUT_SLOT, null)
            player.level -= repairCost
        }

        // Finally, we add the item to the player
        if (slotDestination.type == SlotType.CURSOR) {
            player.setItemOnCursor(output)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, output)
        }
    }

    private fun getUnitRepairCost(inventory: AnvilInventory, player: Player,
                              leftItem: ItemStack, output: ItemStack,
                              resultCopy: ItemStack, resultAmount: Int): Int {
        if (player.gameMode == GameMode.CREATIVE) return 0

        var repairCost = 0
        // Get repairCost
        leftItem.itemMeta?.let { leftMeta ->
            val leftName = leftMeta.displayName
            output.itemMeta?.let {
                // Rename cost
                if (!leftName.contentEquals(it.displayName)) {
                    repairCost += ConfigOptions.itemRenameCost

                    // Color cost
                    if(it.displayName.contains('§')){
                        repairCost += ConfigOptions.useOfColorCost
                    }
                }
            }
        }

        repairCost += AnvilXpUtil.calculatePenalty(leftItem, null, resultCopy)
        repairCost += resultAmount * ConfigOptions.unitRepairCost

        if (
            !ConfigOptions.doRemoveCostLimit &&
            ConfigOptions.doCapCost
        ) {
            repairCost = min(repairCost, ConfigOptions.maxAnvilCost)
        }

        if ((inventory.maximumRepairCost <= repairCost)
            || (player.level < repairCost)
        ) return Int.MIN_VALUE

        return repairCost
    }

    /**
     * Get the destination slot or "NO_SLOT" slot container if there is no slot available
     */
    private fun getActionSlot(event: InventoryClickEvent, player: Player): SlotContainer {
        if (event.isShiftClick) {
            val inventory = player.inventory
            val firstEmpty = inventory.firstEmpty()
            if (firstEmpty == -1) {
                return NO_SLOT
            }
            //check hotbare full
            var slotIndex = 8
            while (slotIndex >= 0 && ((inventory.getItem(slotIndex)?.type ?: Material.AIR) != Material.AIR)) {
                slotIndex--
            }
            if (slotIndex >= 0) {
                return SlotContainer(SlotType.INVENTORY, slotIndex)
            }
            slotIndex = 35 //4*9 - 1 (max of player inventory)
            while (slotIndex >= 9 && ((inventory.getItem(slotIndex)?.type ?: Material.AIR) != Material.AIR)) {
                slotIndex--
            }
            if (slotIndex < 9) {
                return NO_SLOT
            }
            return SlotContainer(SlotType.INVENTORY, slotIndex)
        }
        else if (player.itemOnCursor.type != Material.AIR) return NO_SLOT
        return CURSOR_SLOT
    }

    private class SlotContainer(val type: SlotType, val slot: Int)
    private enum class SlotType {
        CURSOR,
        INVENTORY,
        NO_SLOT

    }

}
