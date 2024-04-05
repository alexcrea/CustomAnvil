package xyz.alexcrea.cuanvil.recipe

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import java.util.LinkedHashMap

class CustomAnvilRecipeManager {

    lateinit var recipeMap: LinkedHashMap<String, AnvilCustomRecipe>

    lateinit var recipeByMat: LinkedHashMap<Material, ArrayList<AnvilCustomRecipe>>

    fun prepareRecipes(config: FileConfiguration) {
        recipeMap = LinkedHashMap()
        recipeByMat = LinkedHashMap()

        // read all configs
        val keys = config.getKeys(false)
        for (key in keys) {
            if (recipeMap.containsKey(key))
                continue
            val recipe = AnvilCustomRecipe.getFromConfig(key)
            if(recipe == null){
                CustomAnvil.log("Can't load recipe $key")
                continue
            }

            recipeMap[key] = recipe
            val leftItem = recipe.leftItem
            if(leftItem != null){
                addToMap(recipe, leftItem)
            }
        }

    }

    fun cleanSetLeftItem(recipe: AnvilCustomRecipe, leftItem: ItemStack?){
        // Remove left item mat if exist
        val oldLeftItem = recipe.leftItem
        if(oldLeftItem != null){
            val oldMat = oldLeftItem.type

            val test = recipeByMat[oldMat]
            test!!.remove(recipe)
        }
        if(leftItem != null){
            addToMap(recipe, leftItem)
        }

        recipe.leftItem = leftItem
    }

    fun addToMap(recipe: AnvilCustomRecipe, leftItem: ItemStack){
        var recipeList = recipeByMat[leftItem.type]
        if(recipeList == null){
            recipeList = ArrayList()
            recipeByMat[leftItem.type] = recipeList
        }
        recipeList.add(recipe)

    }

}