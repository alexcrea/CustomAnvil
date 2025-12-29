package xyz.alexcrea.cuanvil.dependency.util

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

// Mostly made for paper, spigot and folia support
@Suppress("DEPRECATION")
object PlatformUtil {

    private fun hasClass(className: String): Boolean {
        try {
            Class.forName(className)
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    private fun hasMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Boolean {
        try {
            clazz.getDeclaredMethod(name, *parameterTypes)
            return true
        } catch (_: NoSuchMethodException) {
            return false
        }
    }


    val isFolia = hasClass("io.papermc.paper.threadedregions.RegionizedServer")

    val isMockbukkit = hasClass("org.mockbukkit.mockbukkit.exception.UnimplementedOperationException")

    // Lore
    fun ItemMeta.componentLore(): MutableList<Component> {
        val lore = this.lore()
        return lore ?: ArrayList()
    }

    fun ItemMeta.setComponentLore(lore: List<Component?>) {
        this.lore(lore)
    }

    // Display name
    private val useCustomName = hasMethod(ItemStack::class.java, "customName")

    fun ItemMeta.componentDisplayName(): Component? {
        if(useCustomName){
            if(!this.hasCustomName()) return null //TODO check if I can use customName
            return this.customName()
        }else {
            if(!this.hasDisplayName()) return null
            return this.displayName()
        }
    }

    fun ItemMeta.setComponentDisplayName(component: Component?) {
        if(useCustomName){
            this.customName(component)
        }else {
            this.displayName(component)
        }
    }

}
