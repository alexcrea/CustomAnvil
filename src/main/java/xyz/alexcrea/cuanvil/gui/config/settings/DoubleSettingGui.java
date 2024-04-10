package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DoubleSettingGui extends AbstractSettingGui {

    protected final DoubleSettingFactory holder;
    protected final boolean asPercentage;
    @NotNull
    protected final BigDecimal before;
    @NotNull
    protected BigDecimal now;
    protected BigDecimal step;

    /**
     * Create a double setting config gui.
     *
     * @param holder Configuration factory of this setting.
     * @param now    The defined value of this setting.
     */
    protected DoubleSettingGui(DoubleSettingFactory holder, @NotNull BigDecimal now, boolean asPercentage) {
        super(3, holder.getTitle(), holder.parent);
        assert holder.steps.length > 0 && holder.steps.length <= 9;
        this.holder = holder;
        this.asPercentage = asPercentage;

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
        meta.setLore(Collections.singletonList("\u00A77Default value is: " + displayValue(holder.defaultVal)));
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


        //minus item
        GuiItem minusItem;
        if (now.compareTo(holder.min) > 0) {
            BigDecimal planned = holder.min.max(now.subtract(step));

            minusItem = getSetValueItem(Material.RED_TERRACOTTA, planned, "\u00A7c-");
        } else {
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('-', minusItem);

        //plus item
        GuiItem plusItem;
        if (now.compareTo(holder.max) < 0) {
            BigDecimal planned = holder.max.min(now.add(step));

            plusItem = getSetValueItem(Material.GREEN_TERRACOTTA, planned, "\u00A7a+");
        } else {
            plusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('+', plusItem);

        // "result" display
        ItemStack resultPaper = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = resultPaper.getItemMeta();
        resultMeta.setDisplayName("\u00A7eValue: " + displayValue(now));
        resultPaper.setItemMeta(resultMeta);
        GuiItem resultItem = new GuiItem(resultPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('v', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if (!now.equals(holder.defaultVal)) {
            returnToDefault = this.returnToDefault;
        } else {
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);


    }

    private GuiItem getSetValueItem(Material mat, BigDecimal planned, String numberPrefix){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A7e" + displayValue(now) + " -> " + displayValue(planned)
                + " \u00A7r(" + numberPrefix + (displayValue(planned.subtract(now).abs()) + "\u00A7r)"));
        meta.setLore(AbstractSettingGui.CLICK_LORE);
        item.setItemMeta(meta);

        return new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
    }

    /**
     * @param planned Value to change current setting to.
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> updateNowConsumer(BigDecimal planned) {
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
        BigDecimal stepValue = holder.steps[stepIndex];

        // Get material properties
        Material stepMat;
        StringBuilder stepName = new StringBuilder("\u00A7");
        List<String> stepLore;
        Consumer<InventoryClickEvent> clickEvent;
        if (Objects.equals(stepValue, step)) {
            stepMat = Material.GREEN_STAINED_GLASS_PANE;
            stepName.append('a');
            stepLore = Collections.singletonList("\u00A77Value is changing by " + displayValue(stepValue));
            clickEvent = GuiGlobalActions.stayInPlace;
        } else {
            stepMat = Material.RED_STAINED_GLASS_PANE;
            stepName.append('c');
            stepLore = Collections.singletonList("\u00A77Click here to change the value by " + displayValue(stepValue));
            clickEvent = updateStepValue(stepValue);
        }
        stepName.append("Step of ").append(displayValue(stepValue));

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
    protected Consumer<InventoryClickEvent> updateStepValue(BigDecimal stepValue) {
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
        holder.config.getConfig().set(holder.configPath, now.doubleValue());

        MetricsUtil.INSTANCE.notifyChange(this.holder.config, this.holder.configPath);
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return !now.equals(before);
    }

    private static final BigDecimal PERCENTAGE_OFFSET = BigDecimal.valueOf(100);
    public String displayValue(BigDecimal value){
        return displayValue(value, this.asPercentage);
    }

    public static String displayValue(BigDecimal value, boolean isAsPercentage){
        if(isAsPercentage){
            return value.multiply(PERCENTAGE_OFFSET).setScale(value.scale()-2, RoundingMode.HALF_UP) + "%";
        }
        return value.toString();
    }

    /**
     * Create a double setting factory from setting's parameters.
     *
     * @param title        The title of the gui.
     * @param parent       Parent gui to go back when completed.
     * @param configPath   Configuration path of this setting.
     * @param config       Configuration holder of this setting.
     * @param scale        The scale of the decimal.
     * @param asPercentage If we should display the value as a %.
     * @param min          Minimum value of this setting.
     * @param max          Maximum value of this setting.
     * @param defaultVal   Default value if not found on the config.
     * @param steps        List of step the value can increment/decrement.
     *                     List's size should be between 1 (included) and 5 (included).
     *                     it is visually preferable to have an odd number of step.
     *                     If step only contain 1 value, no step item should be displayed.
     * @return A factory for a double setting gui.
     */
    public static DoubleSettingFactory doubleFactory(@NotNull String title, ValueUpdatableGui parent,
                                                     String configPath, ConfigHolder config,
                                                     int scale, boolean asPercentage,
                                                     double min, double max, double defaultVal, double... steps) {
        return new DoubleSettingFactory(
                title, parent,
                configPath, config,
                scale, asPercentage,
                min, max, defaultVal, steps);
    }

    /**
     * A factory for a double setting gui that hold setting's information.
     */
    public static class DoubleSettingFactory extends SettingGuiFactory {
        @NotNull
        String title;
        ValueUpdatableGui parent;

        int scale;
        boolean asPercentage;
        BigDecimal min;
        BigDecimal max;
        BigDecimal defaultVal;
        BigDecimal[] steps;

        /**
         * Constructor for a double setting gui factory.
         *
         * @param title        The title of the gui.
         * @param parent       Parent gui to go back when completed.
         * @param configPath   Configuration path of this setting.
         * @param config       Configuration holder of this setting.
         * @param scale        The scale of the decimal.
         * @param asPercentage If we should display the value as a %.
         * @param min          Minimum value of this setting.
         * @param max          Maximum value of this setting.
         * @param defaultVal   Default value if not found on the config.
         * @param steps        List of step the value can increment/decrement.
         *                     List's size should be between 1 (included) and 5 (included).
         *                     it is visually preferable to have an odd number of step.
         *                     If step only contain 1 value, no step item should be displayed.
         */
        protected DoubleSettingFactory(
                @NotNull String title, ValueUpdatableGui parent,
                String configPath, ConfigHolder config,
                int scale, boolean asPercentage,
                double min, double max, double defaultVal, double... steps) {
            super(configPath, config);
            this.title = title;
            this.parent = parent;
            this.scale = scale;
            this.asPercentage = asPercentage;
            this.min = BigDecimal.valueOf(min).setScale(scale, RoundingMode.HALF_UP);
            this.max = BigDecimal.valueOf(max).setScale(scale, RoundingMode.HALF_UP);
            this.defaultVal = BigDecimal.valueOf(defaultVal).setScale(scale, RoundingMode.HALF_UP);

            this.steps = new BigDecimal[steps.length];
            for (int i = 0; i < steps.length; i++) {
                this.steps[i] = BigDecimal.valueOf(steps[i]).setScale(scale, RoundingMode.HALF_UP);
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
        public BigDecimal getConfiguredValue() {
            ConfigurationSection section = this.config.getConfig();
            if(section.isDouble(this.configPath)){
                return BigDecimal.valueOf(section.getDouble(this.configPath)).setScale(2, RoundingMode.HALF_UP);
            }
            return this.defaultVal;
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            BigDecimal now = getConfiguredValue();
            // create new gui
            return new DoubleSettingGui(this, now, this.asPercentage);
        }


        public GuiItem getItem(Material itemMat, String name){
            // Get item properties
            BigDecimal value = getConfiguredValue();
            StringBuilder itemName = new StringBuilder("\u00A7a").append(name);

            return GuiGlobalItems.createGuiItemFromProperties(this, itemMat, itemName, displayValue(value, this.asPercentage));
        }

        public GuiItem getItem(Material itemMat){
            // Get item properties
            String configPath = GuiGlobalItems.getConfigNameFromPath(getConfigPath());

            return getItem(itemMat, CasedStringUtil.detectToUpperSpacedCase(configPath));
        }

    }

}