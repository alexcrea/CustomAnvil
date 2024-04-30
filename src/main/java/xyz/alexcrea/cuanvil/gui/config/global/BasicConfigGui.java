package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import kotlin.ranges.IntRange;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Arrays;
import java.util.Collections;

/**
 * Global config to edit basic basic settings.
 */
public class BasicConfigGui extends ValueUpdatableGui {

    public final static BasicConfigGui INSTANCE = new BasicConfigGui();

    static {
        INSTANCE.init();
    }

    /**
     * Constructor of this Global gui for basic settings.
     */
    private BasicConfigGui() {
        super(3, "\u00A78Basic Config", CustomAnvil.instance);
    }

    PatternPane pane;

    /**
     * Initialise Basic gui
     */
    private void init() {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "012345670",
                "B00000000"
        );
        pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);
        GuiGlobalItems.addBackgroundItem(pane);

        prepareValues();
        updateGuiValues();
    }

    private BoolSettingsGui.BoolSettingFactory limitRepairFactory;
    private IntSettingsGui.IntSettingFactory repairCostFactory;
    private GuiItem notNeededLimitValueItem;
    private BoolSettingsGui.BoolSettingFactory removeRepairLimit;
    private IntSettingsGui.IntSettingFactory itemRepairCost;
    private IntSettingsGui.IntSettingFactory unitRepairCost;
    private IntSettingsGui.IntSettingFactory itemRenameCost;
    private IntSettingsGui.IntSettingFactory sacrificeIllegalEnchantCost;

    /**
     * Prepare basic gui displayed items factory and static items..
     */
    protected void prepareValues() {
        // limit repair item
        this.limitRepairFactory = BoolSettingsGui.boolFactory("\u00A78Limit Repair Cost ?", this,
                ConfigOptions.LIMIT_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG, ConfigOptions.DEFAULT_LIMIT_REPAIR,
                "",
                "\u00A77Whether all anvil actions cost should be capped.",
                "\u00A77If true, all anvil repairs will max out at the value of \u00A7aLimit Repair Value\u00A77.");

        // repair cost item
        IntRange range = ConfigOptions.REPAIR_LIMIT_RANGE;
        this.repairCostFactory = IntSettingsGui.intFactory("\u00A78Repair Cost Limit", this,
                ConfigOptions.LIMIT_REPAIR_VALUE, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "",
                        "\u00A77Value to limit repair costs to when \u00A7aLimit Repair Value\u00A77 is true.",
                        "\u00A77Valid values include \u00A7e1 \u00A77to \u00A7e39\u00A77: " +
                                "vanilla would display \u00A7e40+\u00A77 as \u00A7ctoo expensive\u00A77."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_LIMIT_REPAIR_VALUE,
                1, 5, 10);

        // repair cost not needed
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7cLimit Repair Value");
        meta.setLore(Collections.singletonList("\u00A77This config need \u00A7cLimit Repair Cost\u00A77 enabled."));
        item.setItemMeta(meta);
        this.notNeededLimitValueItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        // remove repair limit item
        this.removeRepairLimit = BoolSettingsGui.boolFactory("\u00A78Remove Repair Limit ?", this,
                ConfigOptions.REMOVE_REPAIR_LIMIT, ConfigHolder.DEFAULT_CONFIG, ConfigOptions.DEFAULT_REMOVE_LIMIT,
                "",
                "\u00A77Whether the anvil's repair limit should be removed entirely.",
                "\u00A77The anvil will still visually display \u00A7ctoo expensive\u00A77.",
                "\u00A77However the action will be completable.");

        // item repair cost
        range = ConfigOptions.REPAIR_COST_RANGE;
        this.itemRepairCost = IntSettingsGui.intFactory("\u00A78Item Repair Cost", this,
                ConfigOptions.ITEM_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "",
                        "\u00A77XP Level amount added to the anvil when the item",
                        "\u00A77is repaired by another item of the same type."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_REPAIR_COST,
                1, 5, 10, 50, 100);

        // unit repair cost
        this.unitRepairCost = IntSettingsGui.intFactory("\u00A78Unit Repair Cost", this,
                ConfigOptions.UNIT_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "",
                        "\u00A77XP Level amount added to the anvil when the item is repaired by an \u00A7eunit\u00A77.",
                        "\u00A77For example: a Diamond on a Diamond Sword.",
                        "\u00A77What's considered unit for what can be edited on the unit repair configuration."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_UNIT_REPAIR_COST,
                1, 5, 10, 50, 100);

        // item rename cost
        range = ConfigOptions.ITEM_RENAME_COST_RANGE;
        this.itemRenameCost = IntSettingsGui.intFactory("\u00A78Rename Cost", this,
                ConfigOptions.ITEM_RENAME_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "",
                        "\u00A77XP Level amount added to the anvil when the item is renamed."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_RENAME_COST,
                1, 5, 10, 50, 100);

        // sacrifice illegal enchant cost
        range = ConfigOptions.SACRIFICE_ILLEGAL_COST_RANGE;
        this.sacrificeIllegalEnchantCost = IntSettingsGui.intFactory("\u00A78Sacrifice Illegal Enchant Cost", this,
                ConfigOptions.SACRIFICE_ILLEGAL_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "",
                        "\u00A77XP Level amount added to the anvil when a sacrifice enchantment",
                        "\u00A77conflict With one of the left item enchantment"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST,
                1, 5, 10, 50, 100);

    }

    @Override
    public void updateGuiValues() {
        // limit repair item
        GuiItem limitRepairItem = this.limitRepairFactory.getItem();
        pane.bindItem('1', limitRepairItem);

        // rename cost item
        GuiItem limitRepairValueItem;
        if (this.limitRepairFactory.getConfiguredValue()) {
            limitRepairValueItem = this.repairCostFactory.getItem(Material.EXPERIENCE_BOTTLE);
        } else {
            limitRepairValueItem = this.notNeededLimitValueItem;
        }
        pane.bindItem('2', limitRepairValueItem);

        // remove repair limit item
        GuiItem removeRepairLimitItem = this.removeRepairLimit.getItem();
        pane.bindItem('3', removeRepairLimitItem);

        // item repair cost
        GuiItem itemRepairCostItem = this.itemRepairCost.getItem(Material.ANVIL);
        pane.bindItem('4', itemRepairCostItem);

        // unit repair cost
        GuiItem unitRepairCostItem = this.unitRepairCost.getItem(Material.DIAMOND);
        pane.bindItem('5', unitRepairCostItem);

        // item rename cost
        GuiItem itemRenameCost = this.itemRenameCost.getItem(Material.NAME_TAG);
        pane.bindItem('6', itemRenameCost);

        // sacrifice illegal enchant cost
        GuiItem illegalCostItem = this.sacrificeIllegalEnchantCost.getItem(Material.ENCHANTED_BOOK);
        pane.bindItem('7', illegalCostItem);

        update();
    }

}
