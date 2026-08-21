package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
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
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomRecipeConfigGui extends MappedGuiListConfigGui<AnvilCustomRecipe,
        MappedGuiListConfigGui.LazyElement<CustomRecipeSubSettingGui>> {

    private static CustomRecipeConfigGui INSTANCE = new CustomRecipeConfigGui();

    @Nullable
    public static CustomRecipeConfigGui getCurrentInstance() {
        return INSTANCE;
    }

    @NotNull
    public static CustomRecipeConfigGui getInstance() {
        if(INSTANCE == null) INSTANCE = new CustomRecipeConfigGui();

        return INSTANCE;
    }

    private CustomRecipeConfigGui() {
        super(MsgUI.INSTANCE.getCUSTOM_RECIPE_TITLE());

        init();
    }

    public CustomRecipeConfigGui(Gui parent) {
        super(MsgUI.INSTANCE.getCUSTOM_RECIPE_TITLE(), parent);
    }

    @Override
    protected ItemStack createItemForGeneric(AnvilCustomRecipe recipe) {
        // Get base item to display
        ItemStack craftResultItem = recipe.getResultItem();
        ItemStack displayedItem;
        if(craftResultItem == null) {
            displayedItem = new ItemStack(Material.BARRIER);
        } else {
            displayedItem = craftResultItem.clone();
        }

        // edit displayed item
        ItemMeta meta = displayedItem.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getCUSTOM_RECIPE_NAME());
        ComponentUtil.INSTANCE.applyLore(getRecipeLore(recipe), meta);

        displayedItem.setItemMeta(meta);
        return displayedItem;
    }

    private static @NotNull List<Component> getRecipeLore(AnvilCustomRecipe recipe) {
        boolean shouldWork = recipe.validate();

        var shouldWorkMsg = MsgUI.INSTANCE.booleanMessage(shouldWork);
        var exactCount = MsgUI.INSTANCE.booleanMessage(recipe.getExactCount());

        ArrayList<Component> lore = new ArrayList<>(MsgUI.INSTANCE.getCUSTOM_RECIPE_LORE_DEFAULT()
                .formatted(
                        shouldWorkMsg,
                        exactCount,
                        recipe.getLevelCostPerCraft(),
                        recipe.getXpCostPerCraft()
                ));

        if(recipe.getXpCostPerCraft() != 0) {
            var removeExact = MsgUI.INSTANCE.booleanMessage(recipe.getRemoveExactLinearXp());
            lore.addAll(MsgUI.INSTANCE.getCUSTOM_RECIPE_LORE_LINEAR().formatted(removeExact));
        }
        return lore;
    }

    @Override
    protected LazyElement<CustomRecipeSubSettingGui> newInstanceOfGui(AnvilCustomRecipe generic, GuiItem item) {
        return new LazyElement<>(item, () -> new CustomRecipeSubSettingGui(this, generic));
    }

    @Override
    protected Message genericDisplayedName() {
        return MsgUI.INSTANCE.getCUSTOM_RECIPE_GENERIC_NAME();
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
                AnvilCustomRecipe.Companion.getDEFAULT_RESULT_ITEM_CONFIG()
        );

        ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().cleanAddNew(recipe);

        // Save recipe to file
        recipe.saveToFile(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE, GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);

        return recipe;
    }


    @Override
    protected Collection<AnvilCustomRecipe> getEveryInstanceOfGeneric() {
        return ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager().getRecipeList();
    }
}
