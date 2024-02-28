package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.GuiGlobalItems;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSettingGui extends ChestGui {

    protected final static List<String> CLICK_LORE = Collections.singletonList("\u00A77Click Here to change the value");

    public AbstractSettingGui(int rows, @NotNull TextHolder title, Gui parent) {
        super(rows, title, CustomAnvil.instance);
        initBase(parent);
    }

    public AbstractSettingGui(int rows, @NotNull String title, Gui parent) {
        this(rows, StringHolder.of(title), parent);
    }

    private PatternPane pane;
    private void initBase(Gui parent){
        Pattern pattern = getGuiPattern();
        pane = new PatternPane(0, 0, pattern.getLength(), pattern.getHeight(), pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, parent);
        GuiGlobalItems.addBackgroundItem(pane);

        pane.bindItem('S', GuiGlobalItems.saveItem(this, parent));

    }

    protected PatternPane getPane() {
        return pane;
    }

    // S, b, 0 is used by: save, back, background
    protected abstract Pattern getGuiPattern();

    public abstract void onSave();


    public abstract static class SettingGuiFactory{
        protected String configPath;
        protected ConfigurationSection section;

        protected SettingGuiFactory(String configPath, ConfigurationSection section){
            this.configPath = configPath;
            this.section = section;
        }

        public String getConfigPath() {
            return configPath;
        }

        public ConfigurationSection getSection() {
            return section;
        }


        public abstract AbstractSettingGui create();
    }
}
