package io.delilaheve

import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.combineWith
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isBook
import io.delilaheve.util.ItemUtil.repairFrom
import io.delilaheve.util.ItemUtil.setEnchantmentsUnsafe
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
        val second = inventory.getItem(ANVIL_INPUT_RIGHT) ?: return
        if (first.canMergeWith(second)) {
            // Should find player
            val player = event.view.player

            val newEnchants = first.findEnchantments()
                .combineWith(second.findEnchantments(), first.type, player)
            val resultItem = first.clone()
            resultItem.setEnchantmentsUnsafe(newEnchants)

            var anvilCost = calculateCost(first, second, resultItem)
            if (!first.isBook() && !second.isBook()) {
                // we only need to be concerned with repair when neither item is a book
                val repaired = resultItem.repairFrom(first, second)
                anvilCost += if(repaired) 2 else 0
            }

            // Test if nothing change and stop.
            if(first == resultItem){
                event.result = null
                return
            }

            // Rename item and add renaming cost
            resultItem.itemMeta?.let {
                if(!it.displayName.contentEquals(inventory.renameText)){
                    it.setDisplayName(inventory.renameText)
                    anvilCost += ConfigOptions.itemRepairCost
                }
                resultItem.itemMeta = it
            }

            if (ConfigOptions.limitRepairCost) {
                anvilCost = min(anvilCost, ConfigOptions.limitRepairValue)
            }

            event.result = resultItem

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

    /**
     * Event handler logic for when a player is trying to pull an item out of the anvil
     */
    @EventHandler(ignoreCancelled = true)
    fun anvilExtractionCheck(event: InventoryClickEvent) {
        //val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory as? AnvilInventory ?: return
        if (event.rawSlot != ANVIL_OUTPUT_SLOT) { return }
        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        // Is true if there was no change. probably when there are conflict
        if(output == inventory.getItem(ANVIL_INPUT_LEFT)){
            event.result = Event.Result.DENY
            return
        }
        event.result = Event.Result.ALLOW
    }

    /**
     * Function to calculate most of the xp requirement for the anvil fuse
     * Change result work penalty for future use
     */
    private fun calculateCost(left: ItemStack, right: ItemStack, result: ItemStack): Int{
        // Extracted From https://minecraft.fandom.com/wiki/Anvil_mechanics#Enchantment_equation
        // Calculate work penality
        val leftPenality = (left.itemMeta as? Repairable)?.repairCost ?: 0
        val rightPenality = (right.itemMeta as? Repairable)?.repairCost ?: 0

        // Calculate right value and illegal enchant penalty
        var rightValue = 0
        var illegalPenalty = 0

        val rightIsFormBook = right.isBook()
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
            rightValue+=value

        }

        // Try to set work penality for the result item
        result.itemMeta?.let {
            (it as? Repairable)?.repairCost = leftPenality*2+1
            result.itemMeta = it
        }

        UnsafeEnchants.log("Calculated cost: " +
                "leftPenality: $leftPenality, " +
                "rightPenality: $rightPenality, " +
                "rightValue: $rightValue, " +
                "illegalPenalty: $illegalPenalty," +
                "result penality: ${(result.itemMeta as? Repairable)?.repairCost ?: "none"}")

        // We are missing [Renaming Cost] + [Refilling Durability] but it will be handled later
        return rightValue + leftPenality + rightPenality + illegalPenalty
    }

}
