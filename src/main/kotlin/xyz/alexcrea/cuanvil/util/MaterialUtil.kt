package xyz.alexcrea.cuanvil.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.plugins.EcoItemDependencyUtil

object MaterialUtil {

    val ItemStack?.isAir: Boolean
        get() {
            return this == null || this.type.isAir || this.amount == 0
        }

    val NamespacedKey?.isAir: Boolean
        get() {
            return Material.AIR.key == this
        }

    private val HasEcoItem = Bukkit.getPluginManager().isPluginEnabled("EcoItems")

    val ItemStack.customType: NamespacedKey
        get() {
            if(HasEcoItem) {
                val result = EcoItemDependencyUtil.ecoItemNamespace(this)
                if(result != null) return result
            }

            val itemAdder = DependencyManager.itemsAdderCompatibility
            if(itemAdder != null) {
                val result = itemAdder.getKey(this)
                if (result != null) return result
            }

            return this.type.key
        }

    private fun bukkitMaterialFromKey(key: NamespacedKey): Material? {
        //TODO on paper only transition Registry.MATERIAL.get(key)
        return Material.matchMaterial(key.toString())
    }

    fun getMatFromKey(key: NamespacedKey): Material? {
        if(HasEcoItem) {
            val result = EcoItemDependencyUtil.ecoItemMaterialFromKey(key)
            if(result != null) return result
        }

        val itemAdder = DependencyManager.itemsAdderCompatibility
        if(itemAdder != null) {
            val result = itemAdder.fromKey(key)
            if (result != null) return result.type
        }

        return bukkitMaterialFromKey(key)
    }

    fun itemFromKey(key: NamespacedKey): ItemStack {
        if(HasEcoItem) {
            val result = EcoItemDependencyUtil.newEcoItemstack(key)
            if(result != null) return result
        }

        val itemAdder = DependencyManager.itemsAdderCompatibility
        if(itemAdder != null) {
            val result = itemAdder.fromKey(key)
            if (result != null) return result
        }

        return ItemStack(bukkitMaterialFromKey(key)!!)
    }

    fun materialExist(key: NamespacedKey): Boolean {
        return getMatFromKey(key) != null
    }

    fun getMaterialCount(): Int {
        var count = Material.entries.size
        if(HasEcoItem) {
            count += EcoItemDependencyUtil.getItems().size
        }

        val itemAdder = DependencyManager.itemsAdderCompatibility
        if(itemAdder != null) {
            count += itemAdder.idsCount().size
        }

        return count
    }

    fun getMaterials(): MutableList<NamespacedKey> {
        val all = ArrayList(Material.entries.map { it.key })
        if(HasEcoItem) {
            all.addAll(EcoItemDependencyUtil.getItems())
        }

        val itemAdder = DependencyManager.itemsAdderCompatibility
        if(itemAdder != null) {
            all.addAll(itemAdder.idsCount().map { NamespacedKey.fromString(it) })
        }

        return all
    }

}