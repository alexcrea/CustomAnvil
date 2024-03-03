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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.util.Collections;
import java.util.function.Consumer;

public class BoolSettingsGui extends AbstractSettingGui{

    private final BoolSettingFactory holder;
    private final boolean before;
    private boolean now;

    protected BoolSettingsGui(BoolSettingFactory holder, boolean now) {
        super(3, holder.title, holder.parent);
        this.holder = holder;
        this.before = now;
        this.now = now;

        prepareReturnToDefault();
        updateValueDisplay();
    }


    @Override
    public Pattern getGuiPattern() {
        return new Pattern(
                "000000000",
                "D0-0v0+00",
                "B0000000S"
        );
    }
    GuiItem returnToDefault;
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

        // Get displayed value for this config.
        String displayedName;
        Material displayedMat;
        if(now){
            displayedName = "\u00A7aTrue";
            displayedMat = Material.GREEN_TERRACOTTA;
        }else{
            displayedName = "\u00A7cFalse";
            displayedMat = Material.RED_TERRACOTTA;
        }

        ItemStack valueItemStack = new ItemStack(displayedMat);
        ItemMeta valueMeta = valueItemStack.getItemMeta();
        valueMeta.setDisplayName(displayedName);
        valueMeta.setLore(AbstractSettingGui.CLICK_LORE);
        valueItemStack.setItemMeta(valueMeta);
        GuiItem resultItem = new GuiItem(valueItemStack, inverseNowConsumer(), CustomAnvil.instance);

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

    protected Consumer<InventoryClickEvent> inverseNowConsumer(){
        return event->{
            event.setCancelled(true);
            now = !now;
            updateValueDisplay();
            update();
        };
    }

    @Override
    public boolean onSave() {
        holder.config.getConfig().set(holder.configPath, now);

        MetricsUtil.INSTANCE.notifyChange(this.holder.config, this.holder.configPath);
        if(TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
            return holder.config.saveToDisk(TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public boolean hadChange() {
        return now != before;
    }

    public static BoolSettingFactory boolFactory(@NotNull String title, ValueUpdatableGui parent,
                                                 String configPath, ConfigHolder config,
                                                 boolean defaultVal){
        return new BoolSettingFactory(
                title,parent,
                configPath, config,
                defaultVal);
    }


    public static class BoolSettingFactory extends SettingGuiFactory{
        @NotNull String title; ValueUpdatableGui parent;
        boolean defaultVal;

        protected BoolSettingFactory(
                @NotNull String title, ValueUpdatableGui parent,
                String configPath, ConfigHolder config,
                boolean defaultVal){
            super(configPath, config);
            this.title = title;
            this.parent = parent;

            this.defaultVal = defaultVal;
        }

        @NotNull
        public String getTitle() {
            return title;
        }

        public boolean getConfiguredValue(){
            return this.config.getConfig().getBoolean(this.configPath, this.defaultVal);
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            boolean now = getConfiguredValue();
            // create new gui
            return new BoolSettingsGui(this, now);
        }

    }

}

