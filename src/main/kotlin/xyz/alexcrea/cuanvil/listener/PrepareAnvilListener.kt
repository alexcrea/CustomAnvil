package xyz.alexcrea.cuanvil.listener

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil
import com.jankominek.disenchantment.utils.AnvilCostUtils
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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dialog.AnvilRenameDialog
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.util.*
import xyz.alexcrea.cuanvil.util.AnvilXpUtil.AnvilCost
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil

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
    }

    /**
     * Event handler logic for when an anvil contains items to be combined
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun anvilCombineCheck(event: PrepareAnvilEvent) {
        val view = event.view
        val inventory = event.inventory

        val player = JustForEasierHotswapUtil.getPlayerFromView(view)
        if(player !is Player) return

        tryRenameDialog(player, event)

        // Test if custom anvil is bypassed before immutability test
        if (DependencyManager.earlyTryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvCost(inventory, view, player, AnvilCost(event.inventory.repairCost))
            return
        }

        val first = inventory.getItem(ANVIL_INPUT_LEFT)
        val second = inventory.getItem(ANVIL_INPUT_RIGHT)

        if(IS_EMPTY_TEST) {
            setNoResult(event, player, view)
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

            setNoResult(event, player, view)
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvCost(inventory, view, player, AnvilCost(event.inventory.repairCost))
            return
        }

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        if(first == null) {
            setNoResult(event, player, view)
            return
        }

        // Test custom recipe
        if (testCustomRecipe(event, inventory, player, first, second)) return

        // Test rename lonely item
        val shouldTryRename = second.isAir
        CustomAnvil.verboseLog("checking air in main logic: $shouldTryRename")
        if (shouldTryRename) {
            if(!doRenaming(event, inventory, player, first))
                setNoResult(event, player, view)
            return
        }

        // Test for merge
        if (first.canMergeWith(second!!)) {
            if(!doMerge(event, inventory, player, first, second))
                setNoResult(event, player, view)
            return
        }

        // Test for unit repair
        if (testUnitRepair(event, inventory, player, first, second)) return

        // Test for lore edit
        if (testLoreEdit(event, inventory, player, first, second)) return

        setNoResult(event, player, view)
    }

    private fun setNoResult(event: PrepareAnvilEvent, player: Player, view: InventoryView) {
        event.result = null
        AnvilXpUtil.onNoResult(player, view)
    }

    private fun tryRenameDialog(
        player: HumanEntity,
        event: PrepareAnvilEvent
    ) {
        if(!ConfigOptions.canUseDialogRename(player)) return

        AnvilRenameDialogUtil.anvilRenameDialog.tryShowDialog(player, event)
    }

    private fun processDialogPCD(it: ItemMeta, player: HumanEntity) {
        val keepDialog = ConfigOptions.canUseDialogRename(player) && ConfigOptions.shouldKeepRenameText

        val pdc = it.persistentDataContainer
        if(!keepDialog)
            pdc.remove(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY)
        else {
            val text = AnvilRenameDialogUtil.anvilRenameDialog.currentText(player)
            if(text == null || text.isBlank())
                pdc.remove(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY)
            else pdc.set(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY, PersistentDataType.STRING, text)
        }
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
        player: Player,
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

        val cost = AnvilCost()
        cost.recipe = if (recipe.removeExactLinearXp) AnvilXpUtil.calculateMinimumLevelForXp(xpCost)
            else AnvilXpUtil.calculateLevelForXp(xpCost)

        event.result = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.CUSTOM_CRAFT, cost)
        AnvilXpUtil.setAnvilInvCost(inventory, event.view, player, cost, true)
        return true
    }

    private fun doRenaming(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: Player, first: ItemStack
    ): Boolean {
        val resultItem = DependencyManager.cloneItem(event, first)
        val cost = AnvilCost()
        cost.rename = handleRename(resultItem, inventory, player)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("no right item, But input is same as output")
            return false
        }

        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.RENAME_ONLY)

        event.result = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.RENAME_ONLY, cost)
        AnvilXpUtil.setAnvilInvCost(inventory, event.view, player, cost)
        return true
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
                processDialogPCD(it, player)
                resultItem.itemMeta = it

                sumCost += ConfigOptions.itemRenameCost
            }

            return sumCost
        }
        return 0
    }

    private fun doMerge(
        event: PrepareAnvilEvent, inventory: AnvilInventory,
        player: Player,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val newEnchants = first.findEnchantments()
            .combineWith(second.findEnchantments(), first, player)
        var hasChanged = !isIdentical(first.findEnchantments(), newEnchants)

        val resultItem = DependencyManager.cloneItem(event, first)
        val cost = AnvilCost()
        if(hasChanged){
            resultItem.setEnchantmentsUnsafe(newEnchants)
            // Calculate enchantment cost
            AnvilXpUtil.getRightValues(second, resultItem, cost)
        }

        // Calculate repair cost
        if (!first.isEnchantedBook() && !second.isEnchantedBook()) {
            // we only need to be concerned with repair when neither item is a book
            val repaired = resultItem.repairFrom(first, second)
            cost.repair = if (repaired) ConfigOptions.itemRepairCost else 0
            hasChanged = hasChanged || repaired
        }

        // Test/stop if nothing changed.
        if (!hasChanged) {
            CustomAnvil.log("Mergeable with second, But input is same as output")
            return false
        }
        // As calculatePenalty edit result, we need to calculate penalty after checking equality
        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, second, resultItem, AnvilUseType.MERGE)
        // Calculate rename cost
        cost.rename = handleRename(resultItem, inventory, player)

        // Finally, we set result
        event.result = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.MERGE, cost)
        AnvilXpUtil.setAnvilInvCost(inventory, event.view, player, cost)
        return true
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
        event: PrepareAnvilEvent, inventory: AnvilInventory, player: Player,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val unitRepairAmount = first.getRepair(second) ?: return false

        val resultItem = DependencyManager.cloneItem(event, first)
        val cost = AnvilCost()
        cost.rename = handleRename(resultItem, inventory, player)

        val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
        if (repairAmount > 0) {
            cost.repair = repairAmount * ConfigOptions.unitRepairCost
        }
        // We do not care about right item penalty for unit repair
        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.UNIT_REPAIR)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("unit repair, But input is same as output")
            event.result = null
            return true
        }

        event.result = DependencyManager.tryTreatAnvilResult(event, resultItem, AnvilUseType.UNIT_REPAIR, cost)
        AnvilXpUtil.setAnvilInvCost(inventory, event.view, player, cost)
        return true
    }

    private fun testLoreEdit(
        event: PrepareAnvilEvent, inventory: AnvilInventory, player: Player,
        first: ItemStack, second: ItemStack
    ): Boolean {
        val type = second.type
        var result: ItemStack? = null

        val cost = AnvilCost()
        if (Material.WRITABLE_BOOK == type) {
            result = AnvilLoreEditUtil.tryLoreEditByBook(player, first, second, cost)
        } else if (Material.PAPER == type) {
            result = AnvilLoreEditUtil.tryLoreEditByPaper(player, first, second, cost)
        }

        if (result.isAir || first == result) {
            CustomAnvil.log("lore edit, But input is same as output")
            event.result = null
            return false
        }

        event.result = result
        AnvilXpUtil.setAnvilInvCost(inventory, event.view, player, cost)
        return true
    }
}