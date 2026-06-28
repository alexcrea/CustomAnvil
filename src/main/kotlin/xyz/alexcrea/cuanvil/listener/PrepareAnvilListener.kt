package xyz.alexcrea.cuanvil.listener

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil
import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.canMergeWith
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
import xyz.alexcrea.cuanvil.anvil.AnvilCost
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

        val player = InventoryViewUtil.getInstance().getPlayer(view)
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
            AnvilXpUtil.setAnvilInvCost(inventory, view, player, AnvilCost(event.inventory.repairCost))
            return
        }

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        val result = getResult(view, inventory, player, first, second)
        applyResult(event, player, result)
    }

    fun getResult(
        view: InventoryView, //TODO use anvil view
        inventory: AnvilInventory,
        player: Player,
        first: ItemStack?, second: ItemStack?) : AnvilResult
    {
        if(first == null)
            return AnvilResult.EMPTY

        // Test custom recipe
        var result: AnvilResult = testCustomRecipe(view, inventory, player, first, second)
        if (!result.isEmpty())
            return result

        // Test rename lonely item
        val shouldTryRename = second.isAir
        CustomAnvil.verboseLog("checking air in main logic: $shouldTryRename")
        if (shouldTryRename)
            return doRenaming(view, inventory, player, first)

        // Test for merge
        if (first.canMergeWith(second!!))
            return doMerge(view, inventory, player, first, second)

        // Test for unit repair
        result = testUnitRepair(view, inventory, player, first, second)
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
        if(!ConfigOptions.canUseDialogRename(player)) return

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

        AnvilXpUtil.setAnvilResult(event.inventory, event.view, player, result)
    }

}