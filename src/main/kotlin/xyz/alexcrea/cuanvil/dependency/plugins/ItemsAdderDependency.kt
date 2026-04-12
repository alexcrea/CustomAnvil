package xyz.alexcrea.cuanvil.dependency.plugins

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.ItemsAdder
import io.delilaheve.CustomAnvil
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class ItemsAdderDependency(plugin: Plugin) : GenericPluginDependency(plugin) {
    var isLoaded: Boolean = false
        get() {
            if (field) return true

            // We can't be sure the event is registered before being triggered so we need to use this function
            field = ItemsAdder.areItemsLoaded()
            return field
        }

    fun tryClone(item: ItemStack): ItemStack? {
        if(!isLoaded) return null
        val customItem = CustomStack.byItemStack(item) ?: return null

        CustomAnvil.instance.logger.warning("testing equal: ${customItem.itemStack == item}")
        return customItem.itemStack
    }

}
