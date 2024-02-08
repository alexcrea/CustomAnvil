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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView.Property.REPAIR_COST
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Repairable
import xyz.alexcrea.group.ConflictType
import xyz.alexcrea.util.UnitRepairUtil.getRepair
import kotlin.math.min

/**
 * Listener for anvil events
 */
class AnvilEventListener : Listener {

    companion object {
        // Anvil's output slot
        private const val ANVIL_INPUT_LEFT = 0
        private const val ANVIL_INPUT_RIGHT = 1
        private const val ANVIL_OUTPUT_SLOT = 2
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
        if(!player.hasPermission(UnsafeEnchants.unsafePermission)) return

        // Test rename lonely item
        if(second == null){
            val resultItem = first.clone()
            var anvilCost = handleRename(resultItem, inventory)
            anvilCost+= calculatePenalty(first,null,resultItem)

            // Test/stop if nothing changed.
            if(first == resultItem){
                event.result = null
                return
            }
            // We do set item here as vanilla do all of our job (renaming)

            handleDisplayedXp(inventory, event, anvilCost)
            return
        }

        // Test for merge
        if (first.canMergeWith(second)) {

            val newEnchants = first.findEnchantments()
                .combineWith(second.findEnchantments(), first.type, player)
            val resultItem = first.clone()
            resultItem.setEnchantmentsUnsafe(newEnchants)

            var anvilCost = calculatePenalty(first, second, resultItem)
            anvilCost+= getRightValues(second, resultItem)
            if (!first.isEnchantedBook() && !second.isEnchantedBook()) {
                // we only need to be concerned with repair when neither item is a book
                val repaired = resultItem.repairFrom(first, second)
                anvilCost += if(repaired) ConfigOptions.itemRepairCost else 0
            }

            // Test/stop if nothing changed.
            if(first == resultItem){
                event.result = null
                return
            }

            anvilCost+= handleRename(resultItem, inventory)

            if (ConfigOptions.limitRepairCost) {
                anvilCost = min(anvilCost, ConfigOptions.limitRepairValue)
            }
            event.result = resultItem

            handleDisplayedXp(inventory, event, anvilCost)
            return
        }

        // Test for unit repair
        val unitRepairAmount = first.getRepair(second)
        if(unitRepairAmount != null){
            val resultItem = first.clone()
            var anvilCost = handleRename(resultItem, inventory)
            // We do not care about right item penalty for unit repair
            anvilCost+= calculatePenalty(first,null,resultItem)

            val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
            if(repairAmount > 0){
                anvilCost += repairAmount*ConfigOptions.unitRepairCost
            }

            // Test/stop if nothing changed.
            if(first == resultItem){
                event.result = null
                return
            }
            event.result = resultItem

            handleDisplayedXp(inventory, event, anvilCost)
        }else{
            event.result = null
        }
    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory): Int{
        // Rename item and add renaming cost
        resultItem.itemMeta?.let {
            if(!it.displayName.contentEquals(inventory.renameText)){
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
        if(!player.hasPermission(UnsafeEnchants.unsafePermission)) return
        val inventory = event.inventory as? AnvilInventory ?: return
        if (event.rawSlot != ANVIL_OUTPUT_SLOT) { return }
        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        val leftItem = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val rightItem = inventory.getItem(ANVIL_INPUT_RIGHT)

        val canMerge = leftItem.canMergeWith(rightItem)
        val unitRepairResult = leftItem.getRepair(rightItem)
        val allowed = (rightItem == null)
                || (canMerge)
                || (unitRepairResult != null)
        // True if there was no change or not allowed
        if((output == inventory.getItem(ANVIL_INPUT_LEFT))
            || !allowed){

            event.result = Event.Result.DENY
            return
        }
        if(rightItem == null){
            event.result = Event.Result.ALLOW
            return
        }
        if(canMerge){
            event.result = Event.Result.ALLOW
        }else if(unitRepairResult != null){
            val resultCopy = leftItem.clone()
            val resultAmount = resultCopy.unitRepair(
                rightItem.amount, unitRepairResult)

            // To avoid vanilla, we cancel the event for unit repair
            event.result = Event.Result.DENY
            event.isCancelled = true
            // And we give the item manually
            // But first we check if we should give the item
            if(player.itemOnCursor.type != Material.AIR) return
            if(inventory.repairCost > player.level) return

            // Get repairCost
            var repairCost = 0
            leftItem.itemMeta?.let { leftMeta ->
                val leftName = leftMeta.displayName
                output.itemMeta?.let {
                    if(!leftName.contentEquals(it.displayName)){
                        repairCost+= ConfigOptions.itemRenameCost
                    }
                }
            }

            repairCost+= calculatePenalty(leftItem,null,resultCopy)
            repairCost+= resultAmount*ConfigOptions.unitRepairCost

            if((inventory.maximumRepairCost < repairCost)
                || (player.level < repairCost)) return

            // We remove what should be removed
            inventory.setItem(ANVIL_INPUT_LEFT,null)
            rightItem.amount-= resultAmount
            inventory.setItem(ANVIL_INPUT_RIGHT,rightItem)
            inventory.setItem(ANVIL_OUTPUT_SLOT, null)

            UnsafeEnchants.log("repair cost: $repairCost")
            player.level-= repairCost

            // Finally, we add the item to the player
            player.setItemOnCursor(output)

            return
        }
    }

    /**
     * Function to calculate work penalty of anvil work
     * Also change result work penalty
     */
    private fun calculatePenalty(left: ItemStack, right: ItemStack?, result: ItemStack): Int{
        // Extracted From https://minecraft.fandom.com/wiki/Anvil_mechanics#Enchantment_equation
        // Calculate work penality
        val leftPenality = (left.itemMeta as? Repairable)?.repairCost ?: 0
        val rightPenality =
            if(right == null){ 0 }
            else{ (right.itemMeta as? Repairable)?.repairCost ?: 0 }

        // Try to set work penality for the result item
        result.itemMeta?.let {
            (it as? Repairable)?.repairCost = leftPenality*2+1
            result.itemMeta = it
        }

        UnsafeEnchants.log("Calculated penality: " +
                "leftPenality: $leftPenality, " +
                "rightPenality: $rightPenality, " +
                "result penality: ${(result.itemMeta as? Repairable)?.repairCost ?: "none"}")

        return leftPenality + rightPenality
    }

    /**
     * Function to calculate right enchantment values
     * it include enchantment placed on final item and conflicting enchantment
     */
    private fun getRightValues(right: ItemStack, result:ItemStack) : Int {
        // Calculate right value and illegal enchant penalty
        var illegalPenalty = 0
        var rightValue = 0

        val rightIsFormBook = right.isEnchantedBook()
        val resultEnchs = result.findEnchantments()
        val resultEnchsKeys = HashSet(resultEnchs.keys)

        for (enchantment in right.findEnchantments()) {
            // count enchant as illegal enchant if it conflicts with another enchant or not in result
            if((enchantment.key !in resultEnchsKeys)){
                resultEnchsKeys.add(enchantment.key)
                val conflictType = UnsafeEnchants.conflictManager.isConflicting(resultEnchsKeys,result.type,enchantment.key)
                resultEnchsKeys.remove(enchantment.key)

                if(ConflictType.BIG_CONFLICT == conflictType){
                    illegalPenalty += ConfigOptions.sacrificeIllegalCost
                }
                continue
            }
            // We know "enchantment.key in resultEnchs" true
            val resultLevel = resultEnchs[enchantment.key]!!

            val enchantmentMultiplier = ConfigOptions.enchantmentValue(enchantment.key, rightIsFormBook)
            val value = resultLevel * enchantmentMultiplier
            UnsafeEnchants.log("Value for ${enchantment.key.enchantmentName} level ${enchantment.value} is $value")
            rightValue += value

        }
        UnsafeEnchants.log("Calculated right values: " +
                "rightValue: $rightValue, " +
                "illegalPenalty: $illegalPenalty")

        return rightValue + illegalPenalty
    }

    /**
     * Display xp needed for the work on the anvil inventory
     */
    private fun handleDisplayedXp(inventory: AnvilInventory,
                                  event: PrepareAnvilEvent,
                                  anvilCost: Int){
        inventory.maximumRepairCost = Int.MAX_VALUE
        inventory.repairCost = anvilCost
        /* Because Minecraft likes to have the final say in the repair cost displayed
            * we need to wait for the event to end before overriding it, this ensures that
            * we have the final say in the process. */
        UnsafeEnchants.instance
            .server
            .scheduler
            .runTask(UnsafeEnchants.instance, Runnable {
                if (ConfigOptions.removeRepairLimit) {
                    inventory.maximumRepairCost = Int.MAX_VALUE
                }
                inventory.repairCost = anvilCost

                event.view.setProperty(REPAIR_COST, anvilCost)
            })
    }

}
