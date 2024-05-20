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
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.packet.PacketManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Global config to edit basic basic settings.
 */
public class BasicConfigGui extends ValueUpdatableGui {

    private static BasicConfigGui INSTANCE;

    public static BasicConfigGui getInstance() {
        return INSTANCE;
    }

    private final PacketManager packetManager;
    /**
     * Constructor of this Global gui for basic settings.
     */
    public BasicConfigGui(PacketManager packetManager) {
        super(4, "\u00A78Basic Config", CustomAnvil.instance);
        INSTANCE = this;

        this.packetManager = packetManager;
        init();
    }

    PatternPane pane;

    /**
     * Initialise Basic gui
     */
    private void init() {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "0L0T0I0S0",
                "0C0R0U0r0",
                "B00000000"
        );
        pane = new PatternPane(0, 0, 9, 4, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.getInstance());
        GuiGlobalItems.addBackgroundItem(pane);

        prepareValues();
        updateGuiValues();
    }

    private BoolSettingsGui.BoolSettingFactory limitRepairFactory; // L character
    private IntSettingsGui.IntSettingFactory repairCostFactory; // C character
    private GuiItem notNeededLimitValueItem;

    private BoolSettingsGui.BoolSettingFactory removeRepairLimit; // R character
    private BoolSettingsGui.BoolSettingFactory replaceToExpensive; // T character

    private IntSettingsGui.IntSettingFactory itemRepairCost; // I character
    private IntSettingsGui.IntSettingFactory unitRepairCost; // U character
    private IntSettingsGui.IntSettingFactory itemRenameCost; // r character
    private IntSettingsGui.IntSettingFactory sacrificeIllegalEnchantCost; // S character

    /**
     * Prepare basic gui displayed items factory and static items..
     */
    protected void prepareValues() {
        // limit repair item
        this.limitRepairFactory = BoolSettingsGui.boolFactory("\u00A78Limit Repair Cost ?", this,
                ConfigOptions.LIMIT_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG, ConfigOptions.DEFAULT_LIMIT_REPAIR,
                "\u00A77Whether all anvil actions cost should be capped.",
                "\u00A77If true, all anvil repairs will max out at the value of \u00A7aLimit Repair Value\u00A77.");

        // repair cost item
        IntRange range = ConfigOptions.REPAIR_LIMIT_RANGE;
        this.repairCostFactory = IntSettingsGui.intFactory("\u00A78Repair Cost Limit", this,
                ConfigOptions.LIMIT_REPAIR_VALUE, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
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
                "\u00A77Whether the anvil's repair limit should be removed entirely.",
                "\u00A77The anvil will still visually display \u00A7cto expensive\u00A77.",
                "\u00A77However the action will be completable.");

        // replace to expensive item
        this.replaceToExpensive = BoolSettingsGui.boolFactory("\u00A78Replace To Expensive ?", this,
                ConfigOptions.REPLACE_TO_EXPENSIVE, ConfigHolder.DEFAULT_CONFIG, ConfigOptions.DEFAULT_REPLACE_TO_EXPENSIVE,
                getReplaceToExpensiveLore());

        // item repair cost
        range = ConfigOptions.REPAIR_COST_RANGE;
        this.itemRepairCost = IntSettingsGui.intFactory("\u00A78Item Repair Cost", this,
                ConfigOptions.ITEM_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
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
                        "\u00A77XP Level amount added to the anvil when a sacrifice enchantment",
                        "\u00A77conflict With one of the left item enchantment"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST,
                1, 5, 10, 50, 100);

    }

    @NotNull
    private String[] getReplaceToExpensiveLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("\u00A77Whenever anvil cost is above \u00A7e39\u00A77 should display the true price and not \u00A7cto expensive\u00A77.");
        lore.add("\u00A77However, when cost is above \u00A7e39\u00A77, anvil price will be displayed as \u00A7aGreen\u00A77,");
        lore.add("\u00A77even if player do not have the required xp level. But the action will not be completable.");

        if(!this.packetManager.isProtocoLibInstalled()){
            lore.add("");
            lore.add("\u00A74/!\\\u00A7cCaution/!\\ \u00A7cYou need ProtocoLib installed for this to work.");
        }

        String[] loreAsArray = new String[lore.size()];
        return lore.toArray(loreAsArray);
    }

    @Override
    public void updateGuiValues() {
        // limit repair item
        GuiItem limitRepairItem = this.limitRepairFactory.getItem();
        pane.bindItem('L', limitRepairItem);

        // rename cost item
        GuiItem limitRepairValueItem;
        if (this.limitRepairFactory.getConfiguredValue()) {
            limitRepairValueItem = this.repairCostFactory.getItem(Material.EXPERIENCE_BOTTLE);
        } else {
            limitRepairValueItem = this.notNeededLimitValueItem;
        }
        pane.bindItem('C', limitRepairValueItem);

        // remove repair limit item
        GuiItem removeRepairLimitItem = this.removeRepairLimit.getItem();
        pane.bindItem('R', removeRepairLimitItem);

        // replace to expensive item
        GuiItem replaceToExpensiveItem = this.replaceToExpensive.getItem();
        pane.bindItem('T', replaceToExpensiveItem);


        // item repair cost
        GuiItem itemRepairCostItem = this.itemRepairCost.getItem(Material.ANVIL);
        pane.bindItem('I', itemRepairCostItem);

        // unit repair cost
        GuiItem unitRepairCostItem = this.unitRepairCost.getItem(Material.DIAMOND);
        pane.bindItem('U', unitRepairCostItem);

        // item rename cost
        GuiItem itemRenameCost = this.itemRenameCost.getItem(Material.NAME_TAG);
        pane.bindItem('r', itemRenameCost);

        // sacrifice illegal enchant cost
        GuiItem illegalCostItem = this.sacrificeIllegalEnchantCost.getItem(Material.ENCHANTED_BOOK);
        pane.bindItem('S', illegalCostItem);

        update();
    }

}
