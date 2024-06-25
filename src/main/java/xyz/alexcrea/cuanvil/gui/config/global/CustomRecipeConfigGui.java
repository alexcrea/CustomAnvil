package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.CustomRecipeSubSettingGui;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Collection;

public class CustomRecipeConfigGui extends MappedGuiListConfigGui<AnvilCustomRecipe, CustomRecipeSubSettingGui> {


    public final static CustomRecipeConfigGui INSTANCE = new CustomRecipeConfigGui();

    static {
        INSTANCE.init();
    }

    private CustomRecipeConfigGui() {
        super("Custom Recipe Config");

    }

    @Override
    protected ItemStack createItemForGeneric(AnvilCustomRecipe recipe) {
        // Get base item to display
        ItemStack craftResultItem = recipe.getResultItem();
        ItemStack displaydItem;
        if(craftResultItem == null){
            displaydItem = new ItemStack(Material.BARRIER);
        }else{
            displaydItem = craftResultItem.clone();
        }

        // edit displayed item
        ItemMeta meta = displaydItem.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(recipe.toString()) + " \u00A7fCustom recipe");
        meta.addItemFlags(ItemFlag.values());

        boolean shouldWork = recipe.validate();

        meta.setLore(Arrays.asList(
                "\u00A77Should work:    \u00A7"+(shouldWork ? "aYes" : "cNo"),
                "\u00A77Exact count:    \u00A7"+(recipe.getExactCount() ? "aYes" : "cNo"),
                "\u00A77Recipe Xp Cost: \u00A7e"+recipe.getXpCostPerCraft()

        ));

        displaydItem.setItemMeta(meta);
        return displaydItem;
    }

    @Override
    protected CustomRecipeSubSettingGui newInstanceOfGui(AnvilCustomRecipe generic, GuiItem item) {
        return new CustomRecipeSubSettingGui(this, generic, item);
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
        recipe.saveToFile();

        return recipe;
    }


    @Override
    protected Collection<AnvilCustomRecipe> getEveryDisplayableInstanceOfGeneric() {
        return ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().getRecipeList();
    }
}
