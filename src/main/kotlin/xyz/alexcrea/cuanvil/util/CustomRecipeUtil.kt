package xyz.alexcrea.cuanvil.util

import io.delilaheve.CustomAnvil
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import kotlin.math.min

object CustomRecipeUtil {

    fun getCustomRecipe (
        leftItem: ItemStack,
        rightItem: ItemStack?) : AnvilCustomRecipe? {

        val recipeList = ConfigHolder.CUSTOM_RECIPE_HOLDER.recipeManager.recipeByMat[leftItem.type] ?: return null

        CustomAnvil.verboseLog("Testing " + recipeList.size + " recipe...")
        for (recipe in recipeList) {
            if(recipe.testItem(leftItem, rightItem)){
                return recipe
            }
        }

        return null
    }

    fun getCustomRecipeAmount(
        recipe: AnvilCustomRecipe,
        leftItem: ItemStack,
        rightItem: ItemStack?
    ): Int{
        return if(recipe.exactCount) {
            if(leftItem.amount != recipe.leftItem!!.amount){
                0
            }else if(rightItem != null && rightItem.amount != recipe.rightItem!!.amount){
                0
            }else{
                1
            }
        }
        else {
            // test amount
            val resultItem = recipe.resultItem!! // we know exist as the recipe was returned to us
            val maxResultAmount = resultItem.type.maxStackSize/resultItem.amount
            val maxLeftAmount = leftItem.amount/recipe.leftItem!!.amount
            val maxRightAmount = if(rightItem == null){ maxLeftAmount } else{ rightItem.amount/recipe.rightItem!!.amount }

            CustomAnvil.verboseLog("resultItem: $resultItem, maxResultAmount: $maxResultAmount, maxLeftAmount: $maxLeftAmount, maxRightAmount: $maxRightAmount")

            min(min(maxResultAmount, maxLeftAmount), maxRightAmount)
        }
    }

}