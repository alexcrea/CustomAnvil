package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit a boolean setting.
 */
public class BoolSettingsGui extends AbstractSettingGui {

    private final BoolSettingFactory holder;
    private final boolean before;
    private boolean now;

    /**
     * Create a boolean setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected BoolSettingsGui(BoolSettingFactory holder, boolean now) {
        super(3, holder.getTitle(), holder.parent, holder.param);
        this.holder = holder;
        this.before = now;
        this.now = now;

        prepareReturnToDefault();
        updateValueDisplay();
    }


    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "D0-0v0+00",
                "B0000000S"
        );
    }

    protected GuiItem returnToDefault;

    /**
     * Prepare "return to default value" gui item.
     */
    protected void prepareReturnToDefault() {
        // Prepare default Value text
        String defaultValueLore;
        if(holder.defaultVal){
            defaultValueLore = "§aYes §7Is the default value";
        }else{
            defaultValueLore = "§cNo §7Is the default value";
        }

        // Create reset to default item
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§eReset to default value");
        meta.setLore(Collections.singletonList(defaultValueLore));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.defaultVal;
            updateValueDisplay();
            update();
        }, CustomAnvil.instance);
    }

    /**
     * Update item using the setting value to match the new value
     */
    protected void updateValueDisplay() {
        PatternPane pane = getPane();

        // Get displayed value for this config.
        String displayedName;
        Material displayedMat;
        if (now) {
            displayedName = "§aYes";
            displayedMat = Material.GREEN_TERRACOTTA;
        } else {
            displayedName = "§cNo";
            displayedMat = Material.RED_TERRACOTTA;
        }

        // create & set Value item
        ArrayList<Component> valueLore = new ArrayList<>();
        if(holder.displayLore != null){
            valueLore.addAll(ComponentUtil.INSTANCE.asComponents(holder.displayLore, holder.param));
            valueLore.add(Component.empty());
        }
        valueLore.addAll(MsgUI.INSTANCE.getSHARED_CLICK_TO_CHANGE().formatted());

        ItemStack valueItemStack = new ItemStack(displayedMat);
        ItemMeta valueMeta = valueItemStack.getItemMeta();
        assert valueMeta != null;

        valueMeta.setDisplayName(displayedName);//TODO MESSAGE ?
        ComponentUtil.INSTANCE.applyLore(valueLore, valueMeta);
        valueItemStack.setItemMeta(valueMeta);

        GuiItem resultItem = new GuiItem(valueItemStack, inverseNowConsumer(), CustomAnvil.instance);

        pane.bindItem('v', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if (now != holder.defaultVal) {
            returnToDefault = this.returnToDefault;
        } else {
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);

    }

    /**
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> inverseNowConsumer() {
        return event -> {
            event.setCancelled(true);
            now = !now;
            updateValueDisplay();
            update();
        };
    }

    @Override
    public boolean onSave() {
        holder.config.getConfig().set(holder.configPath, now);

        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return now != before;
    }

    /**
     * A factory for a boolean setting gui that hold setting's information.
     */
    public static class BoolSettingFactory extends SettingGuiFactory {
        @NotNull
        Message title;
        @NotNull
        ValueUpdatableGui parent;
        boolean defaultVal;

        @Nullable
        List<Message> displayLore;
        @Nullable
        Object param;

        /**
         * Constructor for a boolean setting gui factory.
         *
         * @param title        The title of the gui.
         * @param parent       Parent gui to go back when completed.
         * @param config       Configuration holder of this setting.
         * @param configPath   Configuration path of this setting.
         * @param defaultVal   Default value if not found on the config.
         * @param displayLore  Gui display item lore.
         */
        public BoolSettingFactory(
                @NotNull Message title, @NotNull ValueUpdatableGui parent,
                @NotNull ConfigHolder config, @NotNull String configPath,
                boolean defaultVal,
                @Nullable Object param, @Nullable Message... displayLore) {
            super(configPath, config);
            this.title = title;
            this.parent = parent;

            this.defaultVal = defaultVal;

            this.displayLore = displayLore == null ? null : Arrays.asList(displayLore);
            this.param = param;
        }

        /**
         * @return Get setting's gui title.
         */
        @NotNull
        public Message getTitle() {
            return title;
        }

        /**
         * @return The configured value for the associated setting.
         */
        public boolean getConfiguredValue() {
            return this.config.getConfig().getBoolean(this.configPath, this.defaultVal);
        }

        @Override
        public Gui create() {
            // Get current value or default
            boolean now = getConfiguredValue();
            // create new gui
            return new BoolSettingsGui(this, now);
        }

        /**
         * Create a new Boolean setting GuiItem.
         * This item will create and open a boolean setting GUI from the factory.
         * The item will have its value written in the lore part of the item.
         *
         * @param name Name of the item.
         * @param params parameters for the given name.
         * @return A formatted GuiItem that will create and open a GUI for the boolean setting.
         */
        public GuiItem getItem(
                @NotNull Message name,
                Object... params
        ){
            // Get item properties
            boolean value = getConfiguredValue();

            Material itemMat;
            Component itemName = name.formattedConcatenated(params);

            String finalValue;
            if (value) {
                itemMat = Material.GREEN_TERRACOTTA;
                finalValue = "<green>Yes";//TODO MESSAGE
            } else {
                itemMat = Material.RED_TERRACOTTA;
                finalValue = "<red>No";//TODO MESSAGE
            }

            return GuiGlobalItems.createGuiItemFromProperties(
                    this,
                    itemMat, itemName,
                    finalValue,
                    this.displayLore,
                    false,
                    this.param
            );
        }

        /**
         * Create a new boolean setting GuiItem.
         * This item will create and open a boolean setting GUI from the factory.
         * The item will have its value written in the lore part of the item.
         * Item's name will be the factory set title.
         *
         * @return A formatted GuiItem that will create and open a GUI for the boolean setting.
         */
        public GuiItem getItem(){
            // Get item properties
            String configPath = GuiGlobalItems.getConfigNameFromPath(getConfigPath());

            return getItem(MsgUI.INSTANCE.getSHARED_YELLOW_GET_ITEM(), CasedStringUtil.detectToUpperSpacedCase(configPath));
        }

    }

}

