package xyz.alexcrea.cuanvil.recipe

import io.delilaheve.CustomAnvil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.anvil.AnvilUseType
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir
import xyz.alexcrea.cuanvil.util.anvil.AnvilXpUtil

//TODO use equivalent of record #130 part 1.4
class AnvilCustomRecipe(
    val name: String,
    var exactCount: Boolean,
    //var exactLeft: Boolean,
    //var exactRight: Boolean,

    var levelCostPerCraft: Int,

    var XpCostPerCraft: Int,
    var removeExactLinearXp: Boolean,

    var leftItem: ItemStack?,
    var rightItem: ItemStack?,
    var resultItem: ItemStack?,
) {

    // Static config name
    companion object {
        const val EXACT_COUNT_CONFIG = "exact_count"
        //const val EXACT_LEFT_CONFIG = "exact_left"
        //const val EXACT_RIGHT_CONFIG = "exact_right"

        const val XP_LEVEL_COST_CONFIG = "xp_cost"
        const val LINEAR_XP_COST_CONFIG = "linear_xp_cost"
        const val REMOVE_EXACT_XP_CONFIG = "remove_exact_linear_xp"

        const val LEFT_ITEM_CONFIG = "left_item"
        const val RIGHT_ITEM_CONFIG = "right_item"
        const val RESULT_ITEM_CONFIG = "result_item"


        const val DEFAULT_EXACT_COUNT_CONFIG = true
        //val DEFAULT_EXACT_LEFT_CONFIG = true
        //val DEFAULT_EXACT_RIGHT_CONFIG = true

        const val DEFAULT_XP_LEVEL_COST_CONFIG = 1
        const val DEFAULT_LINEAR_XP_COST_CONFIG = 0
        const val DEFAULT_REMOVE_EXACT_XP_CONFIG = false

        val DEFAULT_LEFT_ITEM_CONFIG: ItemStack? = null
        val DEFAULT_RIGHT_ITEM_CONFIG: ItemStack? = null
        val DEFAULT_RESULT_ITEM_CONFIG: ItemStack? = null

        val XP_COST_CONFIG_RANGE = 0..255

        fun getFromConfig(name: String, configSection: ConfigurationSection?): AnvilCustomRecipe? {
            if(configSection == null) return null
            return AnvilCustomRecipe(
                name,
                configSection.getBoolean(EXACT_COUNT_CONFIG, DEFAULT_EXACT_COUNT_CONFIG),
                //configSection.getBoolean(EXACT_LEFT_CONFIG, true),
                //configSection.getBoolean(EXACT_RIGHT_CONFIG, true),

                configSection.getInt(XP_LEVEL_COST_CONFIG, DEFAULT_XP_LEVEL_COST_CONFIG),
                configSection.getInt(LINEAR_XP_COST_CONFIG, DEFAULT_LINEAR_XP_COST_CONFIG),
                configSection.getBoolean(REMOVE_EXACT_XP_CONFIG, DEFAULT_REMOVE_EXACT_XP_CONFIG),


                configSection.getItemStack(LEFT_ITEM_CONFIG, DEFAULT_LEFT_ITEM_CONFIG),

                configSection.getItemStack(RIGHT_ITEM_CONFIG, DEFAULT_RIGHT_ITEM_CONFIG),
                configSection.getItemStack(RESULT_ITEM_CONFIG, DEFAULT_RESULT_ITEM_CONFIG),

                )
        }

        fun getFromConfig(name: String): AnvilCustomRecipe? {
            ConfigHolder.CUSTOM_RECIPE.read.use {lock ->
                val section = lock.get().config.getConfigurationSection(name)
                return getFromConfig(name, section)
            }
        }
    }

    fun validate(): Boolean {
        return !leftItem.isAir &&
               (rightItem == null || !resultItem.isAir) &&
               !resultItem.isAir
    }

    private fun saveToFile(config: ConfigHolder.CustomAnvilCraftHolder, writeFile: Boolean, doBackup: Boolean) {
        val fileConfig = config.config

        fileConfig["$name.$EXACT_COUNT_CONFIG"] = exactCount
        //fileConfig.set("$name.$EXACT_LEFT_CONFIG", exactLeft)
        //fileConfig.set("$name.$EXACT_RIGHT_CONFIG", exactRight)

        fileConfig["$name.$XP_LEVEL_COST_CONFIG"] = levelCostPerCraft
        fileConfig["$name.$LINEAR_XP_COST_CONFIG"] = XpCostPerCraft
        fileConfig["$name.$REMOVE_EXACT_XP_CONFIG"] = removeExactLinearXp

        fileConfig["$name.$LEFT_ITEM_CONFIG"] = leftItem
        fileConfig["$name.$RIGHT_ITEM_CONFIG"] = rightItem
        fileConfig["$name.$RESULT_ITEM_CONFIG"] = resultItem

        if(writeFile) {
            config.saveToDisk(doBackup)
        }
    }

    fun saveToFile(writeFile: Boolean, doBackup: Boolean) {
        ConfigHolder.CUSTOM_RECIPE.write.use {lock ->
            saveToFile(lock.get(), writeFile, doBackup)
        }
    }

    @Deprecated("Should use saveToFile(Boolean, Boolean) instead") //TODO determine when an where to save/do backup and remove use of variable like TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE
    fun saveToFile() {
        saveToFile(
            GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE,
            GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE
        )
    }

    private fun updateFromFile(recipes: ConfigHolder.CustomAnvilCraftHolder) {
        val config = recipes.config

        config.getBoolean(
            "$name.$EXACT_COUNT_CONFIG",
            DEFAULT_EXACT_COUNT_CONFIG
        )
        config.getInt(
            "$name.$XP_LEVEL_COST_CONFIG",
            DEFAULT_XP_LEVEL_COST_CONFIG
        )
        config.getInt(
            "$name.$LINEAR_XP_COST_CONFIG",
            DEFAULT_LINEAR_XP_COST_CONFIG
        )
        config.getBoolean(
            "$name.$REMOVE_EXACT_XP_CONFIG",
            DEFAULT_REMOVE_EXACT_XP_CONFIG
        )
        config.getItemStack(
            "$name.$LEFT_ITEM_CONFIG",
            DEFAULT_LEFT_ITEM_CONFIG
        )
        config.getItemStack(
            "$name.$RIGHT_ITEM_CONFIG",
            DEFAULT_RIGHT_ITEM_CONFIG
        )
        config.getItemStack(
            "$name.$RESULT_ITEM_CONFIG",
            DEFAULT_RESULT_ITEM_CONFIG
        )

        // Update material map
        recipes.recipeManager.cleanSetLeftItem(this, leftItem)
    }

    fun updateFromFile() {
        ConfigHolder.CUSTOM_RECIPE.read.use {lock ->
            updateFromFile(lock.get())
        }
    }

    fun testItem(item1: ItemStack, item2: ItemStack?): Boolean {
        CustomAnvil.verboseLog("Testing $name $leftItem")
        // We assume this function can be call only if leftItem != null

        // Test if valid
        if(!validate()) return false

        val leftSimilar = leftItem!!.isSimilar(item1)
        CustomAnvil.verboseLog("Validated test !")

        // test of left item
        if(!leftSimilar) return false // Test similar
        if(exactCount) {
            if((leftItem!!.amount != item1.amount)) return false // test exact amount
        } else if(item1.amount < leftItem!!.amount) return false // test if it has at least the amount we ask

        CustomAnvil.verboseLog("Left item passed !")

        // we don't know if right item can be
        if(rightItem.isAir) { // null test
            if(!item2.isAir) return false
        } else {
            val rightSimilar = rightItem!!.isSimilar(item2)
            CustomAnvil.verboseLog("Right similar: $rightSimilar")
            if(!rightSimilar) return false // test if similar when not null

            if(exactCount) {
                if(rightItem!!.amount != item2!!.amount) return false // test exact amount
            } else if(item2!!.amount < rightItem!!.amount) return false // test if it has at least the amount we ask
        }

        CustomAnvil.verboseLog("Right item passed !")

        return true
    }

    override fun toString(): String {
        return name
    }

    fun determineCost(amount: Int, first: ItemStack, resultItem: ItemStack): Int {
        // First we determine the non-linear level cost
        var levelCost = levelCostPerCraft * amount
        // TODO Maybe add an option per custom craft to ignore/not ignore penalty ??
        levelCost += AnvilXpUtil.calculatePenalty(first, null, resultItem, AnvilUseType.CUSTOM_CRAFT)

        var xpCost = AnvilXpUtil.calculateXpForLevel(levelCost)
        // Then we add the linear cost
        xpCost += XpCostPerCraft * amount

        return xpCost
    }


}
