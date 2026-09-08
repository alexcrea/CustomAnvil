package xyz.alexcrea.cuanvil.dependency.plugins

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener

open class GenericPluginDependency(protected open val plugin: Plugin, private val testPrepare: Boolean = true) {

    protected val preAnvil = ArrayList<RegisteredListener>()
    protected val postAnvil = ArrayList<RegisteredListener>()

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

    open fun fillPreAnvil(preAnvil: ArrayList<RegisteredListener>) {
        // get PreAnvil and PostAnvil listeners
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {

            if (registeredListener.plugin != plugin) continue
            preAnvil.add(registeredListener)
        }
    }

    protected open fun fillPostAnvil(
        postAnvil: ArrayList<RegisteredListener>,
        preAnvil: ArrayList<RegisteredListener>
    ) {

    }

    open fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        if (!testPrepare) return false

        val previousResult = event.result
        event.result = null

        for (registeredListener in preAnvil) {
            // We do not want error from another plugin to be our fault
            try {
                registeredListener.callEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (event.result != null) return true
        }

        event.result = previousResult;
        return false
    }

    open fun testAnvilResult(event: InventoryClickEvent): Boolean {
        if (!testPrepare) return false

        for (registeredListener in postAnvil) {
            // We do not want error from another plugin to be our fault
            try {
                registeredListener.callEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (event.inventory.getItem(2) == null) return true
        }

        return false
    }


}
