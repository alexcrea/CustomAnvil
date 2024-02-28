package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BoolSettingsGui extends AbstractSettingGui{

    private final BoolSettingFactory holder;
    private boolean now;

    private BoolSettingsGui(BoolSettingFactory holder, boolean now) {
        super(3, holder.title, holder.parent);
        this.holder = holder;
        this.now = now;

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
    public void onSave() {
        holder.section.set(holder.configPath, now);
        // TODO SAVE (also backup before)

    }

    public static SettingGuiFactory factory(@NotNull String title, Gui parent,
                                            String configPath, ConfigurationSection section,
                                            boolean defaultVal){
        return new BoolSettingFactory(
                title,parent,
                configPath, section,
                defaultVal);
    }


    public static class BoolSettingFactory extends SettingGuiFactory{
        @NotNull String title; Gui parent;
        String configPath; ConfigurationSection section;
        boolean defaultVal;

        private BoolSettingFactory(@NotNull String title, Gui parent,
                                  String configPath, ConfigurationSection section,
                                  boolean defaultVal){
            this.title = title;
            this.parent = parent;
            this.configPath = configPath;
            this.section = section;
            this.defaultVal = defaultVal;
        }

        @Override
        public AbstractSettingGui create() {
            // Get value or default
            //TODO maybe get section dynamically (and maybe same for save ?)
            boolean now = section.getBoolean(this.configPath, this.defaultVal);
            // create new gui
            return new BoolSettingsGui(this, now);
        }

    }

}

