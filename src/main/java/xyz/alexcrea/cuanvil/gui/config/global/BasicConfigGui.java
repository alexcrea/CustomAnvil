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
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.WorkPenaltyTypeSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;

/**
 * Global config to edit basic settings.
 */
@NotNullByDefault
public class BasicConfigGui extends ChestGui implements ValueUpdatableGui {

    //TODO #130 part 3
    private static @Nullable BasicConfigGui INSTANCE = null;

    @Nullable
    public static BasicConfigGui getInstance() {
        return INSTANCE;
    }

    private final PacketManager packetManager;

    /**
     * Constructor of this Global gui for basic settings.
     */
    public BasicConfigGui(PacketManager packetManager) {
        super(4, MsgUI.INSTANCE.getBASIC_TITLE().textHolder(), CustomAnvil.instance);
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
                "LT0IWS0cp",
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

    //TODO #130 part 3
    private @Nullable BoolSettingsGui.BoolSettingFactory capAnvilCost; // L character
    private @Nullable GuiItem noCapRepairItem;
    private @Nullable IntSettingsGui.IntSettingFactory maxAnvilCost; // C character
    private @Nullable GuiItem noMaxCostItem;

    private @Nullable BoolSettingsGui.BoolSettingFactory removeAnvilCostLimit; // R character
    private @Nullable BoolSettingsGui.BoolSettingFactory replaceTooExpensive; // T character

    private @Nullable IntSettingsGui.IntSettingFactory itemRepairCost; // I character
    private @Nullable IntSettingsGui.IntSettingFactory unitRepairCost; // U character
    private @Nullable IntSettingsGui.IntSettingFactory itemRenameCost; // r character
    private @Nullable IntSettingsGui.IntSettingFactory sacrificeIllegalEnchantCost; // S character

    private @Nullable BoolSettingsGui.BoolSettingFactory allowColourCode; // c character
    private @Nullable BoolSettingsGui.BoolSettingFactory allowHexColour; // h character

    private @Nullable BoolSettingsGui.BoolSettingFactory permissionNeededForColour; // p character
    private @Nullable GuiItem noPermissionNeededItem;
    private @Nullable IntSettingsGui.IntSettingFactory useOfColourCost; // P character
    private @Nullable GuiItem noColourCostItem;

    /**
     * Prepare basic gui displayed items factory and static items...
     */
    protected void prepareValues() {
        // cap anvil cost
        this.capAnvilCost = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_CAP_ANVIL_COST_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST,
                null, MsgUI.INSTANCE.getBASIC_CAP_ANVIL_COST_DESCRIPTION()
        );
        // cap anvil cost not needed
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getBASIC_CAP_ANVIL_COST_DISABLED_TITLE());
        ComponentUtil.INSTANCE.applyLore(MsgUI.INSTANCE.getBASIC_CAP_ANVIL_COST_DISABLED_DESCRIPTION().formatted(), meta);

        item.setItemMeta(meta);
        this.noCapRepairItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // repair cost item
        IntRange range = ConfigOptions.MAX_ANVIL_COST_RANGE;
        this.maxAnvilCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_MAX_ANVIL_COST_TITLE(), this,
                ConfigOptions.MAX_ANVIL_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_MAX_ANVIL_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_MAX_ANVIL_COST,
                1, 5, 10
        );
        // max anvil cost not needed
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getBASIC_MAX_ANVIL_COST_DISABLED_TITLE());
        ComponentUtil.INSTANCE.applyLore(MsgUI.INSTANCE.getBASIC_MAX_ANVIL_COST_DISABLED_DESCRIPTION().formatted(), meta);
        item.setItemMeta(meta);
        this.noMaxCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        // remove repair limit item
        this.removeAnvilCostLimit = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_REMOVE_COST_LIMIT_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.REMOVE_ANVIL_COST_LIMIT, ConfigOptions.DEFAULT_REMOVE_ANVIL_COST_LIMIT,
                null, MsgUI.INSTANCE.getBASIC_REMOVE_COST_LIMIT_DESCRIPTION()
        );

        // replace too expensive item
        this.replaceTooExpensive = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_REPLACE_TOO_EXPENSIVE_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.REPLACE_TOO_EXPENSIVE, ConfigOptions.DEFAULT_REPLACE_TOO_EXPENSIVE,
                null, getReplaceToExpensiveLore()
        );

        // ------------
        // Cost config
        // ------------

        // item repair cost
        range = ConfigOptions.REPAIR_COST_RANGE;
        this.itemRepairCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_ITEM_REPAIR_COST_TITLE(), this,
                ConfigOptions.ITEM_REPAIR_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_ITEM_REPAIR_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_REPAIR_COST,
                1, 5, 10, 50, 100
        );

        // unit repair cost
        this.unitRepairCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_UNIT_REPAIR_COST_TITLE(), this,
                ConfigOptions.UNIT_REPAIR_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_UNIT_REPAIR_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_UNIT_REPAIR_COST,
                1, 5, 10, 50, 100
        );

        // item rename cost
        range = ConfigOptions.ITEM_RENAME_COST_RANGE;
        this.itemRenameCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_ITEM_RENAME_COST_TITLE(), this,
                ConfigOptions.ITEM_RENAME_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_ITEM_RENAME_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_ITEM_RENAME_COST,
                1, 5, 10, 50, 100
        );

        // sacrifice illegal enchant cost
        range = ConfigOptions.SACRIFICE_ILLEGAL_COST_RANGE;
        this.sacrificeIllegalEnchantCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_SACRIFICE_ILLEGAL_COST_TITLE(), this,
                ConfigOptions.SACRIFICE_ILLEGAL_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_SACRIFICE_ILLEGAL_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST,
                1, 5, 10, 50, 100
        );

        // -------------
        // Colour config
        // -------------

        // Allow us of colour code
        this.allowColourCode = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_COLOUR_CODE_LIMIT_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.ALLOW_COLOUR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOUR_CODE,
                null, MsgUI.INSTANCE.getBASIC_COLOUR_CODE_LIMIT_DESCRIPTION()
        );

        // Allow us of hexadecimal colour
        this.allowHexColour = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_COLOUR_HEX_LIMIT_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.ALLOW_HEXADECIMAL_COLOUR, ConfigOptions.DEFAULT_ALLOW_HEXADECIMAL_COLOUR,
                null, MsgUI.INSTANCE.getBASIC_COLOUR_HEX_LIMIT_DESCRIPTION()
        );

        // Permission needed for colour
        this.permissionNeededForColour = new BoolSettingsGui.BoolSettingFactory(
                MsgUI.INSTANCE.getBASIC_COLOUR_PERMISSION_TITLE(), this,
                ConfigHolder.DEFAULT,
                ConfigOptions.PERMISSION_NEEDED_FOR_COLOUR, ConfigOptions.DEFAULT_PERMISSION_NEEDED_FOR_COLOUR,
                null, MsgUI.INSTANCE.getBASIC_COLOUR_PERMISSION_DESCRIPTION()
        );

        // Permission needed for colour not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getBASIC_COLOUR_PERMISSION_DISABLED_TITLE());
        ComponentUtil.INSTANCE.applyLore(MsgUI.INSTANCE.getBASIC_COLOUR_PERMISSION_DISABLED_DESCRIPTION().formatted(), meta);
        item.setItemMeta(meta);
        this.noPermissionNeededItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        // Cost of using colour
        range = ConfigOptions.USE_OF_COLOUR_COST_RANGE;
        this.useOfColourCost = new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getBASIC_COLOUR_COST_TITLE(), this,
                ConfigOptions.USE_OF_COLOUR_COST, ConfigHolder.DEFAULT,
                MsgUI.INSTANCE.getBASIC_COLOUR_COST_DESCRIPTION(), null,
                range.getFirst(), range.getLast(),
                ConfigOptions.DEFAULT_USE_OF_COLOUR_COST,
                1, 5, 10, 50, 100
        );

        // Permission needed for colour not necessary
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getBASIC_COLOUR_COST_DISABLED_TITLE());
        ComponentUtil.INSTANCE.applyLore(MsgUI.INSTANCE.getBASIC_COLOUR_COST_DISABLED_DESCRIPTION().formatted(), meta);

        item.setItemMeta(meta);
        this.noColourCostItem = new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

    }

    private Message[] getReplaceToExpensiveLore() {
        ArrayList<Message> lore = new ArrayList<>();
        lore.add(MsgUI.INSTANCE.getBASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION());

        if(!this.packetManager.getCanSetInstantBuild())
            lore.add(MsgUI.INSTANCE.getBASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION_NO_NMS());

        Message[] loreAsArray = new Message[lore.size()];
        return lore.toArray(loreAsArray);
    }

    @Override
    public void updateGuiValues() {
        // limit and cap anvil cost item
        GuiItem capAnvilCostItem;
        GuiItem maxAnvilCostItem;

        //TODO #130 part 3
        assert this.removeAnvilCostLimit != null;
        assert this.capAnvilCost != null;
        assert this.maxAnvilCost != null;
        assert this.noCapRepairItem != null;
        assert this.noMaxCostItem != null;
        assert this.replaceTooExpensive != null;
        assert this.itemRepairCost != null;
        assert this.unitRepairCost != null;
        assert this.itemRenameCost != null;
        assert this.sacrificeIllegalEnchantCost != null;
        assert this.allowColourCode != null;
        assert this.allowHexColour != null;
        assert this.permissionNeededForColour != null;
        assert this.useOfColourCost != null;
        assert this.noPermissionNeededItem != null;
        assert this.noColourCostItem != null;

        if(!this.removeAnvilCostLimit.getConfiguredValue()) {
            capAnvilCostItem = this.capAnvilCost.getItem(
                    MsgUI.INSTANCE.getBASIC_CAP_ANVIL_COST_ITEM()
            );
            maxAnvilCostItem = this.maxAnvilCost.getItem(
                    Material.EXPERIENCE_BOTTLE,
                    MsgUI.INSTANCE.getBASIC_MAX_ANVIL_COST_ITEM()
            );
        } else {
            capAnvilCostItem = this.noCapRepairItem;
            maxAnvilCostItem = this.noMaxCostItem;
        }

        pane.bindItem('L', capAnvilCostItem);
        pane.bindItem('C', maxAnvilCostItem);

        // remove repair limit item
        GuiItem removeRepairLimitItem = this.removeAnvilCostLimit.getItem(
                MsgUI.INSTANCE.getBASIC_REMOVE_COST_LIMIT_ITEM()
        );
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

        // work penalty type
        GuiItem workPenaltyType = WorkPenaltyTypeSettingGui.getDisplayItem(
                this,
                Material.DAMAGED_ANVIL,
                MsgUI.INSTANCE.getBASIC_WORK_PENALTY_ITEM()
        );
        pane.bindItem('W', workPenaltyType);

        // allow colour code
        GuiItem allowColourCodeItem = this.allowColourCode.getItem();
        pane.bindItem('c', allowColourCodeItem);

        // allow hex colour
        GuiItem allowHexColourItem = this.allowHexColour.getItem();
        pane.bindItem('h', allowHexColourItem);

        // True if player could place colour
        if(ConfigOptions.INSTANCE.getRenameColourPossible()) {
            // use permission for colour
            GuiItem permissionNeededItem = this.permissionNeededForColour.getItem();
            pane.bindItem('p', permissionNeededItem);

            // using colour cost
            GuiItem useColorCostItem = this.useOfColourCost.getItem(
                    Material.EXPERIENCE_BOTTLE,
                    MsgUI.INSTANCE.getBASIC_COLOUR_COST_ITEM()
            );
            pane.bindItem('P', useColorCostItem);
        } else {
            pane.bindItem('p', this.noPermissionNeededItem);
            pane.bindItem('P', this.noColourCostItem);
        }

        update();
    }

    @Override
    public Gui getConnectedGui() {
        return this;
    }

}
