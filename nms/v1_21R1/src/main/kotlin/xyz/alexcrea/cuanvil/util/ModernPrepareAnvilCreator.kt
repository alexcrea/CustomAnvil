package xyz.alexcrea.cuanvil.util

import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView

object ModernPrepareAnvilCreator {
    fun createPrepareAnvil(view: InventoryView, item: ItemStack?): PrepareAnvilEvent {
        return PrepareAnvilEvent(view as AnvilView, item)
    }
}
