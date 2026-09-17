package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
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
@NotNullByDefault
public class CustomAnvilRecipeApi {

    private CustomAnvilRecipeApi() {
    }

    private static volatile @Nullable Object saveChangeTask = null;

    /**
     * Write and add a custom anvil recipe.
     * Will not write the recipe if it already exists.
     *
     * @param builder The recipe builder to be based on
     * @return True if successful.
     */
    public static boolean addRecipe(AnvilRecipeBuilder builder) {
        return addRecipe(builder, false);
    }

    /**
     * Write and add a custom anvil recipe.
     * Will not write the recipe if it already exists.
     *
     * @param builder         The recipe builder to be based on
     * @param overrideDeleted If we should write even if the recipe was previously deleted.
     * @return True if successful.
     */
    public static boolean addRecipe(AnvilRecipeBuilder builder, boolean overrideDeleted) {
        try(var lock = ConfigHolder.CUSTOM_RECIPE.write) {
            return addRecipe(lock.get(), builder, overrideDeleted);
        }
    }

    private static boolean addRecipe(ConfigHolder.CustomAnvilCraftHolder holder, AnvilRecipeBuilder builder, boolean overrideDeleted) {
        FileConfiguration config = holder.getConfig();
        String name = builder.getName();

        if(!overrideDeleted && holder.isDeleted(builder.getName())) return false;
        if(config.contains(builder.getName())) return false;

        if(builder.getName().contains(".")) {
            CustomAnvil.instance.getLogger().warning("Custom anvil recipe " + name + " contain \".\" in its name but should not. this recipe is ignored.");
            return false;
        }

        AnvilCustomRecipe recipe = builder.build();
        if(recipe == null) {
            CustomAnvil.instance.getLogger().warning("Custom anvil recipe " + name + " could not be parsed.");
            if(builder.getLeftItem() == null) {
                CustomAnvil.instance.getLogger().warning("It look like left item of the recipe is null.");
            }
            if(builder.getResultItem() == null) {
                CustomAnvil.instance.getLogger().warning("It look like result item of the recipe is null.");
            }
            return false;
        }

        // Add to registry
        holder.getRecipeManager().cleanAddNew(recipe);

        // Save to file
        recipe.saveToFile(false, false);
        prepareSaveTask();

        // Add from gui
        CustomRecipeConfigGui recipeConfigGui = CustomRecipeConfigGui.getCurrentInstance();
        if(recipeConfigGui != null) recipeConfigGui.updateValueForGeneric(recipe, true);

        return true;
    }

    // TODO remove by name and/or by builder (as name is kept) (and maybe create a get by name)

    /**
     * Remove a custom anvil recipe.
     *
     * @param recipe The recipe to remove
     * @return True if successful.
     */
    public static boolean removeRecipe(AnvilCustomRecipe recipe) {
        // Remove from registry
        try(var lock = ConfigHolder.CUSTOM_RECIPE.write) {
            var config = lock.get();
            boolean result = config.getRecipeManager().cleanRemove(recipe);
            if(!result) return false;

            // Delete and save to file
            config.delete(recipe.getName());
        }
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
        //noinspection DuplicatedCode
        if(saveChangeTask != null) return;

        @SuppressWarnings("UnnecessaryLocalVariable")
        var task = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            try(var lock = ConfigHolder.CONFLICT.write) {
                lock.get().saveToDisk(true);
                saveChangeTask = null;
            }
        });

        saveChangeTask = task;
    }

    /**
     * Get every registered recipes.
     *
     * @return An immutable collection of recipes.
     */
    public static List<AnvilCustomRecipe> getRegisteredRecipes() {
        try(var lock = ConfigHolder.CUSTOM_RECIPE.read) {
            var mutableList = lock.get().getRecipeManager().getRecipeList();
            return Collections.unmodifiableList(mutableList);
        }
    }

}
