package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.CustomRecipeSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Collection;

public class CustomRecipeConfigGui extends MappedGuiListConfigGui<AnvilCustomRecipe,
        MappedGuiListConfigGui.LazyElement<CustomRecipeSubSettingGui>> {

    private static CustomRecipeConfigGui INSTANCE = new CustomRecipeConfigGui();

    @Nullable
    public static CustomRecipeConfigGui getCurrentInstance() {
        return INSTANCE;
    }

    @NotNull
    public static CustomRecipeConfigGui getInstance() {
        if (INSTANCE == null) INSTANCE = new CustomRecipeConfigGui();

        return INSTANCE;
    }

    private CustomRecipeConfigGui() {
        super("Custom Recipe Config");

        init();
    }

    @Override
    protected ItemStack createItemForGeneric(AnvilCustomRecipe recipe) {
        // Get base item to display
        ItemStack craftResultItem = recipe.getResultItem();
        ItemStack displayedItem;
        if (craftResultItem == null) {
            displayedItem = new ItemStack(Material.BARRIER);
        } else {
            displayedItem = craftResultItem.clone();
        }

        // edit displayed item
        ItemMeta meta = displayedItem.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§e" + CasedStringUtil.snakeToUpperSpacedCase(recipe.toString()) + " §fCustom recipe");
        meta.addItemFlags(ItemFlag.values());

        boolean shouldWork = recipe.validate();

        meta.setLore(Arrays.asList(
                "§7Should work:    §" + (shouldWork ? "aYes" : "cNo"),
                "§7Exact count:    §" + (recipe.getExactCount() ? "aYes" : "cNo"),
                "§7Recipe Xp Cost: §e" + recipe.getXpCostPerCraft()

        ));

        displayedItem.setItemMeta(meta);
        return displayedItem;
    }

    @Override
    protected LazyElement<CustomRecipeSubSettingGui> newInstanceOfGui(AnvilCustomRecipe generic, GuiItem item) {
        return new LazyElement<>(item, () -> new CustomRecipeSubSettingGui(this, generic));
    }

    @Override
    protected String genericDisplayedName() {
        return "custom recipe";
    }

    @Override
    protected AnvilCustomRecipe createAndSaveNewEmptyGeneric(String name) {
        // Create new empty conflict and display it to the admin
        AnvilCustomRecipe recipe = new AnvilCustomRecipe(
                name,
                AnvilCustomRecipe.DEFAULT_EXACT_COUNT_CONFIG,
                AnvilCustomRecipe.DEFAULT_XP_COST_CONFIG,
                AnvilCustomRecipe.Companion.getDEFAULT_LEFT_ITEM_CONFIG(),
                AnvilCustomRecipe.Companion.getDEFAULT_RIGHT_ITEM_CONFIG(),
                AnvilCustomRecipe.Companion.getDEFAULT_RESULT_ITEM_CONFIG());

        ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().cleanAddNew(recipe);

        // Save recipe to file
        recipe.saveToFile(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE, GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);

        return recipe;
    }


    @Override
    protected Collection<AnvilCustomRecipe> getEveryDisplayableInstanceOfGeneric() {
        return ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().getRecipeList();
    }
}
