package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import kotlin.ranges.IntRange;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Global config to edit basic basic settings.
 */
public class BasicConfigGui extends ChestGui implements ValueUpdatableGui {

    private static BasicConfigGui INSTANCE = null;

    @Nullable
    public static BasicConfigGui getInstance() {
        return INSTANCE;
    }

    private final PacketManager packetManager;
    /**
     * Constructor of this Global gui for basic settings.
     */
    public BasicConfigGui(PacketManager packetManager) {
        super(4, "\u00A78Basic Config", CustomAnvil.instance);
        if(INSTANCE == null) INSTANCE = this;

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
                "LT0I0S0cp",
                "CR0U0r0hP",
                "B00000000"
        );
        pane = new PatternPane(0, 0, 9, 4, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.getInstance());
        GuiGlobalItems.addBackgroundItem(pane);

        prepareValues();
        updateGuiValues();
    }

    private BoolSettingsGui.BoolSettingFactory capAnvilCost; // L character
    private GuiItem noCapRepairItem;
    private IntSettingsGui.IntSettingFactory maxAnvilCost; // C character
    private GuiItem noMaxCostItem;

    private BoolSettingsGui.BoolSettingFactory removeAnvilCostLimit; // R character
    private BoolSettingsGui.BoolSettingFactory replaceTooExpensive; // T character

    private IntSettingsGui.IntSettingFactory itemRepairCost; // I character
    private IntSettingsGui.IntSettingFactory unitRepairCost; // U character
    private IntSettingsGui.IntSettingFactory itemRenameCost; // r character
    private IntSettingsGui.IntSettingFactory sacrificeIllegalEnchantCost; // S character

    private BoolSettingsGui.BoolSettingFactory allowColorCode; // c character
    private BoolSettingsGui.BoolSettingFactory allowHexColor; // h character

    private BoolSettingsGui.BoolSettingFactory permissionNeededForColor; // p character
    private GuiItem noPermissionNeededItem;
    private IntSettingsGui.IntSettingFactory useOfColorCost; // P character
    private GuiItem noColorCostItem;

    /**
     * Prepare basic gui displayed items factory and static items..
     */
    protected void prepareValues() {
        // cap anvil cost
        this.capAnvilCost = BoolSettingsGui.boolFactory("\u00A78Cap Anvil Cost ?", this,
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
        this.maxAnvilCost = IntSettingsGui.intFactory("\u00A78Max Anvil Cost", this,
                ConfigOptions.MAX_ANVIL_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "\u00A77Max cost the Anvil can get to.",
                        "\u00A77Valid values include \u00A7e0 \u00A77to \u00A7e1000\u00A77.",
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

        // ------------
        // Cost config
        // ------------

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

        // -------------
        // Color config
        // -------------

        // Allow us of color code
        this.allowColorCode = BoolSettingsGui.boolFactory("\u00A78Allow Use Of Color Code ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.ALLOW_COLOR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOR_CODE,
                "\u00A77Whether players can use color code.",
                "\u00A77Color code a formatted like \u00A7a&a\u00A77 and is used in the rename field of the anvil.",
                "\u00A77Player may need permission to use color code if \u00A7ePlayer need permission to use color\u00A77 is enabled.");

        // Allow us of hexadecimal color
        this.allowHexColor = BoolSettingsGui.boolFactory("\u00A78Allow Use Of Hexadecimal Color ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.ALLOW_HEXADECIMAL_COLOR, ConfigOptions.DEFAULT_ALLOW_HEXADECIMAL_COLOR,
                "\u00A77Whether players can use hexadecimal color.",
                "\u00A77Color code a formatted like \u00A72#012345 \u00A77and is used in the rename field of the anvil.",
                "\u00A77Player may need permission to use color code if \u00A7ePermission Needed For Color\u00A77 is enabled.");

        // Permission needed for color
        this.permissionNeededForColor = BoolSettingsGui.boolFactory("\u00A78Need Permission To Use Color ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.PERMISSION_NEEDED_FOR_COLOR, ConfigOptions.DEFAULT_PERMISSION_NEEDED_FOR_COLOR,
                "\u00A77Whether players should have permission to be able to use colors.",
                "\u00A77Give player \u00A7eca.color.code\u00A77 Permission to allow use of color code.",
                "\u00A77Give player \u00A7eca.color.hex\u00A77  Permission to allow use of hexadecimal color.");

        // Permission needed for color not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7cNeed Permission To Use Color ?");
        meta.setLore(Arrays.asList("\u00A77This config can do something only if one of the following config is enabled:",
                "\u00A77- \u00A7aAllow Use Of Color Code",
                "\u00A77- \u00A7aAllow Use Of Hexadecimal Color"));
        item.setItemMeta(meta);
        this.noPermissionNeededItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        // Cost of using color
        range = ConfigOptions.USE_OF_COLOR_COST_RANGE;
        this.useOfColorCost = IntSettingsGui.intFactory("\u00A78Cost Of Using Color", this,
                ConfigOptions.USE_OF_COLOR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "\u00A77XP level cost when using color code or hexadecimal color using the anvil.",
                        "\u00A77conflict With one of the left item enchantment"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_USE_OF_COLOR_COST,
                1, 5, 10, 50, 100);

        // Permission needed for color not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7cCost Of Using Color");
        meta.setLore(Arrays.asList("\u00A77This config can do something only if one of the following config is enabled:",
                "\u00A77- \u00A7aAllow Use Of Color Code",
                "\u00A77- \u00A7aAllow Use Of Hexadecimal Color"));
        item.setItemMeta(meta);
        this.noColorCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

    }

    @NotNull
    private String[] getReplaceToExpensiveLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("\u00A77Whenever anvil cost is above \u00A7e39\u00A77 should display the true price and not \u00A7cToo Expensive\u00A77.");
        lore.add("\u00A77However, when bypassing \u00A7cToo Expensive\u00A77, anvil price will be displayed as \u00A7aGreen\u00A77.");
        lore.add("\u00A77Even if cost is displayed as \u00A7aGreen\u00A77:");
        lore.add("\u00A77If the player do not have the required xp level, the action will not be completable.");

        if(!this.packetManager.getCanSetInstantBuild()){
            lore.add("");
            lore.add("\u00A74/!\\\u00A7cCaution\u00A74/!\\ \u00A7cYou need ProtocoLib installed and working or a newer version of this plugin for this to work.");
            lore.add("\u00A7cCurrently ProtocoLib is not detected.");
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
            capAnvilCostItem = this.capAnvilCost.getItem("Cap Anvil Cost");
            maxAnvilCostItem = this.maxAnvilCost.getItem(Material.EXPERIENCE_BOTTLE, "Max Anvil Cost");
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
        GuiItem itemRenameCostItem = this.itemRenameCost.getItem(Material.NAME_TAG);
        pane.bindItem('r', itemRenameCostItem);

        // sacrifice illegal enchant cost
        GuiItem illegalCostItem = this.sacrificeIllegalEnchantCost.getItem(Material.ENCHANTED_BOOK);
        pane.bindItem('S', illegalCostItem);

        // allow color code
        GuiItem allowColorCodeItem = this.allowColorCode.getItem();
        pane.bindItem('c', allowColorCodeItem);

        // allow hex color
        GuiItem allowHexColorItem = this.allowHexColor.getItem();
        pane.bindItem('h', allowHexColorItem);

        // True if player could place color
        if(ConfigOptions.INSTANCE.getRenameColorPossible()){
            // use permission for color
            GuiItem permissionNeededItem = this.permissionNeededForColor.getItem();
            pane.bindItem('p', permissionNeededItem);

            // using color cost
            GuiItem useColorCostItem = this.useOfColorCost.getItem(Material.EXPERIENCE_BOTTLE, "Use color");
            pane.bindItem('P', useColorCostItem);
        }else{
            pane.bindItem('p', this.noPermissionNeededItem);
            pane.bindItem('P', this.noColorCostItem);
        }


        update();
    }

    @Override
    public Gui getConnectedGui() {
        return this;
    }

}
