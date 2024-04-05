package xyz.alexcrea.cuanvil.recipe

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant

class AnvilCustomRecipe(
    val name: String,
    var exactCount: Boolean,
    var exactLeft: Boolean,
    var exactRight: Boolean,

    var xpCostPerCraft: Int,

    var leftItem: ItemStack?,
    var rightItem: ItemStack?,
    var resultItem: ItemStack?,
) {

    // Static config name
    companion object {
        const val EXACT_COUNT_CONFIG = "exact_count"
        const val EXACT_LEFT_CONFIG = "exact_left"
        const val EXACT_RIGHT_CONFIG = "exact_right"

        const val XP_COST_CONFIG = "xp_cost"

        const val LEFT_ITEM_CONFIG = "left_item"
        const val RIGHT_ITEM_CONFIG = "right_item"
        const val RESULT_ITEM_CONFIG = "result_item"

        fun getFromConfig(name: String, configSection: ConfigurationSection?): AnvilCustomRecipe? {
            if(configSection == null) return null;
            return AnvilCustomRecipe(
                name,
                configSection.getBoolean(EXACT_COUNT_CONFIG, true),
                configSection.getBoolean(EXACT_LEFT_CONFIG, true),
                configSection.getBoolean(EXACT_RIGHT_CONFIG, true),

                configSection.getInt(XP_COST_CONFIG, 10),

                configSection.getItemStack(LEFT_ITEM_CONFIG, null),
                configSection.getItemStack(RIGHT_ITEM_CONFIG, null),
                configSection.getItemStack(RESULT_ITEM_CONFIG, null),

            )
        }

        fun getFromConfig(name: String): AnvilCustomRecipe? {
            return getFromConfig(name, ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getConfigurationSection(name))
        }
    }

    fun validate(): Boolean {
        return (leftItem != null) && !(leftItem!!.type.isAir) && (leftItem!!.amount > 0) &&
                //(rightItem != null) && !(rightItem!!.type.isAir) && (rightItem!!.amount > 0) &&
                ((rightItem == null) || (!(rightItem!!.type.isAir) && (rightItem!!.amount > 0))) &&
                (resultItem != null) && !(resultItem!!.type.isAir) && (resultItem!!.amount > 0)

    }

    fun saveToFile(){
        val fileConfig = ConfigHolder.CUSTOM_RECIPE_HOLDER.config

        fileConfig.set("$name.$EXACT_COUNT_CONFIG", exactCount)
        fileConfig.set("$name.$EXACT_LEFT_CONFIG", exactLeft)
        fileConfig.set("$name.$EXACT_RIGHT_CONFIG", exactRight)

        fileConfig.set("$name.$XP_COST_CONFIG", xpCostPerCraft)

        fileConfig.set("$name.$LEFT_ITEM_CONFIG", leftItem)
        fileConfig.set("$name.$RIGHT_ITEM_CONFIG", rightItem)
        fileConfig.set("$name.$RESULT_ITEM_CONFIG", resultItem)


        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
    }



}
