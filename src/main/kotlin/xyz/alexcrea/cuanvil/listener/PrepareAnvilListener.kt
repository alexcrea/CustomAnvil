package xyz.alexcrea.cuanvil.listener

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil
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
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.util.*
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import java.util.concurrent.atomic.AtomicInteger

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
        val player: HumanEntity = InventoryViewUtil.getInstance().getPlayer(event.view)

        val inventory = event.inventory
        val first = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val second = inventory.getItem(ANVIL_INPUT_RIGHT)

        if (isImmutable(first) || isImmutable(second)) {
            CustomAnvil.verboseLog("Skipping anvil process as one of the two item is immutable")

            event.result = null
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryEventPreAnvilBypass(event, player)) return

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        // Test custom recipe
        if (testCustomRecipe(event, inventory, player, first, second)) return

        // Test rename lonely item
        if (second == null) {
            doRenaming(event, inventory, player, first)
            return
        }

        // Test for merge
        if (first.canMergeWith(second)) {
            doMerge(event, inventory, player, first, second)
            return
        }

        // Test for unit repair
        if (testUnitRepair(event, inventory, player, first, second)) return

        // Test for lore edit
        if (testLoreEdit(event, inventory, player, first, second)) return

        CustomAnvil.log("no anvil fuse type found")
        event.result = null

    }

    private fun isImmutable(item: ItemStack?): Boolean {
        if (item == null) return false

        val meta = item.itemMeta
        return meta != null &&
                (hasImmutableEnchants(meta) || hasImmutableStoredEnchants(meta))
    }

    private fun hasImmutableEnchants(meta: ItemMeta): Boolean {
        if (!meta.hasEnchants()) return false

        for (enchant in meta.enchants.keys) {
            if (ConfigOptions.isImmutable(enchant.key)) return true
        }
        return false
    }

    private fun hasImmutableStoredEnchants(meta: ItemMeta): Boolean {
        if (meta !is EnchantmentStorageMeta || !meta.hasStoredEnchants()) return false

        for (enchant in meta.storedEnchants.keys) {
            if (ConfigOptions.isImmutable(enchant.key)) return true
        }
        return false
    }

    // return true if a custom recipe exist with these ingredients
    private fun testCustomRecipe(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: HumanEntity,
        first: ItemStack, second: ItemStack?
    ): Boolean {
        val recipe = CustomRecipeUtil.getCustomRecipe(first, second)
        CustomAnvil.verboseLog("custom recipe not null? ${recipe != null}")
        if (recipe == null) return false

        val amount = CustomRecipeUtil.getCustomRecipeAmount(recipe, first, second)

        val resultItem: ItemStack = recipe.resultItem!!.clone()
        resultItem.amount *= amount

        event.result = resultItem
        if (DependencyManager.tryTreatAnvilResult(event, resultItem)) return true

        // Maybe add an option on custom craft to ignore/not ignore penalty ??
        var xpCost = recipe.xpCostPerCraft * amount
        xpCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.CUSTOM_CRAFT)

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, xpCost, true)

        return true
    }

    private fun doRenaming(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: HumanEntity, first: ItemStack
    ) {
        val resultItem = first.clone()
        var anvilCost = handleRename(resultItem, inventory, player)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("no right item, But input is same as output")
            event.result = null
            return
        }

        event.result = resultItem
        if (DependencyManager.tryTreatAnvilResult(event, resultItem)) return

        anvilCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.RENAME_ONLY)

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory, player: HumanEntity): Int {
        // Can be null
        var inventoryName = ChatColor.stripColor(inventory.renameText)

        var sumCost = 0
        var useColor = false
        if (ConfigOptions.renameColorPossible && inventoryName != null) {
            val resultString = StringBuilder(inventoryName)

            useColor = AnvilColorUtil.handleColor(
                resultString, player,
                ConfigOptions.permissionNeededForColor,
                ConfigOptions.allowColorCode, ConfigOptions.allowHexadecimalColor,
                AnvilColorUtil.ColorUseType.RENAME
            )

            if (useColor) {
                inventoryName = resultString.toString()

                sumCost += ConfigOptions.useOfColorCost
            }
        }

        // Rename item and add renaming cost
        resultItem.itemMeta?.let {
            val hasDisplayName = it.hasDisplayName()
            val displayName = if (!hasDisplayName) null
            else if (useColor) it.displayName
            else ChatColor.stripColor(it.displayName)

            if (!displayName.contentEquals(inventoryName)) {
                it.setDisplayName(inventoryName)
                resultItem.itemMeta = it

                sumCost += ConfigOptions.itemRenameCost
            }

            return sumCost
        }
        return 0
    }

    private fun doMerge(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: HumanEntity,
        first: ItemStack, second: ItemStack
    ) {
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
        anvilCost += AnvilXpUtil.calculatePenalty(first, second, resultItem, AnvilUseType.MERGE)
        // Calculate rename cost
        anvilCost += handleRename(resultItem, inventory, player)

        // Finally, we set result
        event.result = resultItem
        if (DependencyManager.tryTreatAnvilResult(event, resultItem)) return

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
    }

    // return true if there is a valid unit repair with these ingredients
    private fun testUnitRepair(
        event: PrepareAnvilEvent, inventory: AnvilInventory, player: HumanEntity,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val unitRepairAmount = first.getRepair(second) ?: return false

        val resultItem = first.clone()
        var anvilCost = handleRename(resultItem, inventory, player)

        val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
        if (repairAmount > 0) {
            anvilCost += repairAmount * ConfigOptions.unitRepairCost
        }
        // We do not care about right item penalty for unit repair
        anvilCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.UNIT_REPAIR)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("unit repair, But input is same as output")
            event.result = null
            return true
        }
        event.result = resultItem
        if (DependencyManager.tryTreatAnvilResult(event, resultItem)) return true

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, anvilCost)
        return true
    }

    private fun testLoreEdit(
        event: PrepareAnvilEvent, inventory: AnvilInventory, player: HumanEntity,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val type = second.type
        var result: ItemStack? = null

        val xpCost = AtomicInteger()
        if (Material.WRITABLE_BOOK == type) {
            result = AnvilLoreEditUtil.tryLoreEditByBook(player, first, second, xpCost)
        } else if (Material.PAPER == type) {
            result = AnvilLoreEditUtil.tryLoreEditByPaper(player, first, second, xpCost)
        }

        if (result == null || first == result) {
            CustomAnvil.log("lore edit, But input is same as output")
            event.result = null
            return false
        }

        event.result = result
        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, xpCost.get())
        return true
    }
}