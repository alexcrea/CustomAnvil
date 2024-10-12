package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EnumSettingGui<T extends Enum<T> & EnumSettingGui.ConfigurableEnum> extends AbstractSettingGui {

    private final EnumSettingFactory<T> holder;
    private final T before;
    private T now;

    /**
     * Create an item setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected EnumSettingGui(EnumSettingFactory<T> holder, T now) {
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
                "D000v0000",
                "B0000000S"
        );
    }


    public void prepareStaticItems(){
        prepareReturnToDefault();
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
        meta.setLore(Collections.singletonList("§7Default value is §e" + holder.getDefault().configurationGuiName()));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.getDefault();
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
        ItemStack displayedItem = now.configurationGuiItem();

        GuiItem resultItem = new GuiItem(displayedItem, selectNext(), CustomAnvil.instance);
        pane.bindItem('v', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if (now != holder.getDefault()) {
            returnToDefault = this.returnToDefault;
        } else {
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);

    }

    /**
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> selectNext() {
        return event -> {
            event.setCancelled(true);

            this.now = this.holder.next(this.now);

            updateValueDisplay();
            update();
        };

    }

    @Override
    public boolean onSave() {
        holder.config.acquiredWrite().set(holder.configPath, this.now.configName());
        holder.config.releaseWrite();

        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return !now.equals(before);
    }


    /**
     * A factory for an enum setting gui that hold setting's information.
     */
    public abstract static class EnumSettingFactory<T extends Enum<T> & ConfigurableEnum> extends SettingGuiFactory {
        @NotNull
        String title;
        @NotNull
        ValueUpdatableGui parent;

        /**
         * Constructor for an enum settings gui factory
         *
         * @param title      The title of the gui.
         * @param parent     Parent gui to go back when completed.
         * @param configPath Configuration path of this setting.
         * @param config     Configuration holder of this setting.
         */
        protected EnumSettingFactory(
                @NotNull String title, @NotNull ValueUpdatableGui parent,
                @NotNull String configPath, @NotNull ConfigHolder config) {
            super(configPath, config);
            this.title = title;
            this.parent = parent;

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
        @NotNull
        public abstract T getConfiguredValue();

        @NotNull
        public abstract List<String> getDisplayLore(T value);

        /**
         * @return Next value for a given enum
         */
        @NotNull
        public T next(@NotNull T now){
            Class<T> clazz = now.getDeclaringClass();
            T[] values = clazz.getEnumConstants();

            int index = now.ordinal();
            if(index == values.length - 1)
                return values[0];

            return values[index + 1];
        }

        /**
         * Get default value value
         * @return default value
         */
        @NotNull
        public abstract T getDefault();

        @Override
        public Gui create() {
            // Get value or default
            T now = getConfiguredValue();

            // create new gui
            return new EnumSettingGui<>(this, now);
        }

        /**
         * Create a new enum setting GuiItem.
         * This item will create and open an enum setting GUI from the factory.
         *
         * @param name Name of the display.
         * @return A formatted GuiItem that will create and open a GUI for the enum setting.
         */
        public GuiItem getItem(@NotNull Material material, @NotNull String name) {
            T value = getConfiguredValue();

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName(name);
            meta.setLore(getDisplayLore(value));
            meta.addItemFlags(ItemFlag.values());

            item.setItemMeta(meta);

            return GuiGlobalItems.openSettingGuiItem(item, this);
        }
    }

    public interface ConfigurableEnum {

        String configName();

        ItemStack configurationGuiItem();
        String configurationGuiName();


    }

}
