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

    private BoolSettingsGui.BoolSettingFactory capAnvilCostFactory; // L character
    private GuiItem noCapRepairItem;
    private IntSettingsGui.IntSettingFactory maxAnvilCostFactory; // C character
    private GuiItem noMaxCostItem;

    private BoolSettingsGui.BoolSettingFactory removeAnvilCostLimit; // R character
    private BoolSettingsGui.BoolSettingFactory replaceTooExpensive; // T character

    private IntSettingsGui.IntSettingFactory itemRepairCost; // I character
    private IntSettingsGui.IntSettingFactory unitRepairCost; // U character
    private IntSettingsGui.IntSettingFactory itemRenameCost; // r character
    private IntSettingsGui.IntSettingFactory sacrificeIllegalEnchantCost; // S character

    /**
     * Prepare basic gui displayed items factory and static items..
     */
    protected void prepareValues() {
        // cap anvil cost
        this.capAnvilCostFactory = BoolSettingsGui.boolFactory("\u00A78Cap Anvil Cost ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST,
                "\u00A77All anvil cost will be capped to \u00A7aMax Anvil Cost\u00A77 if enabled.",
                "\u00A77In other words:",
                "\u00A77For any anvil cost greater than \u00A7aMax Anvil Cost\u00A77, Cost will be set to \u00A7aMax Anvil Cost\u00A77.");
        // cap anvil cost not needed
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7cCap Anvil Cost ?");
        meta.setLore(Collections.singletonList("\u00A77This config only work if \u00A7cLimit Repair Cost\u00A77 is disabled."));
        item.setItemMeta(meta);
        this.noCapRepairItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // repair cost item
        IntRange range = ConfigOptions.MAX_ANVIL_COST_RANGE;
        this.maxAnvilCostFactory = IntSettingsGui.intFactory("\u00A78Max Anvil Cost", this,
                ConfigOptions.MAX_ANVIL_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "\u00A77Max cost the Anvil can get to.",
                        "\u00A77Valid values include \u00A7e1 \u00A77to \u00A7e255\u00A77.",
                        "\u00A77Cost will be displayed as \u00A7cToo Expensive\u00A77:",
                        "\u00A77- If Cost is above \u00A7e39",
                        "\u00A77- And \u00A7eReplace Too Expensive\u00A77 is disabled"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_MAX_ANVIL_COST,
                1, 5, 10);
        // max anvil cost not needed
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7cMax Anvil Cost");
        meta.setLore(Collections.singletonList("\u00A77This config only work if \u00A7cLimit Repair Cost\u00A77 is disabled."));
        item.setItemMeta(meta);
        this.noMaxCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // remove repair limit item
        this.removeAnvilCostLimit = BoolSettingsGui.boolFactory("\u00A78Remove Anvil Cost Limit ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.REMOVE_ANVIL_COST_LIMIT, ConfigOptions.DEFAULT_REMOVE_ANVIL_COST_LIMIT,
                "\u00A77Whether the anvil's cost limit should be removed entirely.",
                "\u00A77The anvil will still visually display \u00A7cToo Expensive\u00A77 if \u00A7eReplace Too Expensive\u00A77 is disabled.",
                "\u00A77However, the action will be completable if xp requirement is meet.");

        // replace too expensive item
        this.replaceTooExpensive = BoolSettingsGui.boolFactory("\u00A78Replace Too Expensive ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.REPLACE_TOO_EXPENSIVE, ConfigOptions.DEFAULT_REPLACE_TOO_EXPENSIVE,
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
        lore.add("\u00A77Whenever anvil cost is above \u00A7e39\u00A77 should display the true price and not \u00A7cToo Expensive\u00A77.");
        lore.add("\u00A77However, when bypassing \u00A7cToo Expensive\u00A77, anvil price will be displayed as \u00A7aGreen\u00A77.");
        lore.add("\u00A77Even if cost is displayed as \u00A7aGreen\u00A77:");
        lore.add("\u00A77If the player do not have the required xp level, the action will not be completable.");

        if(!this.packetManager.isProtocoLibInstalled()){
            lore.add("");
            lore.add("\u00A74/!\\\u00A7cCaution/!\\ \u00A7cYou need ProtocoLib installed for this to work.");
        }

        String[] loreAsArray = new String[lore.size()];
        return lore.toArray(loreAsArray);
    }

    @Override
    public void updateGuiValues() {
        // limit and cap anvil cost item
        GuiItem capAnvilCostItem;
        GuiItem maxAnvilCostItem;
        if (!this.removeAnvilCostLimit.getConfiguredValue()) {
            capAnvilCostItem = this.capAnvilCostFactory.getItem("Cap Anvil Cost");
            maxAnvilCostItem = this.maxAnvilCostFactory.getItem(Material.EXPERIENCE_BOTTLE, "Max Anvil Cost");
        } else {
            capAnvilCostItem = this.noCapRepairItem;
            maxAnvilCostItem = this.noMaxCostItem;
        }

        pane.bindItem('L', capAnvilCostItem);
        pane.bindItem('C', maxAnvilCostItem);

        // remove repair limit item
        GuiItem removeRepairLimitItem = this.removeAnvilCostLimit.getItem("Remove Anvil Cost Limit");
        pane.bindItem('R', removeRepairLimitItem);

        // replace too expensive item
        GuiItem replaceToExpensiveItem = this.replaceTooExpensive.getItem();
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
