package xyz.alexcrea.cuanvil.dependency.plugins

import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT

@Suppress("UnstableApiUsage")
class EnchantedBookDependency(plugin: Plugin) : GenericPluginDependency(plugin) {

    override fun testAnvilResult(event: InventoryClickEvent): Boolean {
        val view = event.view as? AnvilView ?: return false
        val inv = event.inventory

        //TODO use view here (v2)
        val current = inv.getItem(ANVIL_OUTPUT_SLOT)
        inv.setItem(ANVIL_OUTPUT_SLOT, null)
        val fakeEvent = DependencyManager.createFakeEvent(view, null)

        if (testPrepareAnvil(fakeEvent)) {
            event.result = Event.Result.DEFAULT

            inv.setItem(ANVIL_INPUT_LEFT, fakeEvent.result)
            return true
        }

        inv.setItem(ANVIL_INPUT_LEFT, current)
        return false
    }

}
