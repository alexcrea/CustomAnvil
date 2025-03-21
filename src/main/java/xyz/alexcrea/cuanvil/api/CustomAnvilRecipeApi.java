package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.gui.config.global.CustomRecipeConfigGui;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;

import java.util.Collections;
import java.util.List;

/**
 * Custom Anvil api for custom anvil recipes.
 */
@SuppressWarnings("unused")
public class CustomAnvilRecipeApi {

    private CustomAnvilRecipeApi(){}

    private static Object saveChangeTask = null;

    /**
     * Write and add a custom anvil recipe.
     * Will not write the recipe if it already exists.
     *
     * @param builder The recipe builder to be based on
     * @return True if successful.
     */
    public static boolean addRecipe(@NotNull AnvilRecipeBuilder builder){
        return addRecipe(builder, false);
    }

    /**
     * Write and add a custom anvil recipe.
     * Will not write the recipe if it already exists.
     *
     * @param builder The recipe builder to be based on
     * @param overrideDeleted If we should write even if the recipe was previously deleted.
     * @return True if successful.
     */
    public static boolean addRecipe(@NotNull AnvilRecipeBuilder builder, boolean overrideDeleted){
        FileConfiguration config = ConfigHolder.CUSTOM_RECIPE_HOLDER.getConfig();
        String name = builder.getName();

        if(!overrideDeleted && ConfigHolder.CUSTOM_RECIPE_HOLDER.isDeleted(builder.getName())) return false;
        if(config.contains(builder.getName())) return false;

        if(builder.getName().contains(".")) {
            CustomAnvil.instance.getLogger().warning("Custom anvil recipe " + name + " contain \".\" in its name but should not. this recipe is ignored.");
            return false;
        }

        AnvilCustomRecipe recipe = builder.build();
        if(recipe == null){
            CustomAnvil.instance.getLogger().warning("Custom anvil recipe " + name + " could not be parsed.");
            if(builder.getLeftItem() == null){
                CustomAnvil.instance.getLogger().warning("It look like left item of the recipe is null.");
            }
            if(builder.getResultItem() == null){
                CustomAnvil.instance.getLogger().warning("It look like result item of the recipe is null.");
            }
            return false;
        }

        // Add to registry
        ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().cleanAddNew(recipe);

        // Save to file
        recipe.saveToFile(false, false);
        prepareSaveTask();

        // Add from gui
        CustomRecipeConfigGui recipeConfigGui = CustomRecipeConfigGui.getCurrentInstance();
        if(recipeConfigGui != null) recipeConfigGui.updateValueForGeneric(recipe, true);

        return true;
    }

    // TODO remove by name and/or by builder (as name is keept) (and maybe create a get by name)
    /**
     * Remove a custom anvil recipe.
     *
     * @param recipe The recipe to remove
     * @return True if successful.
     */
    public static boolean removeRecipe(@NotNull AnvilCustomRecipe recipe){
        // Remove from registry
        boolean result = ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().cleanRemove(recipe);
        if(!result) return false;

        // Delete and save to file
        ConfigHolder.CUSTOM_RECIPE_HOLDER.delete(recipe.getName());
        prepareSaveTask();

        // Remove from gui
        CustomRecipeConfigGui recipeConfigGui = CustomRecipeConfigGui.getCurrentInstance();
        if(recipeConfigGui != null) recipeConfigGui.removeGeneric(recipe);

        return true;
    }

    /**
     * Prepare a task to save custom recipe configuration.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != null) return;

        saveChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, ()->{
            ConfigHolder.CONFLICT_HOLDER.saveToDisk(true);
            saveChangeTask = null;
        });
    }

    /**
     * Get every registered recipes.
     * @return An immutable collection of recipes.
     */
    @NotNull
    public static List<AnvilCustomRecipe> getRegisteredRecipes(){
        List<AnvilCustomRecipe> mutableList = ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().getRecipeList();
        return Collections.unmodifiableList(mutableList);
    }

}
