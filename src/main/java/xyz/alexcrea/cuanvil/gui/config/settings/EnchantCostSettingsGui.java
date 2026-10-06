package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.LockedObjectProvider;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * An instance of a gui used to edit an enchantment cost setting.
 * May be considered as a 2 int setting.
 */
@NotNullByDefault
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

        ComponentUtil.setMessageName(bookMeta, MsgUI.ENCHANT_COST_BOOK_NAME);
        ComponentUtil.applyLore(bookMeta, MsgUI.ENCHANT_COST_BOOK_LORE);
        bookItemstack.setItemMeta(bookMeta);

        // sword display
        ItemStack swordItemstack = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta swordMeta = swordItemstack.getItemMeta();
        assert swordMeta != null;

        swordMeta.addItemFlags(ItemFlag.values());
        ComponentUtil.setMessageName(swordMeta, MsgUI.ENCHANT_COST_ITEM_NAME);
        ComponentUtil.applyLore(swordMeta, MsgUI.ENCHANT_COST_ITEM_LORE);
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

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_RESET);
        ComponentUtil.applyLore(Arrays.asList(
                MsgUI.ENCHANT_COST_DEFAULT_ITEM.formattedConcatenated(holder.defaultVal),
                MsgUI.ENCHANT_COST_DEFAULT_BOOK.formattedConcatenated(holder.defaultBookVal)
        ), meta);
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
        if(nowBook > holder.min) {
            int planned = Math.max(holder.min, nowBook - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            ComponentUtil.setMessageName(meta, MsgUI.SETTING_VALUE_ARROW_NAME_PREFIXED, nowBook, planned, "<red>-", (nowBook - planned));
            ComponentUtil.INSTANCE.applyLore(MsgUI.SHARED_CLICK_TO_CHANGE.formatted(), meta);
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowBookConsumer(planned), CustomAnvil.instance);
        } else {
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('M', minusItem);

        //plus item
        GuiItem plusItem;
        if(nowBook < holder.max) {
            int planned = Math.min(holder.max, nowBook + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            ComponentUtil.setMessageName(meta, MsgUI.SETTING_VALUE_ARROW_NAME_PREFIXED, nowBook, planned, "<green>+", (planned - nowBook));
            ComponentUtil.INSTANCE.applyLore(MsgUI.SHARED_CLICK_TO_CHANGE.formatted(), meta);
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

        ComponentUtil.setMessageName(nowMeta, MsgUI.SETTING_VALUE_NAME, nowBook);
        if(holder.displayLore != null) {
            ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(holder.displayLore, holder.param), nowMeta);
        }

        nowPaper.setItemMeta(nowMeta);

        GuiItem resultItem = new GuiItem(nowPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('V', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if(now != holder.defaultVal || nowBook != holder.defaultBookVal) {
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
        try(var lock = holder.getHolder().write) {
            var config = lock.get();
            config.getConfig().set(holder.configPath + ITEM_PATH, now);
            config.getConfig().set(holder.configPath + BOOK_PATH, nowBook);

            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                return config.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return super.hadChange() || nowBook != beforeBook;
    }

    /**
     * A factory for an enchantment cost setting gui that hold setting's information.
     */
    public static class EnchantCostSettingFactory extends IntSettingsGui.IntSettingFactory {

        final int defaultBookVal;
        final CAEnchantment enchantment;

        /**
         * Constructor for an enchantment cost setting gui factory.
         *
         * @param title       The title of the gui.
         * @param parent      Parent gui to go back when completed.
         * @param configPath  Configuration path of this setting.
         * @param holder      Configuration holder of this setting.
         * @param displayLore Gui display item lore.
         * @param min         Minimum value of this setting.
         * @param max         Maximum value of this setting.
         * @param enchantment Enchantment to change the cost to
         * @param steps       List of step the value can increment/decrement.
         *                    List's size should be between 1 (included) and 3 (included).
         *                    it is visually preferable to have an odd number of step.
         *                    If step only contain 1 value, no step item should be displayed.
         */
        public EnchantCostSettingFactory(
                Message title, ValueUpdatableGui parent,
                String configPath, LockedObjectProvider<? extends ConfigHolder> holder,
                @Nullable Message displayLore, @Nullable Object param,
                CAEnchantment enchantment,
                int min, int max, int... steps) {

            super(title, parent,
                    configPath, holder,
                    displayLore, param,
                    min, max, enchantment.defaultRarity().itemValue(),
                    steps);

            this.defaultBookVal = enchantment.defaultRarity().bookValue();
            this.enchantment = enchantment;
        }

        /**
         * @return The configured value for the enchant setting item value.
         */
        @Override
        public int getConfiguredValue() {
            return ConfigOptions.INSTANCE.enchantmentValue(enchantment, false, false);
        }

        /**
         * @return The configured value for the enchant setting book value.
         */
        public int getConfiguredBookValue() {
            return ConfigOptions.INSTANCE.enchantmentValue(enchantment, false, true);
        }

        @Override
        public Gui create() {
            // Get value or default
            int nowItem = getConfiguredValue();
            // create new gui
            return new EnchantCostSettingsGui(this, nowItem);
        }

        @Nullable
        public List<Message> getDisplayLore() {
            return this.displayLore;
        }
    }

}