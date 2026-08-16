package xyz.alexcrea.cuanvil.dependency.plugins

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.ItemsAdder
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@Suppress("DEPRECATION")
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

        return CustomStack.getInstance(customItem.namespacedID)?.itemStack
    }

    fun fromKey(key: NamespacedKey): ItemStack? {
        if(!isLoaded) return null
        return CustomStack.getInstance(key.toString())?.itemStack
    }

    fun getKey(item: ItemStack) : NamespacedKey? {
        if(!isLoaded) return null
        val customItem = CustomStack.byItemStack(item) ?: return null

        return NamespacedKey.fromString(customItem.namespacedID)
    }

    fun idsCount(): Set<String> {
        return CustomStack.getNamespacedIdsInRegistry()
    }

}
