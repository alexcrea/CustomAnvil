package xyz.alexcrea.cuanvil.dependency.plugins

import com.willfp.ecoitems.items.EcoItem
import com.willfp.ecoitems.items.EcoItems
import com.willfp.ecoitems.items.ecoItem
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

object EcoItemDependencyUtil {

    fun ecoItemNamespace(item: ItemStack): NamespacedKey? {
        val ecoItem = item.ecoItem ?: return null

        return ecoItem.id
    }

    fun ecoItemFromKey(key: NamespacedKey): EcoItem? {
        return EcoItems.getByID(key.toString())
    }

    fun ecoItemMaterialFromKey(key: NamespacedKey): Material? {
        val ecoItem = ecoItemFromKey(key) ?: return null

        return ecoItem.itemStack.type
    }

    fun newEcoItemstack(key: NamespacedKey): ItemStack? {
        val ecoItem = ecoItemFromKey(key) ?: return null

        return ecoItem.itemStack
    }

    fun getItems(): List<NamespacedKey> {
        return EcoItems.values().map { item -> item.id }
    }

}