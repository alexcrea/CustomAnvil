package io.delilaheve

import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.calculateValue
import io.delilaheve.util.EnchantmentUtil.combineWith
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isBook
import io.delilaheve.util.ItemUtil.setEnchantmentsUnsafe
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.permissions.Permission
import kotlin.math.min

/**
 * Listener for anvil events
 */
class AnvilEventListener : Listener {

    companion object {
        // Vanilla repair cost limit
        private const val VANILLA_REPAIR_LIMIT = 40
        // Anvil's output slot
        private const val ANVIL_OUTPUT_SLOT = 2
    }

    // Permission node required for the plugin to take over enchantment combination
    private val requirePermission: Permission
        get() = Permission(UnsafeEnchants.unsafePermission)

    /**
     * Event handler logic for when an anvil contains items to be combined
     */
    @EventHandler
    fun anvilCombineCheck(event: PrepareAnvilEvent) {
        val inventory = event.inventory
        val first = inventory.getItem(0) ?: return
        val second = inventory.getItem(1) ?: return
        if (first.canMergeWith(second)) {
            val firstEnchants = first.findEnchantments().toMutableMap()
            val secondEnchants = second.findEnchantments().toMutableMap()
            if (ConfigOptions.removeRepairLimit) {
                inventory.maximumRepairCost = Int.MAX_VALUE
            }
            val newEnchants = firstEnchants.combineWith(secondEnchants)
            val enchantsString = newEnchants.map { "${it.key.key} ${it.value}" }.joinToString(", ")
            UnsafeEnchants.log("New enchants for this item: $enchantsString")
            val resultItem = first.clone()
            resultItem.setEnchantmentsUnsafe(newEnchants)
            val firstValue = firstEnchants.calculateValue(first.isBook())
            val secondValue = secondEnchants.calculateValue(second.isBook())
            var repairCost = firstValue + secondValue
            if (first.isBook() && second.isBook()) {
                repairCost = firstEnchants.values.sum() + secondEnchants.values.sum()
            }
            if (ConfigOptions.limitRepairCost) {
                repairCost = min(repairCost, VANILLA_REPAIR_LIMIT)
            }
            inventory.repairCost = repairCost
            event.view.setProperty(InventoryView.Property.REPAIR_COST, repairCost)
            event.result = resultItem
        }
    }

    /**
     * Event handler logic for when a player is trying to pull an item out of the anvil
     */
    @EventHandler(ignoreCancelled = true)
    fun anvilExtractionCheck(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (player.hasPermission(requirePermission)) {
            val inventory = event.inventory as? AnvilInventory ?: return
            if (event.rawSlot != ANVIL_OUTPUT_SLOT) { return }
            val output = inventory.getItem(2) ?: return
            if (output.type == Material.AIR) { return }
            if (player.level < inventory.repairCost) { return }
            event.result = Event.Result.ALLOW
        }
    }

}
