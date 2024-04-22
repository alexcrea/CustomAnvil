package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

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

        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_TERRACOTTA, this);
        getPane().bindItem('s', temporaryLeave);
    }


    protected GuiItem returnToDefault;

    /**
     * Prepare "return to default value" gui item.
     */
    protected void prepareReturnToDefault() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("\u00A7eReset to default value");
        meta.setLore(Collections.singletonList("\u00A77Default value is: " + holder.defaultVal));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.defaultVal;
            updateValueDisplay();
            update();
        }, CustomAnvil.instance);
    }

    protected final static List<String> CLICK_LORE = Collections.singletonList("\u00A77Click Here with an item to change the value");

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

            valueMeta.setDisplayName("\u00A74NO ITEM SET");
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
    protected Consumer<InventoryClickEvent> setItemAsCursor() { //TODO redo consumer
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

        MetricsUtil.INSTANCE.notifyChange(this.holder.config, this.holder.configPath);
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
    public static ItemSettingGui.ItemSettingFactory itemFactory(@NotNull String title, ValueUpdatableGui parent,
                                                                 String configPath, ConfigHolder config,
                                                                ItemStack defaultVal,
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
        ValueUpdatableGui parent;
        ItemStack defaultVal;
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
                @NotNull String title, ValueUpdatableGui parent,
                String configPath, ConfigHolder config,
                ItemStack defaultVal,
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

        public List<String> getDisplayLore() {
            return displayLore;
        }

        @Override
        public AbstractSettingGui create() {
            // Get current value or default
            ItemStack now = getConfiguredValue();
            // create new gui
            return new ItemSettingGui(this, now);
        }

    }
}
