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
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
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
            @NotNull AnvilCustomRecipe anvilRecipe) {
        super(4, "§e" + CasedStringUtil.snakeToUpperSpacedCase(anvilRecipe.toString()) + " §8Config");
        this.parent = parent;
        this.anvilRecipe = anvilRecipe;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "01203450D",
                "0ab000000",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 4, pattern);
        addPane(this.pane);

        prepareStaticValues();
    }

    private BoolSettingsGui.BoolSettingFactory exactCountFactory;
    private BoolSettingsGui.BoolSettingFactory removeExactLinearXpFactory;
    private GuiItem noRemoveExactLinearXp;

    private IntSettingsGui.IntSettingFactory levelCostFactory;
    private IntSettingsGui.IntSettingFactory linearXpCostFactory;

    private ItemSettingGui.ItemSettingFactory leftItemFactory;
    private ItemSettingGui.ItemSettingFactory rightItemFactory;
    private ItemSettingGui.ItemSettingFactory resultItemFactory;

    private void prepareStaticValues() {

        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        assert deleteMeta != null;

        deleteMeta.setDisplayName("§4DELETE RECIPE");
        deleteMeta.setLore(Collections.singletonList("§cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

        // Displayed item will be updated later
        IntRange costRange = AnvilCustomRecipe.Companion.getXP_COST_CONFIG_RANGE();
        this.exactCountFactory = new BoolSettingsGui.BoolSettingFactory("§8Exact count ?", this,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                this.anvilRecipe + "." + AnvilCustomRecipe.EXACT_COUNT_CONFIG, AnvilCustomRecipe.DEFAULT_EXACT_COUNT_CONFIG);

        this.removeExactLinearXpFactory = new BoolSettingsGui.BoolSettingFactory("§8Remove exact linear xp ?", this,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                this.anvilRecipe + "." + AnvilCustomRecipe.REMOVE_EXACT_XP_CONFIG, AnvilCustomRecipe.DEFAULT_REMOVE_EXACT_XP_CONFIG);

        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cRemove exact linear xp ?");
        meta.setLore(Collections.singletonList("§7Not usable if linear cost is 0"));
        item.setItemMeta(meta);
        this.noRemoveExactLinearXp = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        this.levelCostFactory = new IntSettingsGui.IntSettingFactory("§8Recipe Level Cost", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.XP_LEVEL_COST_CONFIG,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                null,
                costRange.getFirst(), costRange.getLast(), AnvilCustomRecipe.DEFAULT_XP_LEVEL_COST_CONFIG, 1, 5, 10);

        this.linearXpCostFactory = new IntSettingsGui.IntSettingFactory("§8Recipe Linear Xp Cost", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.LINEAR_XP_COST_CONFIG,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                null,
                0, Integer.MAX_VALUE, AnvilCustomRecipe.DEFAULT_LINEAR_XP_COST_CONFIG, 1, 10, 100, 1000, 10000);


        // Right part of the gui
        this.leftItemFactory = new ItemSettingGui.ItemSettingFactory("§eRecipe Left §8Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.LEFT_ITEM_CONFIG,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_LEFT_ITEM_CONFIG(),
                "§7Set the left item of the custom craft",
                "§7\u25A0 + \u25A1 = \u25A1");

        this.rightItemFactory = new ItemSettingGui.ItemSettingFactory("§eRecipe Right §8Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.RIGHT_ITEM_CONFIG,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_RIGHT_ITEM_CONFIG(),
                "§7Set the right item of the custom craft",
                "§7\u25A1 + \u25A0 = \u25A1");

        this.resultItemFactory = new ItemSettingGui.ItemSettingFactory("§aRecipe Result §8Item", this,
                this.anvilRecipe + "." + AnvilCustomRecipe.RESULT_ITEM_CONFIG,
                ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_RESULT_ITEM_CONFIG(),
                "§7Set the result item of the custom craft",
                "§7\u25A1 + \u25A1 = \u25A0");

        // Now we update the items
        updateLocal();
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
            ConfigHolder.CUSTOM_RECIPE_HOLDER.delete(this.anvilRecipe.toString());

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("§cDelete §e" + CasedStringUtil.snakeToUpperSpacedCase(this.anvilRecipe.toString()) + "§c?",
                "§7Confirm that you want to delete this conflict.",
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

        GuiItem exactCountItem = this.exactCountFactory.getItem();
        this.pane.bindItem('1', exactCountItem);

        if (anvilRecipe.getXpCostPerCraft() == 0) {
            this.pane.bindItem('a', noRemoveExactLinearXp);
        } else {
            this.pane.bindItem('a', removeExactLinearXpFactory.getItem());
        }

        GuiItem levelCostItem = this.levelCostFactory.getItem(Material.EXPERIENCE_BOTTLE);
        this.pane.bindItem('2', levelCostItem);


        GuiItem xpCostItem = this.linearXpCostFactory.getItem(Material.EXPERIENCE_BOTTLE);
        this.pane.bindItem('b', xpCostItem);

        GuiItem leftGuiItem = this.leftItemFactory.getItem();
        this.pane.bindItem('3', leftGuiItem);

        GuiItem rightGuiItem = this.rightItemFactory.getItem();
        this.pane.bindItem('4', rightGuiItem);

        GuiItem resultGuiItem = this.resultItemFactory.getItem();
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
