package xyz.alexcrea.cuanvil.recipe

import io.delilaheve.CustomAnvil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant

class AnvilCustomRecipe(
    private val name: String,
    var exactCount: Boolean,
    //var exactLeft: Boolean,
    //var exactRight: Boolean,

    var xpCostPerCraft: Int,

    var leftItem: ItemStack?,
    var rightItem: ItemStack?,
    var resultItem: ItemStack?,
) {

    // Static config name
    companion object {
        const val EXACT_COUNT_CONFIG = "exact_count"
        //const val EXACT_LEFT_CONFIG = "exact_left"
        //const val EXACT_RIGHT_CONFIG = "exact_right"

        const val XP_COST_CONFIG = "xp_cost"

        const val LEFT_ITEM_CONFIG = "left_item"
        const val RIGHT_ITEM_CONFIG = "right_item"
        const val RESULT_ITEM_CONFIG = "result_item"


        val DEFAULT_EXACT_COUNT_CONFIG = true
        //val DEFAULT_EXACT_LEFT_CONFIG = true
        //val DEFAULT_EXACT_RIGHT_CONFIG = true

        val DEFAULT_XP_COST_CONFIG = 1

        val DEFAULT_LEFT_ITEM_CONFIG: ItemStack? = null
        val DEFAULT_RIGHT_ITEM_CONFIG: ItemStack? = null
        val DEFAULT_RESULT_ITEM_CONFIG: ItemStack? = null;

        val XP_COST_CONFIG_RANGE = 0..255

        fun getFromConfig(name: String, configSection: ConfigurationSection?): AnvilCustomRecipe? {
            if(configSection == null) return null;
            return AnvilCustomRecipe(
                name,
                configSection.getBoolean(EXACT_COUNT_CONFIG, DEFAULT_EXACT_COUNT_CONFIG),
                //configSection.getBoolean(EXACT_LEFT_CONFIG, true),
                //configSection.getBoolean(EXACT_RIGHT_CONFIG, true),

                configSection.getInt(XP_COST_CONFIG, DEFAULT_XP_COST_CONFIG),

                configSection.getItemStack(LEFT_ITEM_CONFIG, DEFAULT_LEFT_ITEM_CONFIG),
                configSection.getItemStack(RIGHT_ITEM_CONFIG, DEFAULT_RIGHT_ITEM_CONFIG),
                configSection.getItemStack(RESULT_ITEM_CONFIG, DEFAULT_RESULT_ITEM_CONFIG),

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
        //fileConfig.set("$name.$EXACT_LEFT_CONFIG", exactLeft)
        //fileConfig.set("$name.$EXACT_RIGHT_CONFIG", exactRight)

        fileConfig.set("$name.$XP_COST_CONFIG", xpCostPerCraft)

        fileConfig.set("$name.$LEFT_ITEM_CONFIG", leftItem)
        fileConfig.set("$name.$RIGHT_ITEM_CONFIG", rightItem)
        fileConfig.set("$name.$RESULT_ITEM_CONFIG", resultItem)


        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            ConfigHolder.CUSTOM_RECIPE_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
    }

    fun updateFromFile(){
        this.exactCount = ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getBoolean(
            "$name.$EXACT_COUNT_CONFIG",
            DEFAULT_EXACT_COUNT_CONFIG
        )

        this.xpCostPerCraft = ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getInt(
            "$name.$XP_COST_CONFIG",
            DEFAULT_XP_COST_CONFIG
        )

        // Update items
        val leftItem = ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getItemStack(
            "$name.$LEFT_ITEM_CONFIG",
            DEFAULT_LEFT_ITEM_CONFIG
        )

        this.rightItem = ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getItemStack(
            "$name.$RIGHT_ITEM_CONFIG",
            DEFAULT_RIGHT_ITEM_CONFIG
        )

        this.resultItem = ConfigHolder.CUSTOM_RECIPE_HOLDER.config.getItemStack(
            "$name.$RESULT_ITEM_CONFIG",
            DEFAULT_RESULT_ITEM_CONFIG
        )

        // Update material map
        ConfigHolder.CUSTOM_RECIPE_HOLDER.recipeManager.cleanSetLeftItem(this, leftItem)

    }

    fun testItem(item1: ItemStack, item2: ItemStack?): Boolean {
        CustomAnvil.verboseLog("Testing $name $leftItem")
        // We assume this function can be call only if leftItem != null

        // Test is valid
        if(!validate()) return false

        val leftSimilar = leftItem!!.isSimilar(item1)
        CustomAnvil.verboseLog("Validated test !")

        // test of left item
        if(!leftSimilar) return false // Test similar
        if(exactCount){
            if((leftItem!!.amount != item1.amount)) return false // test exact amount
        }else if(item1.amount < leftItem!!.amount) return false // test if it has at least the amount we ask

        CustomAnvil.verboseLog("Left item passed !")

        // we don't know if right item can be
        if(rightItem == null){ // null test
            if(item2 != null) return false
        }else {
            val rightSimilar = rightItem!!.isSimilar(item2)
            CustomAnvil.verboseLog("Right similar: $rightSimilar")
            if(!rightSimilar) return false // test if similar when not null

            if(exactCount) {
                if (rightItem!!.amount != item2!!.amount) return false // test exact amount
            }else if(item2!!.amount < rightItem!!.amount) return false // test if it has at least the amount we ask
        }

        CustomAnvil.verboseLog("Right item passed !")

        return true
    }

    override fun toString(): String {
        return name
    }


}
