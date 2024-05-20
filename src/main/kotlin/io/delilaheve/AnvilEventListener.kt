package io.delilaheve

import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.combineWith
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isEnchantedBook
import io.delilaheve.util.ItemUtil.repairFrom
import io.delilaheve.util.ItemUtil.setEnchantmentsUnsafe
import io.delilaheve.util.ItemUtil.unitRepair
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView.Property.REPAIR_COST
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Repairable
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.group.ConflictType
import xyz.alexcrea.cuanvil.packet.PacketManager
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import kotlin.math.min


/**
 * Listener for anvil events
 */
class AnvilEventListener(private val packetManager: PacketManager) : Listener {

    companion object {
        // Anvil's output slot
        private const val ANVIL_INPUT_LEFT = 0
        private const val ANVIL_INPUT_RIGHT = 1
        private const val ANVIL_OUTPUT_SLOT = 2

        // static slot container
        private val NO_SLOT = SlotContainer(SlotType.NO_SLOT, 0)
        private val CURSOR_SLOT = SlotContainer(SlotType.CURSOR, 0)
    }

    /**
     * Event handler logic for when an anvil contains items to be combined
     */
    @EventHandler(priority = HIGHEST)
    fun anvilCombineCheck(event: PrepareAnvilEvent) {
        val inventory = event.inventory
        val first = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val second = inventory.getItem(ANVIL_INPUT_RIGHT)

        // Should find player
        val player = event.view.player
        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        // Test custom recipe
        val recipe = getCustomRecipe(first, second)
        CustomAnvil.verboseLog("custom recipe not null? ${recipe != null}")
        if(recipe != null){
            val amount = getCustomRecipeAmount(recipe, first, second)

            val resultItem: ItemStack = recipe.resultItem!!.clone()
            resultItem.amount *= amount

            event.result = resultItem
            handleAnvilXp(inventory, event, recipe.xpCostPerCraft * amount, true)

            return
        }

        // Test rename lonely item
        if (second == null) {
            val resultItem = first.clone()
            var anvilCost = handleRename(resultItem, inventory)

            // Test/stop if nothing changed.
            if (first == resultItem) {
                CustomAnvil.log("no right item, But input is same as output")
                event.result = null
                return
            }
            // We don't manually set item here as vanilla do it (renaming)
            //event.result = null

            anvilCost += calculatePenalty(first, null, resultItem)

            handleAnvilXp(inventory, event, anvilCost)
            return
        }

        // Test for merge
        if (first.canMergeWith(second)) {

            val newEnchants = first.findEnchantments()
                .combineWith(second.findEnchantments(), first.type, player)
            val resultItem = first.clone()
            resultItem.setEnchantmentsUnsafe(newEnchants)

            // Calculate enchantment cost
            var anvilCost = getRightValues(second, resultItem)
            // Calculate repair cost
            if (!first.isEnchantedBook() && !second.isEnchantedBook()) {
                // we only need to be concerned with repair when neither item is a book
                val repaired = resultItem.repairFrom(first, second)
                anvilCost += if (repaired) ConfigOptions.itemRepairCost else 0
            }

            // Test/stop if nothing changed.
            if (first == resultItem) {
                CustomAnvil.log("Mergable with second, But input is same as output")
                event.result = null
                return
            }
            // As calculatePenalty edit result, we need to calculate penalty after checking equality
            anvilCost += calculatePenalty(first, second, resultItem)
            // Calculate rename cost
            anvilCost += handleRename(resultItem, inventory)

            // Finally, we set result
            event.result = resultItem

            handleAnvilXp(inventory, event, anvilCost)
            return
        }

        // Test for unit repair
        val unitRepairAmount = first.getRepair(second)
        if (unitRepairAmount != null) {
            val resultItem = first.clone()
            var anvilCost = handleRename(resultItem, inventory)

            val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
            if (repairAmount > 0) {
                anvilCost += repairAmount * ConfigOptions.unitRepairCost
            }
            // We do not care about right item penalty for unit repair
            anvilCost += calculatePenalty(first, null, resultItem)

            // Test/stop if nothing changed.
            if (first == resultItem) {
                CustomAnvil.log("unit repair, But input is same as output")
                event.result = null
                return
            }
            event.result = resultItem

            handleAnvilXp(inventory, event, anvilCost)
        } else {
            CustomAnvil.log("no anvil fuse type found")
            event.result = null
        }

    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory): Int {
        // Rename item and add renaming cost
        resultItem.itemMeta?.let {
            val displayName = ChatColor.stripColor(it.displayName)
            val inventoryName = ChatColor.stripColor(inventory.renameText)
            if (!displayName.contentEquals(inventoryName)) {
                it.setDisplayName(inventory.renameText)
                resultItem.itemMeta = it
                return ConfigOptions.itemRenameCost
            }
        }
        return 0
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
        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        val leftItem = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val rightItem = inventory.getItem(ANVIL_INPUT_RIGHT)

        // Test custom recipe
        val recipe = getCustomRecipe(leftItem, rightItem)
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
                              inventory: AnvilInventory) {
        event.result = Event.Result.DENY

        if(recipe.leftItem == null) return // in case it changed

        val amount = getCustomRecipeAmount(recipe, leftItem, rightItem)
        val xpCost = amount * recipe.xpCostPerCraft

        if ((player.gameMode != GameMode.CREATIVE) && (player.level < xpCost)) return

        // We give the item manually
        // But first we check if we should give the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return

        // If not creative middle click...
        if (event.click != ClickType.MIDDLE) {
            // We remove what should be removed
            leftItem.amount -= amount * recipe.leftItem!!.amount
            inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

            if(rightItem != null){
                if(recipe.rightItem == null) return // in case it changed

                rightItem.amount -= amount * recipe.rightItem!!.amount
                inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)
            }
            player.level -= amount

            // Then we try to find the new values for the anvil
            val newAmount = getCustomRecipeAmount(recipe, leftItem, rightItem)

            CustomAnvil.verboseLog("new amount is $newAmount")
            if(newAmount <= 0 || recipe.exactCount){
                inventory.setItem(ANVIL_OUTPUT_SLOT, null)
            }else{
                val resultItem: ItemStack = recipe.resultItem!!.clone()
                resultItem.amount *= newAmount

                val newXp = newAmount * newAmount

                inventory.repairCost = newXp
                event.view.setProperty(REPAIR_COST, newXp)

                inventory.setItem(ANVIL_OUTPUT_SLOT, resultItem)

                player.updateInventory()
            }
        }

        // Finally, we add the item to the player
        if (slotDestination.type == SlotType.CURSOR) {
            player.setItemOnCursor(output)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, output)
        }


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
        var repairCost = 0
        if (player.gameMode != GameMode.CREATIVE) {
            // Get repairCost
            leftItem.itemMeta?.let { leftMeta ->
                val leftName = leftMeta.displayName
                output.itemMeta?.let {
                    if (!leftName.contentEquals(it.displayName)) {
                        repairCost += ConfigOptions.itemRenameCost
                    }
                }
            }

            repairCost += calculatePenalty(leftItem, null, resultCopy)
            repairCost += resultAmount * ConfigOptions.unitRepairCost

            if ((inventory.maximumRepairCost < repairCost)
                || (player.level < repairCost)
            ) return
        }
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
        } else {
            if (player.itemOnCursor.type != Material.AIR) {
                return NO_SLOT
            }
            return CURSOR_SLOT
        }
    }

    /**
     * Function to calculate work penalty of anvil work
     * Also change result work penalty
     */
    private fun calculatePenalty(left: ItemStack, right: ItemStack?, result: ItemStack): Int {
        // Extracted From https://minecraft.fandom.com/wiki/Anvil_mechanics#Enchantment_equation
        // Calculate work penalty
        val leftPenalty = (left.itemMeta as? Repairable)?.repairCost ?: 0
        val rightPenalty =
            if (right == null) {
                0
            } else {
                (right.itemMeta as? Repairable)?.repairCost ?: 0
            }

        // Try to set work penalty for the result item
        result.itemMeta?.let {
            (it as? Repairable)?.repairCost = leftPenalty * 2 + 1
            result.itemMeta = it
        }

        CustomAnvil.log(
            "Calculated penalty: " +
                    "leftPenalty: $leftPenalty, " +
                    "rightPenalty: $rightPenalty, " +
                    "result penalty: ${(result.itemMeta as? Repairable)?.repairCost ?: "none"}"
        )

        return leftPenalty + rightPenalty
    }

    /**
     * Function to calculate right enchantment values
     * it include enchantment placed on final item and conflicting enchantment
     */
    private fun getRightValues(right: ItemStack, result: ItemStack): Int {
        // Calculate right value and illegal enchant penalty
        var illegalPenalty = 0
        var rightValue = 0

        val rightIsFormBook = right.isEnchantedBook()
        val resultEnchs = result.findEnchantments()
        val resultEnchsKeys = HashSet(resultEnchs.keys)

        for (enchantment in right.findEnchantments()) {
            // count enchant as illegal enchant if it conflicts with another enchant or not in result
            if ((enchantment.key !in resultEnchsKeys)) {
                resultEnchsKeys.add(enchantment.key)
                val conflictType = ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(
                    resultEnchsKeys,
                    result.type,
                    enchantment.key
                )
                resultEnchsKeys.remove(enchantment.key)

                if (ConflictType.BIG_CONFLICT == conflictType) {
                    illegalPenalty += ConfigOptions.sacrificeIllegalCost
                }
                continue
            }
            // We know "enchantment.key in resultEnchs" true
            val resultLevel = resultEnchs[enchantment.key]!!

            val enchantmentMultiplier = ConfigOptions.enchantmentValue(enchantment.key, rightIsFormBook)
            val value = resultLevel * enchantmentMultiplier
            CustomAnvil.log("Value for ${enchantment.key.enchantmentName} level ${enchantment.value} is $value")
            rightValue += value

        }
        CustomAnvil.log(
            "Calculated right values: " +
                    "rightValue: $rightValue, " +
                    "illegalPenalty: $illegalPenalty"
        )

        return rightValue + illegalPenalty
    }

    private fun getCustomRecipe (
        leftItem: ItemStack,
        rightItem: ItemStack?) : AnvilCustomRecipe? {

        val recipeList = ConfigHolder.CUSTOM_RECIPE_HOLDER.recipeManager.recipeByMat[leftItem.type] ?: return null

        CustomAnvil.verboseLog("Testing " + recipeList.size+" recipe...")
        for (recipe in recipeList) {
            if(recipe.testItem(leftItem, rightItem)){
                return recipe
            }
        }

        return null
    }

    private fun getCustomRecipeAmount(
        recipe: AnvilCustomRecipe,
        leftItem: ItemStack,
        rightItem: ItemStack?
    ): Int{
        return if(recipe.exactCount) {
            if(leftItem.amount != recipe.leftItem!!.amount){
                0
            }else if(rightItem != null && rightItem.amount != recipe.rightItem!!.amount){
                0
            }else{
                1
            }
        }
        else {
            // test amount
            val resultItem = recipe.resultItem!! // we know exist as the recipe was returned to us
            val maxResultAmount = resultItem.type.maxStackSize/resultItem.amount
            val maxLeftAmount = leftItem.amount/recipe.leftItem!!.amount
            val maxRightAmount = if(rightItem == null){ maxLeftAmount } else{ rightItem.amount/recipe.rightItem!!.amount }

            CustomAnvil.verboseLog("resultItem: $resultItem, maxResultAmount: $maxResultAmount, maxLeftAmount: $maxLeftAmount, maxRightAmount: $maxRightAmount")

            min(min(maxResultAmount, maxLeftAmount), maxRightAmount)
        }
    }


    /**
     * Display xp needed for the work on the anvil inventory
     */
    private fun handleAnvilXp(
        inventory: AnvilInventory,
        event: PrepareAnvilEvent,
        anvilCost: Int,
        ignoreRules: Boolean = false
    ) {
        // Test repair cost limit
        val finalAnvilCost = if (ConfigOptions.limitRepairCost && !ignoreRules) {
            min(anvilCost, ConfigOptions.limitRepairValue)
        } else {
            anvilCost
        }

        /* Because Minecraft likes to have the final say in the repair cost displayed
            * we need to wait for the event to end before overriding it, this ensures that
            * we have the final say in the process. */
        CustomAnvil.instance
            .server
            .scheduler
            .runTask(CustomAnvil.instance, Runnable {
                if (ConfigOptions.removeRepairLimit || ignoreRules) {
                    inventory.maximumRepairCost = Int.MAX_VALUE
                } else{
                    inventory.maximumRepairCost = 40 // minecraft default
                }
                inventory.repairCost = finalAnvilCost

                event.view.setProperty(REPAIR_COST, finalAnvilCost)

                val player = event.view.player
                if(player is Player){
                    if(player.gameMode != GameMode.CREATIVE ){
                        packetManager.setInstantBuild(player, (ConfigOptions.replaceToExpensive) && (finalAnvilCost >= 40))
                    }
                    player.updateInventory()

                }
            })
    }

    @EventHandler
    fun onAnvilClose(event: InventoryCloseEvent){
        val player = event.player
        if(event.inventory !is AnvilInventory) return
        if(player is Player && GameMode.CREATIVE != player.gameMode){
            packetManager.setInstantBuild(player, false)
        }

    }

}




private class SlotContainer(val type: SlotType, val slot: Int)
private enum class SlotType {
    CURSOR,
    INVENTORY,
    NO_SLOT

}
