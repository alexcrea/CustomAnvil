package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import kotlin.ranges.IntRange;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.CustomRecipeConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.ItemSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.recipe.CustomAnvilRecipeManager;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.function.Supplier;

public class CustomRecipeSubSettingGui extends MappedToListSubSettingGui {

    private final CustomRecipeConfigGui parent;
    private final AnvilCustomRecipe anvilRecipe;
    private final PatternPane pane;
    private boolean shouldWork = true;

    public CustomRecipeSubSettingGui(
            @NotNull CustomRecipeConfigGui parent,
            @NotNull AnvilCustomRecipe anvilRecipe,
            @NotNull GuiItem parentItemForThisGui) {
        super(parentItemForThisGui, 3, "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(anvilRecipe.toString()) + " \u00A78Config");
        this.parent = parent;
        this.anvilRecipe = anvilRecipe;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "01203450D",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(this.pane);

        prepareStaticValues();
    }

    BoolSettingsGui.BoolSettingFactory exactCountFactory;
    IntSettingsGui.IntSettingFactory xpCostFactory;
    ItemSettingGui.ItemSettingFactory leftItemFactory;
    ItemSettingGui.ItemSettingFactory rightItemFactory;
    ItemSettingGui.ItemSettingFactory resultItemFactory;

    private void prepareStaticValues() {

        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE RECIPE");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

        // Displayed item will be updated later

        IntRange costRange = AnvilCustomRecipe.Companion.getXP_COST_CONFIG_RANGE();
        this.exactCountFactory = BoolSettingsGui.boolFactory("\u00A78Exact count ?", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.EXACT_COUNT_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_EXACT_COUNT_CONFIG());

        this.xpCostFactory = IntSettingsGui.intFactory("\u00A78Recipe Xp Cost", this,
                this.anvilRecipe +"."+AnvilCustomRecipe.XP_COST_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                costRange.getFirst(), costRange.getLast(), AnvilCustomRecipe.Companion.getDEFAULT_XP_COST_CONFIG(), 1, 5, 10);


        this.leftItemFactory = ItemSettingGui.itemFactory("\u00A7eRecipe Left \u00A78Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.LEFT_ITEM_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_LEFT_ITEM_CONFIG());

        this.rightItemFactory = ItemSettingGui.itemFactory("\u00A7eRecipe Right \u00A78Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.RIGHT_ITEM_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_RIGHT_ITEM_CONFIG());

        this.resultItemFactory = ItemSettingGui.itemFactory("\u00A7aRecipe Result \u00A78Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.RESULT_ITEM_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_RESULT_ITEM_CONFIG());
    }

    private ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            CustomAnvilRecipeManager manager = ConfigHolder.CUSTOM_RECIPE_HOLDER.getRecipeManager();

            // Remove from manager
            manager.cleanRemove(this.anvilRecipe);

            // Remove from parent
            this.parent.removeGeneric(this.anvilRecipe);

            // Remove self
            cleanAndBeUnusable();

            // Update config file storage
            ConfigHolder.CUSTOM_RECIPE_HOLDER.getConfig().set(this.anvilRecipe.toString(), null);

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.anvilRecipe.toString()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
        );
    }

    @Override
    public void updateGuiValues() {
        // update value from config to conflict
        this.anvilRecipe.updateFromFile();

        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.anvilRecipe, true);
    }

    public void updateLocal() {
        if (!this.shouldWork) return;

        GuiItem exactCountItem = GuiGlobalItems.boolSettingGuiItem(this.exactCountFactory);
        this.pane.bindItem('1', exactCountItem);

        GuiItem xpCostItem = GuiGlobalItems.intSettingGuiItem(this.xpCostFactory, Material.EXPERIENCE_BOTTLE);
        this.pane.bindItem('2', xpCostItem);

        GuiItem leftGuiItem = GuiGlobalItems.itemSettingGuiItem(this.leftItemFactory);
        this.pane.bindItem('3', leftGuiItem);

        GuiItem rightGuiItem = GuiGlobalItems.itemSettingGuiItem(this.rightItemFactory);
        this.pane.bindItem('4', rightGuiItem);

        GuiItem resultGuiItem = GuiGlobalItems.itemSettingGuiItem(this.resultItemFactory);
        this.pane.bindItem('5', resultGuiItem);
        
        update();
    }

    public void cleanAndBeUnusable() {
        for (HumanEntity viewer : getViewers()) {
            this.parent.show(viewer);
        }
        this.shouldWork = false;

        // Just in case something is extremely wrong
        GuiItem background = GuiGlobalItems.backgroundItem();
        this.pane.bindItem('1', background);
        this.pane.bindItem('2', background);
        this.pane.bindItem('3', background);
        this.pane.bindItem('4', background);
        this.pane.bindItem('5', background);

        this.pane.bindItem('D', background);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (this.shouldWork) {
            super.show(humanEntity);
        } else {
            this.parent.show(humanEntity);
        }
    }


}
