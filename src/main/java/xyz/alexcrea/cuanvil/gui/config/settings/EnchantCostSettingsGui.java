package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.util.Arrays;
import java.util.function.Consumer;

public class EnchantCostSettingsGui extends IntSettingsGui {

    protected final static String ITEM_PATH = ".item";
    protected final static String BOOK_PATH = ".book";

    private int beforeBook;
    private int nowBook;

    protected EnchantCostSettingsGui(EnchantCostSettingFactory holder, int nowItem) {
        super(holder, nowItem);

        this.step = holder.steps[0];

        initStaticItem();
    }

    @Override
    protected void initStepsValue() {
        super.initStepsValue();

        int nowBook = ((EnchantCostSettingFactory)this.holder).getConfiguredBookValue();
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

    private void initStaticItem() {
        PatternPane pane = getPane();

        // book display
        ItemStack bookItemstack = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = bookItemstack.getItemMeta();

        bookMeta.setDisplayName("\u00A7aCost of an Enchantment by Book");
        bookMeta.setLore(Arrays.asList(
                "\u00A77Cost per result item level of an sacrifice enchantment",
                "\u00A77Only apply if sacrificed item \u00A7cis \u00A77a book"));bookItemstack.setItemMeta(bookMeta);

        // sword display
        ItemStack swordItemstack = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta swordMeta = swordItemstack.getItemMeta();
        swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        swordMeta.setDisplayName("\u00A7aCost of an Enchantment by Item");
        swordMeta.setLore(Arrays.asList(
                "\u00A77Cost per result item level of an sacrifice enchantment",
                "\u00A77Only apply if sacrificed item \u00A7cis not \u00A77a book"));
        swordItemstack.setItemMeta(swordMeta);

        pane.bindItem('1', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        pane.bindItem('2', new GuiItem(bookItemstack,  GuiGlobalActions.stayInPlace, CustomAnvil.instance));
        pane.bindItem('3', new GuiItem(swordItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
    }

    @Override
    protected void prepareReturnToDefault(){
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();

        // assume holder is an instance of EnchantCostSettingFactory
        EnchantCostSettingFactory holder = (EnchantCostSettingFactory) this.holder;

        meta.setDisplayName("\u00A7eReset to default value");
        meta.setLore(Arrays.asList(
                "\u00A77Default item  value is: " + holder.defaultVal,
                "\u00A77Default book value is: " + holder.defaultBookVal));
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
    protected void updateValueDisplay(){
        super.updateValueDisplay();
        PatternPane pane = getPane();

        // assume holder is an instance of EnchantCostSettingFactory
        EnchantCostSettingFactory holder = ((EnchantCostSettingFactory) this.holder);

        int nowBook = this.nowBook;

        // minus item
        GuiItem minusItem;
        if(nowBook > holder.min){
            int planned = Math.max(holder.min, nowBook - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+nowBook+" -> "+planned + " \u00A7r(\u00A7c-"+(nowBook-planned)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowBookConsumer(planned), CustomAnvil.instance);
        }else{
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('M', minusItem);

        //plus item
        GuiItem plusItem;
        if(nowBook < holder.max){
            int planned = Math.min(holder.max, nowBook + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+nowBook+" -> "+planned + " \u00A7r(\u00A7a+"+(planned-nowBook)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            plusItem = new GuiItem(item, updateNowBookConsumer(planned), CustomAnvil.instance);
        }else{
            plusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('P', plusItem);

        // "result" display
        ItemStack resultPaper = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = resultPaper.getItemMeta();
        resultMeta.setDisplayName("\u00A7eValue: "+nowBook);
        resultPaper.setItemMeta(resultMeta);
        GuiItem resultItem = new GuiItem(resultPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('V', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if(now != holder.defaultVal || nowBook != holder.defaultBookVal){
            returnToDefault = this.returnToDefault;
        }else{
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);


    }

    protected Consumer<InventoryClickEvent> updateNowBookConsumer(int planned){
        return event->{
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
        holder.config.getConfig().set(holder.configPath+ITEM_PATH, now);
        holder.config.getConfig().set(holder.configPath+BOOK_PATH, nowBook);

        MetricsUtil.INSTANCE.notifyChange(this.holder.config, this.holder.configPath);
        if(TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
            return holder.config.saveToDisk(TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return super.hadChange() || nowBook != beforeBook;
    }

    public static EnchantCostSettingFactory enchFactory(@NotNull String title, ValueUpdatableGui parent,
                                                        String configPath, ConfigHolder config,
                                                        int min, int max, int defaultItemVal, int defaultBookVal,
                                                        int... steps){
        return new EnchantCostSettingFactory(
                title,parent,
                configPath, config,
                min, max, defaultItemVal, defaultBookVal, steps);
    }


    public static class EnchantCostSettingFactory extends IntSettingsGui.IntSettingFactory {

        int defaultBookVal;

        protected EnchantCostSettingFactory(
                @NotNull String title, ValueUpdatableGui parent,
                String configPath, ConfigHolder config,
                int min, int max, int defaultItemVal, int defaultBookVal,
                int... steps){

            super(title,parent,
                    configPath, config,
                    min, max, defaultItemVal, steps);
            this.defaultBookVal = defaultBookVal;
        }

        @NotNull
        public String getTitle() {
            return title;
        }

        @Override
        public int getConfiguredValue() {
            return this.config.getConfig().getInt(this.configPath+ITEM_PATH, this.defaultVal);
        }

        public int getConfiguredBookValue(){
            return this.config.getConfig().getInt(this.configPath+BOOK_PATH, this.defaultBookVal);
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            int nowItem = getConfiguredValue();
            // create new gui
            return new EnchantCostSettingsGui(this, nowItem);
        }

    }

}

