package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.combineWith
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isEnchantedBook
import io.delilaheve.util.ItemUtil.repairFrom
import io.delilaheve.util.ItemUtil.setEnchantmentsUnsafe
import io.delilaheve.util.ItemUtil.unitRepair
import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.util.AnvilColorUtil
import xyz.alexcrea.cuanvil.util.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import java.util.logging.Level

/**
 * Listener for anvil events
 */
class PrepareAnvilListener : Listener {

    companion object {

        // Anvil's output slot
        const val ANVIL_INPUT_LEFT = 0
        const val ANVIL_INPUT_RIGHT = 1
        const val ANVIL_OUTPUT_SLOT = 2
    }

    /**
     * Event handler logic for when an anvil contains items to be combined
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun anvilCombineCheck(event: PrepareAnvilEvent) {
        // Should find player
        val player: HumanEntity = event.viewers.first()

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryEventPreAnvilBypass(event, player)) return

        val inventory = event.inventory
        val first = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val second = inventory.getItem(ANVIL_INPUT_RIGHT)

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        // Test custom recipe
        if(testCustomRecipe(event, inventory, player, first, second)) return

        // Test rename lonely item
        if(second == null) {
            doRenaming(event, inventory, player, first)
            return
        }

        // Test for merge
        if (first.canMergeWith(second)) {
            doMerge(event, inventory, player, first, second)
            return
        }

        // Test for unit repair
        if(testUnitRepair(event, inventory, player, first, second)) return

        CustomAnvil.log("no anvil fuse type found")
        event.result = null

    }

    // return true if a custom recipe exist with these ingredient
    private fun testCustomRecipe(event: PrepareAnvilEvent, inventory: AnvilInventory,
                                 player: HumanEntity,
                                 first: ItemStack, second: ItemStack?): Boolean {
        val recipe = CustomRecipeUtil.getCustomRecipe(first, second)
        CustomAnvil.verboseLog("custom recipe not null? ${recipe != null}")
        if(recipe == null) return false

        val amount = CustomRecipeUtil.getCustomRecipeAmount(recipe, first, second)

        val resultItem: ItemStack = recipe.resultItem!!.clone()
        resultItem.amount *= amount

        event.result = resultItem
        if(DependencyManager.tryTreatAnvilResult(event, resultItem)) return true
        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, recipe.xpCostPerCraft * amount, true)

        return true
    }

    private fun doRenaming(event: PrepareAnvilEvent, inventory: AnvilInventory,
                           player: HumanEntity, first: ItemStack) {
        val resultItem = first.clone()
        var anvilCost = handleRename(resultItem, inventory, player)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("no right item, But input is same as output")
            event.result = null
            return
        }

        event.result = resultItem
        if(DependencyManager.tryTreatAnvilResult(event, resultItem)) return

        anvilCost += AnvilXpUtil.calculatePenalty(first, null, resultItem)

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory, player: HumanEntity): Int {
        // Can be null
        var inventoryName = ChatColor.stripColor(inventory.renameText)

        var sumCost = 0
        var useColor = false
        if(ConfigOptions.renameColorPossible && inventoryName != null){
            val resultString = StringBuilder(inventoryName)

            useColor = AnvilColorUtil.handleRenamingColor(resultString, player)

            if(useColor) {
                inventoryName = resultString.toString()

                sumCost+= ConfigOptions.useOfColorCost
            }
        }

        // Rename item and add renaming cost
        resultItem.itemMeta?.let {
            val displayName =
                if (useColor) it.displayName
                else ChatColor.stripColor(it.displayName)

            if (!displayName.contentEquals(inventoryName)) {
                it.setDisplayName(inventoryName)
                resultItem.itemMeta = it

                sumCost+= ConfigOptions.itemRenameCost
            }

            return sumCost
        }
        return 0
    }

    private fun doMerge(event: PrepareAnvilEvent, inventory: AnvilInventory,
                        player: HumanEntity,
                        first: ItemStack, second: ItemStack) {
        val newEnchants = first.findEnchantments()
            .combineWith(second.findEnchantments(), first, player)
        val resultItem = first.clone()
        resultItem.setEnchantmentsUnsafe(newEnchants)

        // Calculate enchantment cost
        var anvilCost = AnvilXpUtil.getRightValues(second, resultItem)
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
        anvilCost += AnvilXpUtil.calculatePenalty(first, second, resultItem)
        // Calculate rename cost
        anvilCost += handleRename(resultItem, inventory, player)

        // Finally, we set result
        event.result = resultItem
        if(DependencyManager.tryTreatAnvilResult(event, resultItem)) return

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
    }

    // return true if there is a valid unit repair with these ingredients
    private fun testUnitRepair(event: PrepareAnvilEvent, inventory: AnvilInventory, player: HumanEntity,
                               first: ItemStack, second: ItemStack): Boolean {
        val unitRepairAmount = first.getRepair(second) ?: return false

        val resultItem = first.clone()
        var anvilCost = handleRename(resultItem, inventory, player)

        val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
        if (repairAmount > 0) {
            anvilCost += repairAmount * ConfigOptions.unitRepairCost
        }
        // We do not care about right item penalty for unit repair
        anvilCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, true)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("unit repair, But input is same as output")
            event.result = null
            return true
        }
        event.result = resultItem
        if(DependencyManager.tryTreatAnvilResult(event, resultItem)) return true

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
        return true
    }

}