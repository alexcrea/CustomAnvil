package xyz.alexcrea.cuanvil.gui.config.settings.subsetting;

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
        super(parentItemForThisGui, 3, "title");
        this.parent = parent;
        this.anvilRecipe = anvilRecipe;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "01230450D",
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
        exactCountFactory = BoolSettingsGui.boolFactory("title", this,
                this.anvilRecipe.getName()+"."+AnvilCustomRecipe.EXACT_COUNT_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_EXACT_COUNT_CONFIG());

        xpCostFactory = IntSettingsGui.intFactory("title", this,
                this.anvilRecipe.getName()+"."+AnvilCustomRecipe.XP_COST_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                costRange.getFirst(), costRange.getLast(), AnvilCustomRecipe.Companion.getDEFAULT_XP_COST_CONFIG(), 1, 5, 10);


        leftItemFactory = ItemSettingGui.itemFactory("title", this,
                this.anvilRecipe.getName()+"."+AnvilCustomRecipe.LEFT_ITEM_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_LEFT_ITEM_CONFIG());

        rightItemFactory = ItemSettingGui.itemFactory("title", this,
                this.anvilRecipe.getName()+"."+AnvilCustomRecipe.EXACT_COUNT_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
                AnvilCustomRecipe.Companion.getDEFAULT_RIGHT_ITEM_CONFIG());

        resultItemFactory = ItemSettingGui.itemFactory("title", this,
                this.anvilRecipe.getName()+"."+AnvilCustomRecipe.EXACT_COUNT_CONFIG, ConfigHolder.CUSTOM_RECIPE_HOLDER,
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
            cleanUnused();

            // Update config file storage
            ConfigHolder.CUSTOM_RECIPE_HOLDER.getConfig().set(this.anvilRecipe.getName(), null);

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.anvilRecipe.getName()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
        );
    }

    @Override
    public void updateGuiValues() {
        this.parent.updateValueForGeneric(this.anvilRecipe, true);
        // Parent should call updateLocal
    }

    public void updateLocal() {
        if (!this.shouldWork) return;

        /*// Prepare enchantment lore
        ArrayList<String> enchantLore = new ArrayList<>();
        enchantLore.add("\u00A77Allow you to select a list of \u00A75Enchantments \u00A77that this conflict should include");
        Set<Enchantment> enchants = getSelectedEnchantments();
        if (enchants.isEmpty()) {
            enchantLore.add("\u00A77There is no included enchantment for this conflict.");
        } else {
            enchantLore.add("\u00A77List of included enchantment for this conflict:");
            Iterator<Enchantment> enchantIterator = enchants.iterator();

            boolean greaterThanMax = enchants.size() > 5;
            int maxindex = (greaterThanMax ? 4 : enchants.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Fire Protection"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(enchantIterator.next().getKey().getKey());
                enchantLore.add("\u00A77- \u00A75" + formattedName);
            }
            if (greaterThanMax) {
                enchantLore.add("\u00A77And " + (enchants.size() - 4) + " more...");
            }

        }

        // Prepare group lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("\u00A77Allow you to select a list of \u00A73Groups \u00A77that this conflict should include");
        Set<AbstractMaterialGroup> grouos = getSelectedGroups();
        if (grouos.isEmpty()) {
            groupLore.add("\u00A77There is no excluded groups for this conflict.");
        } else {
            groupLore.add("\u00A77List of excluded groups for this conflict:");
            Iterator<AbstractMaterialGroup> groupIterator = grouos.iterator();

            boolean greaterThanMax = grouos.size() > 5;
            int maxindex = (greaterThanMax ? 4 : grouos.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Melee Weapons"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
                groupLore.add("\u00A77- \u00A73" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("\u00A77And " + (grouos.size() - 4) + " more...");
            }
        }

        // Configure enchant setting item
        ItemStack enchantItem = this.enchantSettingItem.getItem();
        ItemMeta enchantMeta = enchantItem.getItemMeta();

        enchantMeta.setDisplayName("\u00A7aSelect included \u00A75Enchantments \u00A7aSettings");
        enchantMeta.setLore(enchantLore);

        enchantItem.setItemMeta(enchantMeta);

        this.enchantSettingItem.setItem(enchantItem); // Just in case

        // Configure group setting item
        ItemStack groupItem = this.groupSettingItem.getItem();
        ItemMeta groupMeta = groupItem.getItemMeta();

        groupMeta.setDisplayName("\u00A7aSelect excluded \u00A73Groups \u00A7aSettings");
        groupMeta.setLore(groupLore);

        groupItem.setItemMeta(groupMeta);

        this.groupSettingItem.setItem(groupItem); // Just in case


        this.pane.bindItem('M', GuiGlobalItems.intSettingGuiItem(this.minBeforeActiveSettingFactory, Material.COMMAND_BLOCK));*/
        update();
    }

    public void cleanUnused() {
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
