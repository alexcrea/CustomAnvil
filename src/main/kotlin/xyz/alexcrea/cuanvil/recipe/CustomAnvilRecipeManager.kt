package xyz.alexcrea.cuanvil.recipe

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack

class CustomAnvilRecipeManager {

    lateinit var recipeList: ArrayList<AnvilCustomRecipe>

    lateinit var recipeByMat: HashMap<Material, ArrayList<AnvilCustomRecipe>>

    fun prepareRecipes(config: FileConfiguration) {
        recipeList = ArrayList()
        recipeByMat = HashMap()

        // read all configs
        val keys = config.getKeys(false)
        for (key in keys) {
            val recipe = AnvilCustomRecipe.getFromConfig(key)
            if (recipe == null) {
                CustomAnvil.log("Can't load recipe $key")
                continue
            }

            cleanAddNew(recipe)
        }

    }


    fun cleanAddNew(recipe: AnvilCustomRecipe) {
        recipeList.add(recipe)
        val leftItem = recipe.leftItem
        if (leftItem != null) {
            addToMatMap(recipe, leftItem)
        }

    }

    fun cleanSetLeftItem(recipe: AnvilCustomRecipe, leftItem: ItemStack?) {
        // Remove left item mat if exist
        val oldLeftItem = recipe.leftItem
        if (oldLeftItem != null) {
            val oldMat = oldLeftItem.type

            val test = recipeByMat[oldMat]
            test!!.remove(recipe)
        }
        if (leftItem != null) {
            addToMatMap(recipe, leftItem)
        }

        recipe.leftItem = leftItem
    }

    private fun addToMatMap(recipe: AnvilCustomRecipe, leftItem: ItemStack) {
        var recipeList = recipeByMat[leftItem.type]
        if (recipeList == null) {
            recipeList = ArrayList()
            recipeByMat[leftItem.type] = recipeList
        }
        recipeList.add(recipe)

    }

    fun cleanRemove(recipe: AnvilCustomRecipe): Boolean {

        val exist = recipeList.remove(recipe)
        if (exist) {
            cleanSetLeftItem(recipe, null)
        }

        return exist;
    }

}