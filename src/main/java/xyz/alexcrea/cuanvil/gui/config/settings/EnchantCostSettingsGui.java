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
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit an enchantment cost setting.
 * May be considered as a 2 int setting.
 */
public class EnchantCostSettingsGui extends IntSettingsGui {

    protected final static String ITEM_PATH = ".item";
    protected final static String BOOK_PATH = ".book";

    private int beforeBook;
    private int nowBook;

    /**
     * Create an enchantment cost setting config gui.
     *
     * @param holder  Configuration factory of this setting.
     * @param nowItem The defined value of this setting item's value.
     */
    protected EnchantCostSettingsGui(EnchantCostSettingFactory holder, int nowItem) {
        super(holder, nowItem);

        this.step = holder.steps[0];

        initStaticItem();
    }

    /**
     * Initialise step items.
     * Also bypassed init sequence to initialise books value
     * as it used as one of the first call from the init sequence of the gui
     */
    @Override
    protected void initStepsValue() {
        super.initStepsValue();

        int nowBook = ((EnchantCostSettingFactory) this.holder).getConfiguredBookValue();
        this.beforeBook = nowBook;
        this.nowBook = nowBook;
    }

    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                "abc13-v+0",
                "D0012MVP0",
                "B0010000S"
        );
    }

    /**
     * Initialise item that should not be updated late.
     */
    private void initStaticItem() {
        PatternPane pane = getPane();

        // book display
        ItemStack bookItemstack = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = bookItemstack.getItemMeta();
        assert bookMeta != null;

        bookMeta.setDisplayName("\u00A7aCost of an Enchantment by Book");
        bookMeta.setLore(Arrays.asList(
                "\u00A77Cost per result item level of an sacrifice enchantment",
                "\u00A77Only apply if sacrificed item \u00A7cis \u00A77a book"));
        bookItemstack.setItemMeta(bookMeta);

        // sword display
        ItemStack swordItemstack = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta swordMeta = swordItemstack.getItemMeta();
        assert swordMeta != null;

        swordMeta.addItemFlags(ItemFlag.values());
        swordMeta.setDisplayName("\u00A7aCost of an Enchantment by Item");
        swordMeta.setLore(Arrays.asList(
                "\u00A77Cost per result item level of an sacrifice enchantment",
                "\u00A77Only apply if sacrificed item \u00A7cis not \u00A77a book"));
        swordItemstack.setItemMeta(swordMeta);

        pane.bindItem('1', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        pane.bindItem('2', new GuiItem(bookItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
        pane.bindItem('3', new GuiItem(swordItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
    }

    @Override
    protected void prepareReturnToDefault() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        // assume holder is an instance of EnchantCostSettingFactory
        EnchantCostSettingFactory holder = (EnchantCostSettingFactory) this.holder;

        meta.setDisplayName("\u00A7eReset to default value");
        meta.setLore(Arrays.asList(
                "\u00A77Default item  value is: \u00A7e" + holder.defaultVal,
                "\u00A77Default book value is: \u00A7e" + holder.defaultBookVal));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            nowBook = holder.defaultBookVal;
            now = holder.defaultVal;
            updateValueDisplay();
            update();
        }, CustomAnvil.instance);
    }

    @Override
    protected void updateValueDisplay() {
        super.updateValueDisplay();
        PatternPane pane = getPane();

        // assume holder is an instance of EnchantCostSettingFactory
        EnchantCostSettingFactory holder = ((EnchantCostSettingFactory) this.holder);

        int nowBook = this.nowBook;

        // minus item
        GuiItem minusItem;
        if (nowBook > holder.min) {
            int planned = Math.max(holder.min, nowBook - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName("\u00A7e" + nowBook + " \u00A7f-> \u00A7e" + planned + " \u00A7r(\u00A7c-" + (nowBook - planned) + "\u00A7r)");
            meta.setLore(Collections.singletonList(AbstractSettingGui.CLICK_LORE));
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowBookConsumer(planned), CustomAnvil.instance);
        } else {
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('M', minusItem);

        //plus item
        GuiItem plusItem;
        if (nowBook < holder.max) {
            int planned = Math.min(holder.max, nowBook + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName("\u00A7e" + nowBook + " \u00A7f-> \u00A7e" + planned + " \u00A7r(\u00A7a+" + (planned - nowBook) + "\u00A7r)");
            meta.setLore(Collections.singletonList(AbstractSettingGui.CLICK_LORE));
            item.setItemMeta(meta);

            plusItem = new GuiItem(item, updateNowBookConsumer(planned), CustomAnvil.instance);
        } else {
            plusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('P', plusItem);

        // now value display
        ItemStack nowPaper = new ItemStack(Material.PAPER);
        ItemMeta nowMeta = nowPaper.getItemMeta();
        assert nowMeta != null;

        nowMeta.setDisplayName("\u00A7fValue: \u00A7e" + nowBook);
        if(!holder.displayLore.isEmpty()){
            nowMeta.setLore(holder.displayLore);
        }

        nowPaper.setItemMeta(nowMeta);

        GuiItem resultItem = new GuiItem(nowPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('V', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if (now != holder.defaultVal || nowBook != holder.defaultBookVal) {
            returnToDefault = this.returnToDefault;
        } else {
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);


    }

    /**
     * @param planned Value to change current book cost setting to.
     * @return A consumer to update the current book cost setting's value.
     */
    protected Consumer<InventoryClickEvent> updateNowBookConsumer(int planned) {
        return event -> {
            event.setCancelled(true);
            nowBook = planned;
            updateValueDisplay();
            update();
        };
    }

    @Override
    protected char getMidStepChar() {
        return 'b';
    }

    @Override
    public boolean onSave() {
        holder.config.getConfig().set(holder.configPath + ITEM_PATH, now);
        holder.config.getConfig().set(holder.configPath + BOOK_PATH, nowBook);

        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return super.hadChange() || nowBook != beforeBook;
    }

    /**
     * Create an int setting factory from setting's parameters.
     *
     * @param title          The title of the gui.
     * @param parent         Parent gui to go back when completed.
     * @param config         Configuration holder of this setting.
     * @param configPath     Configuration path of this setting.
     * @param displayLore    Gui display item lore.
     * @param min            Minimum value of this setting.
     * @param max            Maximum value of this setting.
     * @param defaultItemVal Default item value if not found on the config.
     * @param defaultBookVal Default book value if not found on the config.
     * @param steps          List of step the value can increment/decrement.
     *                       List's size should be between 1 (included) and 3 (included).
     *                       it is visually preferable to have an odd number of step.
     *                       If step only contain 1 value, no step item should be displayed.
     * @return A factory for an enchant cost setting gui.
     */
    public static EnchantCostSettingFactory enchantCostFactory(
            @NotNull String title, @NotNull ValueUpdatableGui parent,
            @NotNull ConfigHolder config, @NotNull String configPath,
            @Nullable List<String> displayLore,
            int min, int max, int defaultItemVal, int defaultBookVal,
            int... steps) {
        return new EnchantCostSettingFactory(
                title, parent,
                configPath, config,
                displayLore,
                min, max, defaultItemVal, defaultBookVal, steps);
    }

    /**
     * A factory for an enchantment cost setting gui that hold setting's information.
     */
    public static class EnchantCostSettingFactory extends IntSettingsGui.IntSettingFactory {

        int defaultBookVal;

        /**
         * Constructor for an enchantment cost setting gui factory.
         *
         * @param title          The title of the gui.
         * @param parent         Parent gui to go back when completed.
         * @param configPath     Configuration path of this setting.
         * @param config         Configuration holder of this setting.
         * @param displayLore    Gui display item lore.
         * @param min            Minimum value of this setting.
         * @param max            Maximum value of this setting.
         * @param defaultItemVal Default item value if not found on the config.
         * @param defaultBookVal Default book value if not found on the config.
         * @param steps          List of step the value can increment/decrement.
         *                       List's size should be between 1 (included) and 3 (included).
         *                       it is visually preferable to have an odd number of step.
         *                       If step only contain 1 value, no step item should be displayed.
         */
        protected EnchantCostSettingFactory(
                @NotNull String title, ValueUpdatableGui parent,
                @NotNull String configPath, @NotNull ConfigHolder config,
                @Nullable List<String> displayLore,
                int min, int max, int defaultItemVal, int defaultBookVal,
                int... steps) {

            super(title, parent,
                    configPath, config,
                    displayLore,
                    min, max, defaultItemVal, steps);
            this.defaultBookVal = defaultBookVal;
        }

        /**
         * @return The configured value for the enchant setting item value.
         */
        @Override
        public int getConfiguredValue() {
            return this.config.getConfig().getInt(this.configPath + ITEM_PATH, this.defaultVal);
        }

        /**
         * @return The configured value for the enchant setting book value.
         */
        public int getConfiguredBookValue() {
            return this.config.getConfig().getInt(this.configPath + BOOK_PATH, this.defaultBookVal);
        }

        @Override
        public Gui create() {
            // Get value or default
            int nowItem = getConfiguredValue();
            // create new gui
            return new EnchantCostSettingsGui(this, nowItem);
        }

        public List<String> getDisplayLore() {
            return this.displayLore;
        }
    }

}