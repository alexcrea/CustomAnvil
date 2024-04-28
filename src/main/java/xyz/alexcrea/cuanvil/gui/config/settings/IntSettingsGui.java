package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit an int setting.
 */
public class IntSettingsGui extends AbstractSettingGui {

    protected final IntSettingFactory holder;
    protected final int before;
    protected int now;
    protected int step;

    /**
     * Create an int setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected IntSettingsGui(IntSettingFactory holder, int now) {
        super(3, holder.getTitle(), holder.parent);
        assert holder.steps.length > 0 && holder.steps.length <= 9;
        this.holder = holder;
        this.before = now;
        this.now = now;
        this.step = holder.steps[0];

        initStepsValue();
        prepareReturnToDefault();
        updateValueDisplay();
    }


    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                "abcdefghi",
                "D0-0v0+00",
                "B0000000S"
        );
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

    /**
     * Update item using the setting value to match the new value.
     */
    protected void updateValueDisplay() {

        PatternPane pane = getPane();

        // minus item
        GuiItem minusItem;
        if (now > holder.min) {
            int planned = Math.max(holder.min, now - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e" + now + " -> " + planned + " \u00A7r(\u00A7c-" + (now - planned) + "\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        } else {
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('-', minusItem);

        //plus item
        // may do a function to generalise ?
        GuiItem plusItem;
        if (now < holder.max) {
            int planned = Math.min(holder.max, now + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e" + now + " -> " + planned + " \u00A7r(\u00A7a+" + (planned - now) + "\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            plusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        } else {
            plusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('+', plusItem);

        // "result" display
        ItemStack resultPaper = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = resultPaper.getItemMeta();
        resultMeta.setDisplayName("\u00A7eValue: " + now);
        resultPaper.setItemMeta(resultMeta);
        GuiItem resultItem = new GuiItem(resultPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

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
     * @param planned Value to change current setting to.
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> updateNowConsumer(int planned) {
        return event -> {
            event.setCancelled(true);
            now = planned;
            updateValueDisplay();
            update();
        };
    }

    /**
     * Initialise step items.
     */
    protected void initStepsValue() {
        // Put background glass on the background of 'a' to 'b'
        GuiItem background = GuiGlobalItems.backgroundItem();
        PatternPane pane = getPane();

        for (char i = 'a'; i < (getMidStepChar() - 'a') * 2 + 1; i++) {
            pane.bindItem(i, background);
        }
        // Then update legit step values
        updateStepValue();
    }

    /**
     * Update steps items value.
     */
    protected void updateStepValue() {
        if (holder.steps.length <= 1) return;
        // We assume steps have a length of 2k+1 cause its more pretty
        char val = getMidStepChar();
        // Offset to start (not the best way to do it)
        val -= (char) ((holder.steps.length - 1) / 2);

        // Then place items
        PatternPane pane = getPane();
        for (int i = 0; i < holder.steps.length; i++) {
            pane.bindItem(val + i, stepGuiItem(i));
        }

    }

    /**
     * Step use lower case character from 'a' to a certain char.
     *
     * @return The middle value of the character set for steps.
     */
    protected char getMidStepChar() {
        return 'e';
    }

    /**
     * Create a step item from a step value.
     *
     * @param stepIndex the index of the step item.
     * @return A step item corresponding to its index value.
     */
    protected GuiItem stepGuiItem(int stepIndex) {
        int stepValue = holder.steps[stepIndex];

        // Get material properties
        Material stepMat;
        StringBuilder stepName = new StringBuilder("\u00A7");
        List<String> stepLore;
        Consumer<InventoryClickEvent> clickEvent;
        if (stepValue == step) {
            stepMat = Material.GREEN_STAINED_GLASS_PANE;
            stepName.append('a');
            stepLore = Collections.singletonList("\u00A77Value is changing by " + stepValue);
            clickEvent = GuiGlobalActions.stayInPlace;
        } else {
            stepMat = Material.RED_STAINED_GLASS_PANE;
            stepName.append('c');
            stepLore = Collections.singletonList("\u00A77Click here to change the value by " + stepValue);
            clickEvent = updateStepValue(stepValue);
        }
        stepName.append("Step of: ").append(stepValue);

        // Create item stack then gui item
        ItemStack item = new ItemStack(stepMat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(stepName.toString());
        meta.setLore(stepLore);
        item.setItemMeta(meta);

        return new GuiItem(item, clickEvent, CustomAnvil.instance);
    }

    /**
     * @param stepValue Value to change current step to.
     * @return A consumer to update the current step of this setting.
     */
    protected Consumer<InventoryClickEvent> updateStepValue(int stepValue) {
        return event -> {
            event.setCancelled(true);
            this.step = stepValue;
            updateStepValue();
            updateValueDisplay();
            update();
        };
    }

    @Override
    public boolean onSave() {
        holder.config.getConfig().set(holder.configPath, now);

        MetricsUtil.INSTANCE.notifyChange(this.holder.config, this.holder.configPath);
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
     * Create an int setting factory from setting's parameters.
     *
     * @param title       The title of the gui.
     * @param parent      Parent gui to go back when completed.
     * @param configPath  Configuration path of this setting.
     * @param config      Configuration holder of this setting.
     * @param displayLore Gui display item lore.
     * @param min         Minimum value of this setting.
     * @param max         Maximum value of this setting.
     * @param defaultVal  Default value if not found on the config.
     * @param steps       List of step the value can increment/decrement.
     *                    List's size should be between 1 (included) and 5 (included).
     *                    it is visually preferable to have an odd number of step.
     *                    If step only contain 1 value, no step item should be displayed.
     * @return A factory for an int setting gui.
     */
    public static IntSettingFactory intFactory(@NotNull String title, ValueUpdatableGui parent,
                                               String configPath, ConfigHolder config,
                                               @Nullable List<String> displayLore,
                                               int min, int max, int defaultVal, int... steps) {
        return new IntSettingFactory(
                title, parent,
                configPath, config,
                displayLore,
                min, max, defaultVal, steps);
    }

    /**
     * A factory for an int setting gui that hold setting's information.
     */
    public static class IntSettingFactory extends SettingGuiFactory {
        @NotNull
        String title;
        @NotNull
        ValueUpdatableGui parent;
        int min;
        int max;
        int defaultVal;
        int[] steps;

        @NotNull
        List<String> displayLore;

        /**
         * Constructor for an int setting gui factory.
         *
         * @param title       The title of the gui.
         * @param parent      Parent gui to go back when completed.
         * @param configPath  Configuration path of this setting.
         * @param config      Configuration holder of this setting.
         * @param displayLore Gui display item lore.
         * @param min         Minimum value of this setting.
         * @param max         Maximum value of this setting.
         * @param defaultVal  Default value if not found on the config.
         * @param steps       List of step the value can increment/decrement.
         *                    List's size should be between 1 (included) and 5 (included).
         *                    it is visually preferable to have an odd number of step.
         *                    If step only contain 1 value, no step item should be displayed.
         */
        protected IntSettingFactory(
                @NotNull String title, @NotNull ValueUpdatableGui parent,
                @NotNull String configPath, @NotNull ConfigHolder config,
                @Nullable List<String> displayLore,
                int min, int max, int defaultVal, int... steps) {
            super(configPath, config);
            this.title = title;
            this.parent = parent;
            this.min = min;
            this.max = max;
            this.defaultVal = defaultVal;
            this.steps = steps;

            if(displayLore == null){
                this.displayLore = Collections.emptyList();
            }else {
                this.displayLore = displayLore;
            }
        }

        /**
         * @return Get setting's gui title
         */
        @NotNull
        public String getTitle() {
            return title;
        }

        /**
         * @return The configured value for the associated setting.
         */
        public int getConfiguredValue() {
            return this.config.getConfig().getInt(this.configPath, this.defaultVal);
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            int now = getConfiguredValue();
            // create new gui
            return new IntSettingsGui(this, now);
        }

        /**
         * Create a new int setting GuiItem.
         * This item will create and open an int setting GUI from the factory.
         * The item will have its value written in the lore part of the item.
         *
         * @param itemMat Displayed material of the item.
         * @param name    Name of the item.
         * @return A formatted GuiItem that will create and open a GUI for the int setting.
         */
        public GuiItem getItem(
                @NotNull Material itemMat,
                @NotNull String name
        ) {
            // Get item properties
            int value = getConfiguredValue();
            StringBuilder itemName = new StringBuilder("\u00A7a").append(name);

            return GuiGlobalItems.createGuiItemFromProperties(this, itemMat, itemName, value, this.displayLore);
        }

        /**
         * Create a new int setting GuiItem.
         * This item will create and open an int setting GUI from the factory.
         * The item will have its value written in the lore part of the item.
         * Item's name will be the factory set title.
         *
         * @param itemMat Displayed material of the item.
         * @return A formatted GuiItem that will create and open a GUI for the int setting.
         */
        public GuiItem getItem(
                @NotNull Material itemMat
        ) {
            String configPath = GuiGlobalItems.getConfigNameFromPath(getConfigPath());
            return getItem(itemMat, CasedStringUtil.detectToUpperSpacedCase(configPath));
        }

    }

}

