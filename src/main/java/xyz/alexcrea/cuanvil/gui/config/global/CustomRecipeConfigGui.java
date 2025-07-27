package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.CustomRecipeSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
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
            displayedItem = ItemType.BARRIER.createItemStack();
        } else {
            displayedItem = craftResultItem.clone();
        }

        // edit displayed item
        ItemMeta meta = displayedItem.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§e" + CasedStringUtil.snakeToUpperSpacedCase(recipe.toString()) + " §fCustom recipe");
        meta.addItemFlags(ItemFlag.values());

        meta.setLore(getRecipeLore(recipe));

        displayedItem.setItemMeta(meta);
        return displayedItem;
    }

    private static @NotNull ArrayList<String> getRecipeLore(AnvilCustomRecipe recipe) {
        boolean shouldWork = recipe.validate();

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Is valid:    §" + (shouldWork ? "aYes" : "cNo"));
        lore.add("§7Exact count:    §" + (recipe.getExactCount() ? "aYes" : "cNo"));
        lore.add("§7Recipe Level Cost: §e" + recipe.getLevelCostPerCraft());
        lore.add("§7Recipe Linear Xp Cost: §e" + recipe.getXpCostPerCraft());
        if (recipe.getXpCostPerCraft() != 0) {
            lore.add("§7Exact Linear xp remove:    §" + (recipe.getRemoveExactLinearXp() ? "aYes" : "cNo"));
        }
        return lore;
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

                AnvilCustomRecipe.DEFAULT_XP_LEVEL_COST_CONFIG,
                AnvilCustomRecipe.DEFAULT_LINEAR_XP_COST_CONFIG,
                AnvilCustomRecipe.DEFAULT_REMOVE_EXACT_XP_CONFIG,

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
