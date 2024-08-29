package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit an item setting.
 */
public class ItemSettingGui extends AbstractSettingGui {

    private final ItemSettingFactory holder;
    private final ItemStack before;
    private ItemStack now;

    /**
     * Create an item setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected ItemSettingGui(ItemSettingFactory holder, ItemStack now) {
        super(3, holder.getTitle(), holder.parent);
        this.holder = holder;
        this.before = now;
        this.now = now;

        prepareStaticItems();
        updateValueDisplay();
    }

    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "D0-0v0+0s",
                "B0000000S"
        );
    }


    public void prepareStaticItems(){
        prepareReturnToDefault();

        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_STAINED_GLASS_PANE, this);
        getPane().bindItem('s', temporaryLeave);
    }


    protected GuiItem returnToDefault;

    /**
     * Prepare "return to default value" gui item.
     */
    protected void prepareReturnToDefault() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§eReset to default value");
        meta.setLore(Collections.singletonList("§7Default value is §e" + holder.defaultVal));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.defaultVal;
            updateValueDisplay();
            update();
        }, CustomAnvil.instance);
    }

    protected final static List<String> CLICK_LORE = Collections.singletonList("§7Click Here with an item to change the value");

    /**
     * Update item using the setting value to match the new value
     */
    protected void updateValueDisplay() {
        PatternPane pane = getPane();

        // Get displayed value for this config.
        ItemStack displayedItem;
        if(this.now != null){
            displayedItem = this.now.clone();
        }else{
            displayedItem = new ItemStack(Material.BARRIER);
            ItemMeta valueMeta = displayedItem.getItemMeta();
            assert valueMeta != null;

            valueMeta.setDisplayName("§4NO ITEM SET");
            valueMeta.setLore(CLICK_LORE);

            displayedItem.setItemMeta(valueMeta);
        }

        GuiItem resultItem = new GuiItem(displayedItem, setItemAsCursor(), CustomAnvil.instance);
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
    protected Consumer<InventoryClickEvent> setItemAsCursor() {
        return event -> {
            event.setCancelled(true);

            HumanEntity player = event.getWhoClicked();
            ItemStack cursor = player.getItemOnCursor();

            if(cursor.getType().isAir()) return;

            this.now = cursor;

            updateValueDisplay();
            update();
        };
    }

    @Override
    public boolean onSave() {
        holder.config.getConfig().set(holder.configPath, this.now);

        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        if(now == null) {
            return before != null;
        }

        return !now.equals(before);
    }

    /**
     * Create aa item setting factory from setting's parameters.
     *
     * @param title       The title of the gui.
     * @param parent      Parent gui to go back when completed.
     * @param configPath  Configuration path of this setting.
     * @param config      Configuration holder of this setting.
     * @param defaultVal  Default value if not found on the config.
     * @param displayLore Gui display item lore.
     * @return A factory for an item setting gui.
     */
    public static ItemSettingGui.ItemSettingFactory itemFactory(@NotNull String title, @NotNull ValueUpdatableGui parent,
                                                                @NotNull String configPath, @NotNull ConfigHolder config,
                                                                @Nullable ItemStack defaultVal,
                                                                String... displayLore) {
        return new ItemSettingGui.ItemSettingFactory(
                title, parent,
                configPath, config,
                defaultVal, displayLore);
    }

    /**
     * A factory for an item setting gui that hold setting's information.
     */
    public static class ItemSettingFactory extends SettingGuiFactory {
        @NotNull
        String title;
        @NotNull
        ValueUpdatableGui parent;
        @Nullable
        ItemStack defaultVal;
        @NotNull
        List<String> displayLore;

        /**
         * Constructor for an item setting gui factory.
         *
         * @param title       The title of the gui.
         * @param parent      Parent gui to go back when completed.
         * @param configPath  Configuration path of this setting.
         * @param config      Configuration holder of this setting.
         * @param defaultVal  Default value if not found on the config.
         * @param displayLore Gui display item lore.
         */
        protected ItemSettingFactory(
                @NotNull String title, @NotNull ValueUpdatableGui parent,
                @NotNull String configPath, @NotNull ConfigHolder config,
                @Nullable ItemStack defaultVal,
                String... displayLore) {
            super(configPath, config);
            this.title = title;
            this.parent = parent;

            this.defaultVal = defaultVal;
            this.displayLore = Arrays.asList(displayLore);
        }

        /**
         * @return Get setting's gui title.
         */
        @NotNull
        public String getTitle() {
            return title;
        }

        /**
         * @return The configured value for the associated setting.
         */
        public ItemStack getConfiguredValue() {
            return this.config.getConfig().getItemStack(this.configPath, this.defaultVal);
        }

        @NotNull
        public List<String> getDisplayLore() {
            return this.displayLore;
        }

        @Override
        public Gui create() {
            // Get current value or default
            ItemStack now = getConfiguredValue();
            // create new gui
            return new ItemSettingGui(this, now);
        }

        /**
         * Create a new item setting GuiItem.
         * This item will create and open an item setting GUI from the factory.
         * Item's name will be the factory set title.
         *
         * @param name Name of the item.
         * @return A formatted GuiItem that will create and open a GUI for the item setting.
         */
        public GuiItem getItem(@NotNull String name) {
            ItemStack item = getConfiguredValue();
            if(item == null || item.getType().isAir()){
                item = new ItemStack(Material.BARRIER);
            }else{
                item = item.clone();
            }
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName("§a" + name);
            meta.setLore(getDisplayLore());
            meta.addItemFlags(ItemFlag.values());

            item.setItemMeta(meta);

            return GuiGlobalItems.openSettingGuiItem(item, this);
        }

        /**
         * Create a new item setting GuiItem.
         * This item will create and open an item setting GUI from the factory.
         * Item's name will be the factory set title.
         *
         * @return A formatted GuiItem that will create and open a GUI for the item setting.
         */
        public GuiItem getItem() {
            String configPath = GuiGlobalItems.getConfigNameFromPath(getConfigPath());
            return getItem(CasedStringUtil.detectToUpperSpacedCase(configPath));
        }

    }
}
