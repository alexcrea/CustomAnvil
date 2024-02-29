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
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.utils.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.utils.GuiGlobalItems;

import java.util.function.Consumer;

public class IntSettingsGui extends AbstractSettingGui{

    private final IntSettingFactory holder;
    private int now;
    private int step;

    private IntSettingsGui(IntSettingFactory holder, int now) {
        super(3, holder.title, holder.parent);
        assert holder.steps.length > 0 && holder.steps.length <= 9;
        this.holder = holder;
        this.now = now;
        this.step = holder.steps[0];

        updateValueDisplay();
    }


    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                "000000000",
                "00-0v0+00",
                "B0000000S"
        );
    }

    protected void updateValueDisplay(){

        PatternPane pane = getPane();

        // minus item
        GuiItem minusItem;
        if(now > holder.min){
            int planned = Math.max(holder.min, now - step);
            ItemStack item = new ItemStack(Material.RED_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+planned + " \u00A7r(\u00A7c-"+(now-planned)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            minusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        }else{
            minusItem = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('-', minusItem);

        //plus item
        // may do a function to generalise ?
        GuiItem plusItem;
        if(now < holder.max){
            int planned = Math.min(holder.max, now + step);
            ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00A7e"+planned + " \u00A7r(\u00A7a+"+(planned-now)+"\u00A7r)");
            meta.setLore(AbstractSettingGui.CLICK_LORE);
            item.setItemMeta(meta);

            plusItem = new GuiItem(item, updateNowConsumer(planned), CustomAnvil.instance);
        }else{
            plusItem = GuiGlobalItems.backgroundItem();
        }
        pane.bindItem('+', plusItem);

        // "result" display
        ItemStack resultPaper = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = resultPaper.getItemMeta();
        resultMeta.setDisplayName("\u00A7e"+now);
        resultPaper.setItemMeta(resultMeta);
        GuiItem resultItem = new GuiItem(resultPaper, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('v', resultItem);

    }

    protected Consumer<InventoryClickEvent> updateNowConsumer(int planned){
        return event->{
            event.setCancelled(true);
            now = planned;
            updateValueDisplay();
            update();
        };
    }

    @Override
    public void onSave() {
        holder.section.set(holder.configPath, now);
        // TODO SAVE (also backup before)

    }

    public static SettingGuiFactory factory(@NotNull String title, ValueUpdatableGui parent,
                                            String configPath, ConfigurationSection section,
                                            int min, int max, int defaultVal, int... steps){
        return new IntSettingFactory(
                title,parent,
                configPath, section,
                min, max, defaultVal, steps);
    }


    public static class IntSettingFactory extends SettingGuiFactory{
        @NotNull String title; ValueUpdatableGui parent;
        int min; int max; int defaultVal; int[] steps;

        private IntSettingFactory(@NotNull String title, ValueUpdatableGui parent,
                                  String configPath, ConfigurationSection section,
                                  int min, int max, int defaultVal, int... steps){
            super(configPath, section);
            this.title = title;
            this.parent = parent;
            this.min = min;
            this.max = max;
            this.defaultVal = defaultVal;
            this.steps = steps;
        }

        public int getConfiguredValue(){
            return this.section.getInt(this.configPath, this.defaultVal);
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            //TODO maybe get section dynamically (and maybe same for save ?)
            int now = getConfiguredValue();
            // create new gui
            return new IntSettingsGui(this, now);
        }

    }

}

