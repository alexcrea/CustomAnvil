package xyz.alexcrea.cuanvil.dependency.plugins

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT

class EnchantedBookDependency(plugin: Plugin) : GenericPluginDependency(plugin) {

    override fun testAnvilResult(event: InventoryClickEvent): Boolean {
        val view = event.view

        val current = view.getItem(ANVIL_OUTPUT_SLOT)
        view.setItem(ANVIL_OUTPUT_SLOT, null)
        val fakeEvent = DependencyManager.createFakeEvent(event.view, null)

        if (testPrepareAnvil(fakeEvent)) {
            event.isCancelled = false
            return true
        }

        view.setItem(ANVIL_INPUT_LEFT, current)
        return false
    }

}
