package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.canMergeWith
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import xyz.alexcrea.cuanvil.anvil.AnvilCost
import org.bukkit.inventory.view.AnvilView
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.AnvilResult
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.doMerge
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.doRenaming
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.testCustomRecipe
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.testLoreEdit
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.testUnitRepair
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir
import xyz.alexcrea.cuanvil.util.anvil.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil

/**
 * Listener for anvil events
 */
@Suppress("UnstableApiUsage")
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
        val player = view.player

        if (player !is Player) return

        tryRenameDialog(player, event)

        // Test if custom anvil is bypassed before immutability test
        if (DependencyManager.earlyTryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvCost(view, player, AnvilCost(view.repairCost))
            return
        }

        val first = view.getItem(ANVIL_INPUT_LEFT)
        val second = view.getItem(ANVIL_INPUT_RIGHT)

        if (IS_EMPTY_TEST) {
            IS_EMPTY_TEST = false
            applyResult(event, player, AnvilResult.EMPTY)
            return
        }

        if (ConfigOptions.verboseDebugLog) {
            CustomAnvil.verboseLog("Testing items:")
            CustomAnvil.verboseLog("first: $first")
            CustomAnvil.verboseLog("second: $second")
        }
        if (isImmutable(first) || isImmutable(second)) {
            CustomAnvil.verboseLog("Skipping anvil process as one of the two item is immutable")

            applyResult(event, player, AnvilResult.EMPTY)
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryEventPreAnvilBypass(event, player)) {
            // even if we got bypassed we still want to set price
            AnvilXpUtil.setAnvilInvCost(view, player, AnvilCost(view.repairCost))
            return
        }

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        val result = getResult(view, player, first, second)
        applyResult(event, player, result)
    }

    fun getResult(
        view: AnvilView,
        player: Player,
        first: ItemStack?, second: ItemStack?
    ): AnvilResult {
        if (first == null)
            return AnvilResult.EMPTY

        // Test custom recipe
        var result: AnvilResult = testCustomRecipe(view, player, first, second)
        if (!result.isEmpty())
            return result

        // Test rename lonely item
        val shouldTryRename = second.isAir
        CustomAnvil.verboseLog("checking air in main logic: $shouldTryRename")
        if (shouldTryRename)
            return doRenaming(view, player, first)

        // Test for merge
        if (first.canMergeWith(second!!))
            return doMerge(view, player, first, second)

        // Test for unit repair
        result = testUnitRepair(view, player, first, second)
        if (!result.isEmpty())
            return result

        // Test for lore edit
        result = testLoreEdit(player, first, second)
        if (!result.isEmpty())
            return result

        return AnvilResult.EMPTY
    }

    private fun tryRenameDialog(
        player: HumanEntity,
        event: PrepareAnvilEvent
    ) {
        if (!ConfigOptions.canUseDialogRename(player)) return

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

    private fun applyResult(event: PrepareAnvilEvent, player: Player, result: AnvilResult) {
        event.result = result.item

        AnvilXpUtil.setAnvilResult(event.view, player, result)
    }

}