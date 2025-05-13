package xyz.alexcrea.cuanvil.dependency.plugins

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener

abstract class GenericPluginDependency(private val plugin: Plugin) {

    protected val preAnvil = ArrayList<RegisteredListener>()
    protected val postAnvil = ArrayList<RegisteredListener>()

    open fun redirectListeners() {
        // get PreAnvil and PostAnvil listeners
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {

            if (registeredListener.plugin != plugin) continue
            preAnvil.add(registeredListener)
        }

        postAnvil.addAll(postAnvilEvents())

        // get required PrepareAnvilEvent listener
        for (listener in preAnvil) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }

        for (listener in postAnvil) {
            InventoryClickEvent.getHandlerList().unregister(listener)
        }

    }

    protected abstract fun postAnvilEvents(): Collection<RegisteredListener>

    open fun testPrepareAnvil(event: PrepareAnvilEvent) {
        for (registeredListener in preAnvil) {
            registeredListener.callEvent(event)
        }
    }

    open fun testAnvilResult(event: InventoryClickEvent): Boolean {
        for (registeredListener in postAnvil) {
            registeredListener.callEvent(event)
        }

        return event.inventory.getItem(2) == null
    }


}
