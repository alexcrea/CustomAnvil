package xyz.alexcrea.cuanvil.dependency.plugins

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener

open class GenericPluginDependency(protected val plugin: Plugin) {

    private val preAnvil = ArrayList<RegisteredListener>()
    private val postAnvil = ArrayList<RegisteredListener>()

    open fun redirectListeners() {
        fillPreAnvil(preAnvil)
        fillPostAnvil(postAnvil, preAnvil)

        // get required PrepareAnvilEvent listener
        for (listener in preAnvil) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }

        for (listener in postAnvil) {
            InventoryClickEvent.getHandlerList().unregister(listener)
        }
    }

    open fun fillPreAnvil(preAnvil: ArrayList<RegisteredListener>){
        // get PreAnvil and PostAnvil listeners
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {

            if (registeredListener.plugin != plugin) continue
            preAnvil.add(registeredListener)
        }
    }

    protected open fun fillPostAnvil(
        postAnvil: ArrayList<RegisteredListener>,
        preAnvil: ArrayList<RegisteredListener>) {

    }

    open fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        val previousResult = event.result
        event.result = null

        for (registeredListener in preAnvil) {
            registeredListener.callEvent(event)
            if (event.result != null) return true
        }

        event.result = previousResult;
        return false
    }

    open fun testAnvilResult(event: InventoryClickEvent): Boolean {
        for (registeredListener in postAnvil) {
            registeredListener.callEvent(event)

            if (event.inventory.getItem(2) == null) return true
        }

        return false
    }


}
