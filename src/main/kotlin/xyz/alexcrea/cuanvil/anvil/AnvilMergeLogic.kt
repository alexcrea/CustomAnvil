package xyz.alexcrea.cuanvil.anvil

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.combineWith
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isEnchantedBook
import io.delilaheve.util.ItemUtil.repairFrom
import io.delilaheve.util.ItemUtil.setEnchantmentsUnsafe
import io.delilaheve.util.ItemUtil.unitRepair
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dialog.AnvilRenameDialog
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import xyz.alexcrea.cuanvil.util.CasedStringUtil
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import xyz.alexcrea.cuanvil.util.anvil.AnvilColorUtil
import xyz.alexcrea.cuanvil.util.anvil.AnvilLoreEditUtil
import xyz.alexcrea.cuanvil.util.anvil.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.config.LoreEditType
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil

object AnvilMergeLogic {

    open class AnvilResult {
        companion object {
            val EMPTY = AnvilResult(null, AnvilCost())
        }

        val item: ItemStack?
        val cost: AnvilCost
        val ignoreXpRules: Boolean

        constructor(item: ItemStack?, cost: AnvilCost, ignoreXpRules: Boolean = false) {
            this.item = item
            this.cost = cost
            this.ignoreXpRules = ignoreXpRules
        }

        fun isEmpty(): Boolean {
            return item == null
        }
    }

    class UnitRepairResult : AnvilResult {
        companion object {
            val EMPTY = UnitRepairResult(null, AnvilCost(), 0)
        }

        val repairAmount: Int

        constructor(item: ItemStack?, cost: AnvilCost, repairAmount: Int) : super(item, cost) {
            this.repairAmount = repairAmount
        }
    }

    class CustomCraftResult : AnvilResult {
        companion object {
            val EMPTY = CustomCraftResult(null, CustomCraftCost(0), 0, null)
        }

        val customCraftCost: CustomCraftCost
        val amount: Int
        val recipe: AnvilCustomRecipe?

        constructor(
            item: ItemStack?, cost: CustomCraftCost,
            amount: Int, recipe: AnvilCustomRecipe?
        ) : super(item, cost, true) {
            this.customCraftCost = cost
            this.amount = amount
            this.recipe = recipe
        }
    }

    class LoreEditResult : AnvilResult {
        companion object {
            val EMPTY = LoreEditResult(null, AnvilCost(), LoreEditType.APPEND_PAPER)
        }

        val type: LoreEditType

        constructor(item: ItemStack?, cost: AnvilCost, type: LoreEditType) : super(item, cost) {
            this.type = type
        }
    }

    fun doRenaming(
        view: InventoryView, //TODO use anvil view
        inventory: AnvilInventory,
        player: Player, first: ItemStack
    ): AnvilResult {
        val resultItem = DependencyManager.cloneItem(player, first)
        val cost = AnvilCost()
        cost.rename = handleRename(resultItem, inventory, player)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("no right item, But input is same as output")
            return AnvilResult.EMPTY
        }

        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.RENAME_ONLY)
        val result =
            DependencyManager.tryTreatAnvilResult(view, inventory, player, resultItem, AnvilUseType.RENAME_ONLY, cost)

        return AnvilResult(result, cost)
    }

    private fun processDialogPCD(meta: ItemMeta, player: HumanEntity) {
        val text = AnvilRenameDialogUtil.anvilRenameDialog.currentText(player)
        return processPCD(meta, player, text)
    }

    fun processPCD(meta: ItemMeta, player: HumanEntity, text: String?) {
        val keepDialog = ConfigOptions.canUseDialogRename(player) && ConfigOptions.shouldKeepRenameText

        val pdc = meta.persistentDataContainer
        if (!keepDialog)
            pdc.remove(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY)
        else {
            if (text == null || text.isBlank())
                pdc.remove(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY)
            else pdc.set(AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY, PersistentDataType.STRING, text)
        }
    }

    private fun handleRename(resultItem: ItemStack, inventory: AnvilInventory, player: HumanEntity): Int {
        // Can be null
        var renameText = ChatColor.stripColor(inventory.renameText)

        var sumCost = 0
        var useColor = false
        if (ConfigOptions.renameColorPossible && renameText != null) {
            val component = AnvilColorUtil.handleColor(
                renameText,
                AnvilColorUtil.renamePermission(player)
            )

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
                        )
            ) {
                it.setDisplayName(renameText)
                processDialogPCD(it, player)
                resultItem.itemMeta = it

                sumCost += ConfigOptions.itemRenameCost
            }

            return sumCost
        }
        return 0
    }

    fun doMerge(
        view: InventoryView, //TODO use anvil view instead
        inventory: AnvilInventory,
        player: Player,
        first: ItemStack, second: ItemStack
    ): AnvilResult {
        val newEnchants = first.findEnchantments()
            .combineWith(second.findEnchantments(), first, player)
        var hasChanged = !isIdentical(first.findEnchantments(), newEnchants)

        val resultItem = DependencyManager.cloneItem(player, first)
        val cost = AnvilCost()
        if (hasChanged) {
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
            return AnvilResult.EMPTY
        }
        // As calculatePenalty edit result, we need to calculate penalty after checking equality
        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, second, resultItem, AnvilUseType.MERGE)
        // Calculate rename cost
        cost.rename = handleRename(resultItem, inventory, player)

        val result =
            DependencyManager.tryTreatAnvilResult(view, inventory, player, resultItem, AnvilUseType.MERGE, cost)

        return AnvilResult(result, cost)
    }

    private fun isIdentical(
        firstEnchants: MutableMap<CAEnchantment, Int>,
        resultEnchants: MutableMap<CAEnchantment, Int>
    ): Boolean {
        if (firstEnchants.size != resultEnchants.size) return false
        for (entry in resultEnchants) {
            if (firstEnchants.getOrDefault(entry.key, entry.value - 1) != entry.value) return false
        }

        return true
    }

    // return true if a custom recipe exist with these ingredients
    fun testCustomRecipe(
        view: InventoryView, //TODO use anvil view instead
        inventory: AnvilInventory,
        player: Player,
        first: ItemStack, second: ItemStack?
    ): CustomCraftResult {
        val recipe = CustomRecipeUtil.getCustomRecipe(first, second)
        CustomAnvil.verboseLog("custom recipe not null? ${recipe != null}")
        if (recipe == null) return CustomCraftResult.EMPTY

        val amount = CustomRecipeUtil.getCustomRecipeAmount(recipe, first, second)

        val resultItem: ItemStack = DependencyManager.cloneItem(player, recipe.resultItem!!)
        resultItem.amount *= amount

        // Maybe add an option on custom craft to ignore/not ignore penalty ??
        val xpCost = recipe.determineCost(amount, first, resultItem)

        val cost = CustomCraftCost(xpCost)
        // This is for displayed cost
        cost.recipe = if (recipe.removeExactLinearXp) AnvilXpUtil.calculateMinimumLevelForXp(xpCost)
        else AnvilXpUtil.calculateLevelForXp(xpCost)

        val result =
            DependencyManager.tryTreatAnvilResult(view, inventory, player, resultItem, AnvilUseType.CUSTOM_CRAFT, cost)
        return CustomCraftResult(result, cost, amount, recipe)
    }

    fun testUnitRepair(
        view: InventoryView, //TODO use anvil view
        inventory: AnvilInventory,
        player: Player,
        first: ItemStack, second: ItemStack
    ): UnitRepairResult {
        val unitRepairAmount = first.getRepair(second) ?: return UnitRepairResult.EMPTY

        return testUnitRepair(view, inventory, player, first, second, unitRepairAmount)
    }

    fun testUnitRepair(
        view: InventoryView, //TODO use anvil view instead
        inventory: AnvilInventory,
        player: Player,
        first: ItemStack, second: ItemStack,
        unitRepairAmount: Double
    ): UnitRepairResult {
        val resultItem = DependencyManager.cloneItem(player, first)
        val cost = AnvilCost()
        cost.rename = handleRename(resultItem, inventory, player)

        val repairAmount = resultItem.unitRepair(second.amount, unitRepairAmount)
        if (repairAmount > 0)
            cost.repair = repairAmount * ConfigOptions.unitRepairCost

        // We do not care about right item penalty for unit repair
        cost.workPenalty = AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.UNIT_REPAIR)

        // Test/stop if nothing changed.
        if (first == resultItem) {
            CustomAnvil.log("unit repair, But input is same as output")
            return UnitRepairResult.EMPTY
        }

        val result =
            DependencyManager.tryTreatAnvilResult(view, inventory, player, resultItem, AnvilUseType.UNIT_REPAIR, cost)
        return UnitRepairResult(result, cost, repairAmount)
    }

    fun testLoreEdit(
        player: Player,
        first: ItemStack, second: ItemStack
    ): LoreEditResult {
        val type = second.type

        val result = if (Material.WRITABLE_BOOK == type)
            AnvilLoreEditUtil.tryLoreEditByBook(player, first, second)
        else if (Material.PAPER == type)
            AnvilLoreEditUtil.tryLoreEditByPaper(player, first, second)
        else LoreEditResult.EMPTY

        if (result.isEmpty()) return result

        if (result.item!!.isAir || first == result.item) {
            CustomAnvil.log("lore edit, But input is same as output")
            return LoreEditResult.EMPTY
        }

        return result
    }

}