package xyz.alexcrea.cuanvil.dependency.plugins

import com.jankominek.disenchantment.Disenchantment
import com.jankominek.disenchantment.events.DisenchantClickEvent
import com.jankominek.disenchantment.events.DisenchantEvent
import com.jankominek.disenchantment.events.ShatterClickEvent
import com.jankominek.disenchantment.events.ShatterEvent
import com.jankominek.disenchantment.listeners.DisenchantClickListener
import com.jankominek.disenchantment.listeners.ShatterClickListener
import io.delilaheve.CustomAnvil
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import xyz.alexcrea.cuanvil.anvil.AnvilCost
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.util.MetricsUtil.trackError
import xyz.alexcrea.cuanvil.util.anvil.AnvilXpUtil
import java.util.logging.Level
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
class DisenchantmentDependency {

    init {
        CustomAnvil.instance.logger.info("Disenchantment Detected !")
    }

    fun redirectListeners() {
        PrepareAnvilEvent.getHandlerList().unregister(Disenchantment.plugin)

        // unregister only the feature click event and not all
        // This is to avoid the disenchantment gui breaking
        try {
            unregisterStaticDisenchantmentListener(ShatterClickListener::class)
            unregisterStaticDisenchantmentListener(DisenchantClickListener::class)
        } catch (e: Exception) {
            CustomAnvil.instance.logger.log(
                Level.SEVERE, "Could not initialize disenchantment support" +
                        "please report this bug to the developer", e
            )
            trackError(e)
        }
    }

    private fun unregisterStaticDisenchantmentListener(clazz: KClass<*>) {
        val field = clazz.java.getDeclaredField("listener")
        field.isAccessible = true
        val listener: Listener = field.get(null) as Listener
        InventoryClickEvent.getHandlerList().unregister(listener)
    }

    fun testPrepareAnvil(event: PrepareAnvilEvent, player: Player): Boolean {
        val previousResult = event.result
        event.result = null

        // Test if event change the result
        DisenchantEvent.onEvent(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil item extract bypass.")
            AnvilXpUtil.setAnvilInvCost(event.view, player, AnvilCost(event.view.repairCost))
            return true
        }

        ShatterEvent.onEvent(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil split enchant bypass.")
            AnvilXpUtil.setAnvilInvCost(event.view, player, AnvilCost(event.view.repairCost))
            return true
        }

        event.result = previousResult
        return false
    }

    fun testAnvilResult(event: InventoryClickEvent, view: AnvilView): Boolean {
        val previousResultSlot = view.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)?.clone()

        // Test event if change the result
        DisenchantClickEvent.onEvent(event)
        if (!testAnvilChange(view, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click item extract bypass.")
            return true
        }

        ShatterClickEvent.onEvent(event)
        if (!testAnvilChange(view, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click split enchant bypass.")
            return true
        }

        return false
    }

    private fun testAnvilChange(view: AnvilView, previous: ItemStack?): Boolean {
        val currentResult = view.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)

        return currentResult == previous
    }

}
