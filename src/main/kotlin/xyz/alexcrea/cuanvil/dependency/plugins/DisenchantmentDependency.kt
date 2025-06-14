package xyz.alexcrea.cuanvil.dependency.plugins

import com.jankominek.disenchantment.Disenchantment
import com.jankominek.disenchantment.events.DisenchantClickEvent
import com.jankominek.disenchantment.events.DisenchantEvent
import com.jankominek.disenchantment.events.ShatterClickEvent
import com.jankominek.disenchantment.events.ShatterEvent
import io.delilaheve.CustomAnvil
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.util.AnvilXpUtil


@Suppress("unstableApiUsage")
class DisenchantmentDependency {

    init {
        CustomAnvil.instance.logger.info("Disenchantment Detected !")
    }

    fun redirectListeners() {
        PrepareAnvilEvent.getHandlerList().unregister(Disenchantment.plugin)
        InventoryClickEvent.getHandlerList().unregister(Disenchantment.plugin)
    }

    fun testPrepareAnvil(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        val previousResult = event.result
        event.result = null

        // Test if event change the result
        DisenchantEvent.onEvent(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil item extract bypass.")
            AnvilXpUtil.setAnvilInvXp(event.view, player)
            return true
        }

        ShatterEvent.onEvent(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil split enchant bypass.")
            AnvilXpUtil.setAnvilInvXp(event.view, player)
            return true
        }

        event.result = previousResult
        return false
    }

    fun testAnvilResult(event: InventoryClickEvent, view: AnvilView): Boolean {
        val previousResultSlot = view.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)?.clone()

        // Test event if change the result
        DisenchantClickEvent.onEvent(event)
        if (!testAnvilInventoryChange(view, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click item extract bypass.")
            return true
        }

        ShatterClickEvent.onEvent(event)
        if (!testAnvilInventoryChange(view, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click split enchant bypass.")
            return true
        }

        return false
    }

    private fun testAnvilInventoryChange(view: AnvilView, previous: ItemStack?): Boolean {
        val currentResult = view.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)

        return currentResult == previous
    }

}
