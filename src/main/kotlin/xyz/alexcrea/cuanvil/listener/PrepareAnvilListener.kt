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
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.util.*
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil
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

        var IS_EMPTY_TEST = false

        const val RENAME_DIALOG_PERMISSION = "ca.rename.dialog"
    }

    /**
     * Event handler logic for when an anvil contains items to be combined
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun anvilCombineCheck(event: PrepareAnvilEvent) {
        // Should find player
        val player: HumanEntity = InventoryViewUtil.getInstance().getPlayer(event.view)
        val inventory = event.inventory

        // Test if custom anvil is bypassed before immutability test
        if (DependencyManager.earlyTryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, event.inventory.repairCost)
            return
        }

        val first = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val second = inventory.getItem(ANVIL_INPUT_RIGHT)

        if(IS_EMPTY_TEST) {
            event.result = null
            IS_EMPTY_TEST = false
            return
        }

        if (ConfigOptions.verboseDebugLog) {
            CustomAnvil.verboseLog("Testing items:")
            CustomAnvil.verboseLog("first: $first")
            CustomAnvil.verboseLog("second: $second")
        }
        if (isImmutable(first) || isImmutable(second)) {
            CustomAnvil.verboseLog("Skipping anvil process as one of the two item is immutable")

            event.result = null
            return
        }

        tryRenameDialog(player, event)

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, event.inventory.repairCost)
            return
        }

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        // Test custom recipe
        if (testCustomRecipe(event, inventory, player, first, second)) return

        // Test rename lonely item
        val isAir = second.isAir
        CustomAnvil.verboseLog("checking air in main logic: $isAir")
        if (isAir) {
            doRenaming(event, inventory, player, first)
            return
        }

        // Test for merge
        if (first.canMergeWith(second!!)) {
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

    private fun tryRenameDialog(
        player: HumanEntity,
        event: PrepareAnvilEvent
    ) {
        if(!ConfigOptions.doRenameDialog || !AnvilRenameDialogUtil.anvilRenameDialog.canSendDialog()) return
        if(ConfigOptions.doRenameDialogUsePermission && !player.hasPermission(RENAME_DIALOG_PERMISSION)) return

        AnvilRenameDialogUtil.anvilRenameDialog.tryShowDialog(player, event)
    }

    private fun isImmutable(item: ItemStack?): Boolean {
        if (item.isAir) return false

        val meta = item!!.itemMeta
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

        val resultItem: ItemStack = DependencyManager.cloneItem(event, recipe.resultItem!!)
        resultItem.amount *= amount

        // Maybe add an option on custom craft to ignore/not ignore penalty ??
        val xpCost = recipe.determineCost(amount, first, resultItem)

        val levelCost =
            if (recipe.removeExactLinearXp) AnvilXpUtil.calculateMinimumLevelForXp(xpCost)
            else AnvilXpUtil.calculateLevelForXp(xpCost)

        val finalResult = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.CUSTOM_CRAFT, levelCost)
        if (finalResult == null) return false

        event.result = finalResult.result
        if (finalResult.result.isAir) return false

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, finalResult.levelCost, true)
        return true
    }

    private fun doRenaming(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: HumanEntity, first: ItemStack
    ) {
        val resultItem = DependencyManager.cloneItem(event, first)
        var anvilCost = handleRename(resultItem, inventory, player)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("no right item, But input is same as output")
            event.result = null
            return
        }

        anvilCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.RENAME_ONLY)

        val finalResult = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.RENAME_ONLY, anvilCost)
        if (finalResult == null) return

        event.result = finalResult.result
        if (finalResult.result.isAir) return

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, finalResult.levelCost)
    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory, player: HumanEntity): Int {
        // Can be null
        var renameText = ChatColor.stripColor(inventory.renameText)

        var sumCost = 0
        var useColor = false
        if (ConfigOptions.renameColorPossible && renameText != null) {
            val component = AnvilColorUtil.handleColor(
                renameText,
                AnvilColorUtil.renamePermission(player))

            if (component != null) {
                renameText = MiniMessageUtil.legacy_mm.serialize(component)

                sumCost += ConfigOptions.useOfColorCost
                useColor = true
            }
        }

        // Rename item and add renaming cost
        resultItem.itemMeta?.let {
            val hasDisplayName = it.hasDisplayName()
            val displayName = if (!hasDisplayName) null
            else if (useColor) it.displayName
            else ChatColor.stripColor(it.displayName)


            if (!displayName.contentEquals(renameText) && !(displayName == null &&
                        renameText == "" ||
                        //TODO on recent paper check effective name instead
                    renameText == CasedStringUtil.snakeToUpperSpacedCase(resultItem.type.name.lowercase())
                    )) {
                it.setDisplayName(renameText)
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
        var hasChanged = !isIdentical(first.findEnchantments(), newEnchants)

        val resultItem = DependencyManager.cloneItem(event, first)
        var anvilCost = 0
        if(hasChanged){
            resultItem.setEnchantmentsUnsafe(newEnchants)
            // Calculate enchantment cost
            anvilCost+= AnvilXpUtil.getRightValues(second, resultItem)
        }

        // Calculate repair cost
        if (!first.isEnchantedBook() && !second.isEnchantedBook()) {
            // we only need to be concerned with repair when neither item is a book
            val repaired = resultItem.repairFrom(first, second)
            anvilCost += if (repaired) ConfigOptions.itemRepairCost else 0
            hasChanged = hasChanged || repaired
        }

        // Test/stop if nothing changed.
        if (!hasChanged) {
            CustomAnvil.log("Mergable with second, But input is same as output")
            event.result = null
            return
        }
        // As calculatePenalty edit result, we need to calculate penalty after checking equality
        anvilCost += AnvilXpUtil.calculatePenalty(first, second, resultItem, AnvilUseType.MERGE)
        // Calculate rename cost
        anvilCost += handleRename(resultItem, inventory, player)

        // Finally, we set result
        val finalResult = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.MERGE, anvilCost)
        if (finalResult == null) return

        event.result = finalResult.result
        if (finalResult.result.isAir) return

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, finalResult.levelCost)
    }

    private fun isIdentical(
        firstEnchants: MutableMap<CAEnchantment, Int>,
        resultEnchants: MutableMap<CAEnchantment, Int>
    ): Boolean {
        if(firstEnchants.size != resultEnchants.size) return false
        for (entry in resultEnchants) {
            if(firstEnchants.getOrDefault(entry.key, entry.value-1) != entry.value) return false
        }

        return true
    }

    // return true if there is a valid unit repair with these ingredients
    private fun testUnitRepair(
        event: PrepareAnvilEvent, inventory: AnvilInventory, player: HumanEntity,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val unitRepairAmount = first.getRepair(second) ?: return false

        val resultItem = DependencyManager.cloneItem(event, first)
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

        val finalResult = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.UNIT_REPAIR, anvilCost)
        if (finalResult == null) return false

        event.result = finalResult.result
        if (finalResult.result.isAir) return false

        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, finalResult.levelCost)
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

        if (result.isAir || first == result) {
            CustomAnvil.log("lore edit, But input is same as output")
            event.result = null
            return false
        }

        event.result = result
        AnvilXpUtil.setAnvilInvXp(inventory, event.view, player, xpCost.get())
        return true
    }
}