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
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class IntSettingsGui extends AbstractSettingGui{

    protected final IntSettingFactory holder;
    protected final int before;
    protected int now;
    protected int step;

    protected IntSettingsGui(IntSettingFactory holder, int now) {
        super(3, holder.title, holder.parent);
        assert holder.steps.length > 0 && holder.steps.length <= 9;
        this.holder = holder;
        this.before = now;
        this.now = now;
        this.step = holder.steps[0];

        prepareReturnToDefault();
        updateValueDisplay();
        initStepsValue();
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
    protected void prepareReturnToDefault(){
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("\u00A7eReset to default value");
        meta.setLore(Collections.singletonList("\u00A77Default value is: "+holder.defaultVal));
        item.setItemMeta(meta);
        returnToDefault = new GuiItem(item, event -> {
            event.setCancelled(true);
            now = holder.defaultVal;
            updateValueDisplay();
            update();
            }, CustomAnvil.instance);
    }

    protected void updateValueDisplay(){

        PatternPane pane = getPane();

        // minus item
        GuiItem minusItem;
        if(now > holder.min){
            int planned = Math.max(holder.min, now - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+now+" -> "+planned + " \u00A7r(\u00A7c-"+(now-planned)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        }else{
            minusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('-', minusItem);

        //plus item
        // may do a function to generalise ?
        GuiItem plusItem;
        if(now < holder.max){
            int planned = Math.min(holder.max, now + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+now+" -> "+planned + " \u00A7r(\u00A7a+"+(planned-now)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            plusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        }else{
            plusItem = GuiGlobalItems.backgroundItem(Material.BARRIER);
        }
        pane.bindItem('+', plusItem);

        // "result" display
        ItemStack resultPaper = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = resultPaper.getItemMeta();
        resultMeta.setDisplayName("\u00A7eValue: "+now);
        resultPaper.setItemMeta(resultMeta);
        GuiItem resultItem = new GuiItem(resultPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('v', resultItem);

        // reset to default
        GuiItem returnToDefault;
        if(now != holder.defaultVal){
            returnToDefault = this.returnToDefault;
        }else{
            returnToDefault = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('D', returnToDefault);


    }

    protected Consumer<InventoryClickEvent> updateNowConsumer(int planned){
        return event->{
            event.setCancelled(true);
            now = planned;
            updateValueDisplay();
            update();
        };
    }

    protected void initStepsValue(){
        // Put background glass on the background of 'a' to 'b'
        GuiItem background = GuiGlobalItems.backgroundItem();
        PatternPane pane = getPane();

        for (char i = 'a'; i < (getMidStepChar()-'a')*2+1; i++) {
            pane.bindItem(i, background);
        }
        // Then update legit step values
        updateStepValue();
    }
    protected void updateStepValue(){
        if(holder.steps.length <= 1) return;
        // We assume steps have a length of 2k+1 cause its more pretty
        char val = getMidStepChar();
        // Offset
        val -= (char) ((holder.steps.length-1)/2);

        // Then place items
        PatternPane pane = getPane();
        for (int i = 0; i < holder.steps.length; i++) {
            pane.bindItem(val+i, stepGuiItem(i));
        }

    }

    protected char getMidStepChar(){
        return 'e';
    }

    protected GuiItem stepGuiItem(int stepIndex){
        int stepValue = holder.steps[stepIndex];

        // Get material properties
        Material stepMat;
        StringBuilder stepName = new StringBuilder("\u00A7");
        List<String> stepLore;
        Consumer<InventoryClickEvent> clickEvent;
        if(stepValue == step){
            stepMat = Material.GREEN_STAINED_GLASS_PANE;
            stepName.append('a');
            stepLore = Collections.singletonList("\u00A77Value is changing by "+stepValue);
            clickEvent = GuiGlobalActions.stayInPlace;
        }else{
            stepMat = Material.RED_STAINED_GLASS_PANE;
            stepName.append('c');
            stepLore = Collections.singletonList("\u00A77Click here to change the value by "+stepValue);
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

    protected Consumer<InventoryClickEvent> updateStepValue(int stepValue){
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
        if(TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
            holder.config.getConfig().set(holder.configPath, now);
            return holder.config.saveToDisk(TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return now != before;
    }

    public static IntSettingFactory intFactory(@NotNull String title, ValueUpdatableGui parent,
                                               String configPath, ConfigHolder config,
                                               int min, int max, int defaultVal, int... steps){
        return new IntSettingFactory(
                title,parent,
                configPath, config,
                min, max, defaultVal, steps);
    }


    public static class IntSettingFactory extends SettingGuiFactory{
        @NotNull String title; ValueUpdatableGui parent;
        int min; int max; int defaultVal; int[] steps;

        protected IntSettingFactory(
                @NotNull String title, ValueUpdatableGui parent,
                String configPath, ConfigHolder config,
                int min, int max, int defaultVal, int... steps){
            super(configPath, config);
            this.title = title;
            this.parent = parent;
            this.min = min;
            this.max = max;
            this.defaultVal = defaultVal;
            this.steps = steps;
        }

        @NotNull
        public String getTitle() {
            return title;
        }

        public int getConfiguredValue(){
            return this.config.getConfig().getInt(this.configPath, this.defaultVal);
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            int now = getConfiguredValue();
            // create new gui
            return new IntSettingsGui(this, now);
        }

    }

}

