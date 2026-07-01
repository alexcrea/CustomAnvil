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
    public const val UNIT_REPAIR_DEFAULT_PATH = "default_repair_amount"

    /**
     * Get the % of repair by unit [other] will do to this [ItemStack].
     * null if can't unit repaired by [other]
     */
    fun ItemStack.getRepair(
        other: ItemStack?
    ): Double? {
        if (other == null) return null
        val config = ConfigHolder.UNIT_REPAIR_HOLDER.config

        val result = findRepairValue(this, other, config) ?: return null

        if(result > 0) return result

        // Get default
        val userDefault = config.getDouble(UNIT_REPAIR_DEFAULT_PATH, DEFAULT_DEFAULT_UNIT_REPAIR)
        if (userDefault <= 0)
            return DEFAULT_DEFAULT_UNIT_REPAIR
        return userDefault
    }

    private fun findRepairValue(
        self: ItemStack,
        other: ItemStack,
        config: FileConfiguration
    ): Double? {
        val material = other.customType
        val selfType = self.customType

        val result = checkSection(config, material.toString(), selfType)
        if (result != null) return result

        return checkSection(config, material.key, selfType)
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