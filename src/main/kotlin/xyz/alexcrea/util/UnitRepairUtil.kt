package xyz.alexcrea.util

import io.delilaheve.UnsafeEnchants
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

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
        if(other == null) return null
        val config = UnsafeEnchants.unitRepairConfig
        // Get configuration section if exist
        val otherName = other.type.name.uppercase()
        var section = config.getConfigurationSection(otherName)
        if(section == null){
            section = config.getConfigurationSection(otherName.lowercase())
            if(section == null) {
                return null
            }
        }
        // Get repair amount
        var userDefault = config.getDouble(UNIT_REPAIR_DEFAULT_PATH,DEFAULT_DEFAULT_UNIT_REPAIR)
        if(userDefault <= 0){
            userDefault = DEFAULT_DEFAULT_UNIT_REPAIR
        }

        return getRepairAmount(this,section,userDefault)
    }

    /**
     * Get the item % repaired by this configuration section of a unit repair.
     * null if not found.
     * If value is set to less than or equal to 0 then it will be set to default
     */
    private fun getRepairAmount(item: ItemStack, section: ConfigurationSection, default: Double): Double?{
        val itemName = item.type.name.uppercase()
        val repairValue = if(section.isDouble(itemName)){
            section.getDouble(itemName)
        }else if(section.isDouble(itemName.lowercase())){
            section.getDouble(itemName.lowercase())
        }else{
            return null
        }
        if(repairValue <= 0)
            return default
        return repairValue
    }

}