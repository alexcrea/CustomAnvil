package xyz.alexcrea.cuanvil.util

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

object ItemTypeUtil {

    /**
     * Get item type by its name
     * @return the item type or null if absent or malformed key
     */
    fun getItemType(name: String): ItemType? {
        val key = NamespacedKey.fromString(name.lowercase())
        if (key == null) return null;

        return Registry.ITEM.get(key)
    }

    /**
     * Get item type by its name
     * @return the item type or null if absent or malformed key
     */
    fun getItemTypeExact(name: String): ItemType? {
        val key = NamespacedKey.fromString(name)
        if (key == null) return null;

        return Registry.ITEM.get(key)
    }

    fun ItemType.name(): String {
        return this.key.key
    }

    val ItemStack.itemType: ItemType
        get() {
            // we assume material of an item stack is a material of an item...
            return this.type.asItemType()!!
        }


}