package xyz.alexcrea.cuanvil.util

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.util.ItemTypeUtil.itemType

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

        val otherKey = other.itemType.key

        // Get configuration section if exist
        var section = config.getConfigurationSection(otherKey.toString())
        if (section == null) {
            section = config.getConfigurationSection(otherKey.key)
            if (section == null) return null
        }

        // Get repair amount
        var userDefault = config.getDouble(UNIT_REPAIR_DEFAULT_PATH, DEFAULT_DEFAULT_UNIT_REPAIR)
        if (userDefault <= 0) {
            userDefault = DEFAULT_DEFAULT_UNIT_REPAIR
        }

        return getRepairAmount(this, section, userDefault)
    }

    /**
     * Get the item % repaired by this configuration section of a unit repair.
     * null if not found.
     * If value is set to less than or equal to 0 then it will be set to default
     */
    private fun getRepairAmount(item: ItemStack, section: ConfigurationSection, default: Double): Double? {
        val itemKey = item.itemType.key

        val repairValue = if (section.isDouble(itemKey.toString())) {
            section.getDouble(itemKey.toString())
        } else if (section.isDouble(itemKey.key)) {
            section.getDouble(itemKey.key)
        } else {
            return null
        }

        if (repairValue <= 0)
            return default
        return repairValue
    }

}