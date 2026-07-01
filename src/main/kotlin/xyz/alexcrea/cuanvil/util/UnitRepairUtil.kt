package xyz.alexcrea.cuanvil.util

import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.util.MaterialUtil.customType

object UnitRepairUtil {

    // Default value for user set default unit repair %
    private const val DEFAULT_DEFAULT_UNIT_REPAIR = 0.25

    // Path to user default unit repair value
    private const val UNIT_REPAIR_DEFAULT_PATH = "default_repair_amount"

    /**
     * Get the % of repair by unit [other] will do to this [ItemStack].
     * null if can't unit repaired by [other]
     */
    fun ItemStack.getRepair(
        other: ItemStack?
    ): Double? {
        if (other == null) return null
        val config = ConfigHolder.UNIT_REPAIR_HOLDER.config

        val material = other.customType
        val selfType = this.customType

        var result = checkSection(config, material.toString(), selfType)
        if (result != null) return result

        result = checkSection(config, material.key, selfType)
        if (result != null) return result

        // Get default
        val userDefault = config.getDouble(UNIT_REPAIR_DEFAULT_PATH, DEFAULT_DEFAULT_UNIT_REPAIR)
        if (userDefault <= 0)
            return DEFAULT_DEFAULT_UNIT_REPAIR
        return userDefault
    }

    fun checkSection(
        config: FileConfiguration,
        path: String,
        material: NamespacedKey
    ): Double? {
        val section = config.getConfigurationSection(path) ?: return null

        if (section.isDouble(material.toString()))
            return section.getDouble(material.toString())
        if (section.isDouble(material.key))
            return section.getDouble(material.key)

        return null
    }

}