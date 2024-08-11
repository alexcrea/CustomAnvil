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
        super(4, "§8Basic Config", CustomAnvil.instance);
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
        this.capAnvilCost = BoolSettingsGui.boolFactory("§8Cap Anvil Cost ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST,
                "§7All anvil cost will be capped to §aMax Anvil Cost§7 if enabled.",
                "§7In other words:",
                "§7For any anvil cost greater than §aMax Anvil Cost§7, Cost will be set to §aMax Anvil Cost§7.");
        // cap anvil cost not needed
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cCap Anvil Cost ?");
        meta.setLore(Collections.singletonList("§7This config only work if §cLimit Repair Cost§7 is disabled."));
        item.setItemMeta(meta);
        this.noCapRepairItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // repair cost item
        IntRange range = ConfigOptions.MAX_ANVIL_COST_RANGE;
        this.maxAnvilCost = IntSettingsGui.intFactory("§8Max Anvil Cost", this,
                ConfigOptions.MAX_ANVIL_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7Max cost the Anvil can get to.",
                        "§7Valid values include §e0 §7to §e1000§7.",
                        "§7Cost will be displayed as §cToo Expensive§7:",
                        "§7- If Cost is above §e39",
                        "§7- And §eReplace Too Expensive§7 is disabled"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_MAX_ANVIL_COST,
                1, 5, 10);
        // max anvil cost not needed
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cMax Anvil Cost");
        meta.setLore(Collections.singletonList("§7This config only work if §cLimit Repair Cost§7 is disabled."));
        item.setItemMeta(meta);
        this.noMaxCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // remove repair limit item
        this.removeAnvilCostLimit = BoolSettingsGui.boolFactory("§8Remove Anvil Cost Limit ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.REMOVE_ANVIL_COST_LIMIT, ConfigOptions.DEFAULT_REMOVE_ANVIL_COST_LIMIT,
                "§7Whether the anvil's cost limit should be removed entirely.",
                "§7The anvil will still visually display §cToo Expensive§7 if §eReplace Too Expensive§7 is disabled.",
                "§7However, the action will be completable if xp requirement is meet.");

        // replace too expensive item
        this.replaceTooExpensive = BoolSettingsGui.boolFactory("§8Replace Too Expensive ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.REPLACE_TOO_EXPENSIVE, ConfigOptions.DEFAULT_REPLACE_TOO_EXPENSIVE,
                getReplaceToExpensiveLore());

        // ------------
        // Cost config
        // ------------

        // item repair cost
        range = ConfigOptions.REPAIR_COST_RANGE;
        this.itemRepairCost = IntSettingsGui.intFactory("§8Item Repair Cost", this,
                ConfigOptions.ITEM_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7XP Level amount added to the anvil when the item",
                        "§7is repaired by another item of the same type."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_REPAIR_COST,
                1, 5, 10, 50, 100);

        // unit repair cost
        this.unitRepairCost = IntSettingsGui.intFactory("§8Unit Repair Cost", this,
                ConfigOptions.UNIT_REPAIR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7XP Level amount added to the anvil when the item is repaired by an §eunit§7.",
                        "§7For example: a Diamond on a Diamond Sword.",
                        "§7What's considered unit for what can be edited on the unit repair configuration."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_UNIT_REPAIR_COST,
                1, 5, 10, 50, 100);

        // item rename cost
        range = ConfigOptions.ITEM_RENAME_COST_RANGE;
        this.itemRenameCost = IntSettingsGui.intFactory("§8Rename Cost", this,
                ConfigOptions.ITEM_RENAME_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7XP Level amount added to the anvil when the item is renamed."
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_RENAME_COST,
                1, 5, 10, 50, 100);

        // sacrifice illegal enchant cost
        range = ConfigOptions.SACRIFICE_ILLEGAL_COST_RANGE;
        this.sacrificeIllegalEnchantCost = IntSettingsGui.intFactory("§8Sacrifice Illegal Enchant Cost", this,
                ConfigOptions.SACRIFICE_ILLEGAL_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7XP Level amount added to the anvil when a sacrifice enchantment",
                        "§7conflict With one of the left item enchantment"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST,
                1, 5, 10, 50, 100);

        // -------------
        // Color config
        // -------------

        // Allow us of color code
        this.allowColorCode = BoolSettingsGui.boolFactory("§8Allow Use Of Color Code ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.ALLOW_COLOR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOR_CODE,
                "§7Whether players can use color code.",
                "§7Color code a formatted like §a&a§7 and is used in the rename field of the anvil.",
                "§7Player may need permission to use color code if §ePlayer need permission to use color§7 is enabled.");

        // Allow us of hexadecimal color
        this.allowHexColor = BoolSettingsGui.boolFactory("§8Allow Use Of Hexadecimal Color ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.ALLOW_HEXADECIMAL_COLOR, ConfigOptions.DEFAULT_ALLOW_HEXADECIMAL_COLOR,
                "§7Whether players can use hexadecimal color.",
                "§7Color code a formatted like §2#012345 §7and is used in the rename field of the anvil.",
                "§7Player may need permission to use color code if §ePermission Needed For Color§7 is enabled.");

        // Permission needed for color
        this.permissionNeededForColor = BoolSettingsGui.boolFactory("§8Need Permission To Use Color ?", this,
                ConfigHolder.DEFAULT_CONFIG,
                ConfigOptions.PERMISSION_NEEDED_FOR_COLOR, ConfigOptions.DEFAULT_PERMISSION_NEEDED_FOR_COLOR,
                "§7Whether players should have permission to be able to use colors.",
                "§7Give player §eca.color.code§7 Permission to allow use of color code.",
                "§7Give player §eca.color.hex§7  Permission to allow use of hexadecimal color.");

        // Permission needed for color not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cNeed Permission To Use Color ?");
        meta.setLore(Arrays.asList("§7This config can do something only if one of the following config is enabled:",
                "§7- §aAllow Use Of Color Code",
                "§7- §aAllow Use Of Hexadecimal Color"));
        item.setItemMeta(meta);
        this.noPermissionNeededItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        // Cost of using color
        range = ConfigOptions.USE_OF_COLOR_COST_RANGE;
        this.useOfColorCost = IntSettingsGui.intFactory("§8Cost Of Using Color", this,
                ConfigOptions.USE_OF_COLOR_COST, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7XP level cost when using color code or hexadecimal color using the anvil.",
                        "§7conflict With one of the left item enchantment"
                ),
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_USE_OF_COLOR_COST,
                1, 5, 10, 50, 100);

        // Permission needed for color not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cCost Of Using Color");
        meta.setLore(Arrays.asList("§7This config can do something only if one of the following config is enabled:",
                "§7- §aAllow Use Of Color Code",
                "§7- §aAllow Use Of Hexadecimal Color"));
        item.setItemMeta(meta);
        this.noColorCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

    }

    @NotNull
    private String[] getReplaceToExpensiveLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Whenever anvil cost is above §e39§7 should display the true price and not §cToo Expensive§7.");
        lore.add("§7However, when bypassing §cToo Expensive§7, anvil price will be displayed as §aGreen§7.");
        lore.add("§7Even if cost is displayed as §aGreen§7:");
        lore.add("§7If the player do not have the required xp level, the action will not be completable.");

        if(!this.packetManager.getCanSetInstantBuild()){
            lore.add("");
            lore.add("§4/!\\§cCaution§4/!\\ §cYou need ProtocoLib installed and working or a newer version of this plugin for this to work.");
            lore.add("§cCurrently ProtocoLib is not detected.");
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
