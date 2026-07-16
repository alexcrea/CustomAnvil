package xyz.alexcrea.cuanvil.dependency.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
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

    val isPaper = hasClass("com.destroystokyo.paper.PaperConfig") ||
            hasClass("io.papermc.paper.configuration.Configuration")

    val isFolia = hasClass("io.papermc.paper.threadedregions.RegionizedServer")

    private val legacy_mm = LegacyComponentSerializer.legacySection()

    // Lore
    fun ItemMeta.componentLore(): MutableList<Component> {
        val lore: List<Component>?
        if (isPaper) {
            lore = this.lore()
        } else {
            val legacyLores = this.lore ?: return ArrayList()

            lore = ArrayList(legacyLores.size)
            for (legacyLore in legacyLores) {
                lore.add(legacy_mm.deserialize(legacyLore))
            }
        }

        return lore ?: ArrayList()
    }

    fun ItemMeta.setComponentLore(lore: List<Component?>) {
        if (isPaper) {
            this.lore(lore)
        } else {
            val legacyLore = ArrayList<String?>(lore.size)
            for (component in lore) {
                legacyLore.add(
                    if (component == null) null
                    else legacy_mm.serialize(component)
                )
            }

            this.lore = legacyLore
        }
    }

    // Display name
    private val useCustomName = hasMethod(ItemMeta::class.java, "customName")

    fun ItemMeta.componentDisplayName(): Component? {
        if (useCustomName) {
            if (!this.hasCustomName()) return null
            return this.customName()
        }
        if (!this.hasDisplayName()) return null

        return if (isPaper) {
            this.displayName()
        } else {
            legacy_mm.deserialize(this.displayName)
        }
    }

    fun ItemMeta.setComponentDisplayName(component: Component?, fallback: String? = null) {
        if (useCustomName) {
            this.customName(component)
        } else if (isPaper) {
            this.displayName(component)
        } else {
            if (component == null) {
                this.setDisplayName(null)
                return
            }

            val legacy = fallback ?: legacy_mm.serialize(component)
            this.setDisplayName(legacy)
        }
    }

}
