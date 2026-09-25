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
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.LockedObjectProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit an item setting.
 */
@NotNullByDefault
public class ItemSettingGui extends AbstractSettingGui {

    private final ItemSettingFactory holder;
    private final @Nullable ItemStack before;
    private @Nullable ItemStack now;

    /**
     * Create an item setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected ItemSettingGui(ItemSettingFactory holder, @Nullable ItemStack now) {
        super(3, holder.getTitle(), holder.parent, holder.param);
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
                "D000v000s",
                "B0000000S"
        );
    }


    public void prepareStaticItems() {
        prepareReturnToDefault();

        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_STAINED_GLASS_PANE, this);
        getPane().bindItem('s', temporaryLeave);
    }


    protected @UnknownNullability GuiItem returnToDefault;

    /**
     * Prepare "return to default value" gui item.
     */
    protected void prepareReturnToDefault() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("<yellow>Reset to default value");
        meta.setLore(Collections.singletonList("<gray>Default value is <yellow>" + holder.defaultVal));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.defaultVal;
            updateValueDisplay();
            update();
        }, CustomAnvil.instance);
    }

    protected final static List<String> CLICK_LORE = Collections.singletonList("<gray>Click Here with an item to change the value");

    /**
     * Update item using the setting value to match the new value
     */
    protected void updateValueDisplay() {
        PatternPane pane = getPane();

        // Get displayed value for this config.
        ItemStack displayedItem;
        if(this.now != null) {
            displayedItem = this.now.clone();
        } else {
            displayedItem = new ItemStack(Material.BARRIER);
            ItemMeta valueMeta = displayedItem.getItemMeta();
            assert valueMeta != null;

            valueMeta.setDisplayName("<dark_red*NO ITEM SET");
            valueMeta.setLore(CLICK_LORE);

            displayedItem.setItemMeta(valueMeta);
        }

        GuiItem resultItem = new GuiItem(displayedItem, setItemAsCursor(), CustomAnvil.instance);
        pane.bindItem('v', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if(now != holder.defaultVal) {
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
        try(var lock = holder.getHolder().write) {
            var config = lock.get();
            config.getConfig().set(holder.configPath, this.now);

            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                return config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }
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
     * A factory for an item setting gui that hold setting's information.
     */
    public static class ItemSettingFactory extends SettingGuiFactory {
        final Message title;
        final ValueUpdatableGui parent;
        @Nullable
        final ItemStack defaultVal;
        final List<Message> displayLore;
        @Nullable
        final Object param;

        /**
         * Constructor for an item setting gui factory.
         *
         * @param title       The title of the gui.
         * @param parent      Parent gui to go back when completed.
         * @param configPath  Configuration path of this setting.
         * @param holder      Configuration holder of this setting.
         * @param defaultVal  Default value if not found on the config.
         * @param displayLore Gui display item lore.
         */
        public ItemSettingFactory(
                Message title, ValueUpdatableGui parent,
                String configPath,
                LockedObjectProvider<? extends ConfigHolder> holder,
                @Nullable ItemStack defaultVal,
                @Nullable Object param, Message... displayLore) {
            super(configPath, holder);
            this.title = title;
            this.parent = parent;

            this.defaultVal = defaultVal;
            this.displayLore = Arrays.asList(displayLore);
            this.param = param;
        }

        /**
         * @return Get setting's gui title.
         */
        public Message getTitle() {
            return title;
        }

        /**
         * @return The configured value for the associated setting.
         */
        @Nullable
        public ItemStack getConfiguredValue() {
            try(var lock = getHolder().read) {
                return lock.get().getConfig().getItemStack(this.configPath, this.defaultVal);
            }
        }

        public List<Message> getDisplayLore() {
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
        public GuiItem getItem(String name) {
            ItemStack item = getConfiguredValue();
            if(item == null || item.getType().isAir()) {
                item = new ItemStack(Material.BARRIER);
            } else {
                item = item.clone();
            }
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            //TODO MESSAGE name ?
            meta.setDisplayName("<green>" + name);
            ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(getDisplayLore(), param), meta);
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
