package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.utils.GuiGlobalItems;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSettingGui extends ChestGui {

    // Temporary values, until I get something better.
    public static final boolean TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE = true;
    public static final boolean TEMPORARY_DO_BACKUP_EVERY_SAVE = true;

    protected final static List<String> CLICK_LORE = Collections.singletonList("\u00A77Click Here to change the value");

    private PatternPane pane;

    public AbstractSettingGui(int rows, @NotNull TextHolder title, ValueUpdatableGui parent) {
        super(rows, title, CustomAnvil.instance);
        initBase(parent);
    }

    public AbstractSettingGui(int rows, @NotNull String title, ValueUpdatableGui parent) {
        this(rows, StringHolder.of(title), parent);
    }

    protected GuiItem saveItem;
    protected GuiItem noChangeItem;
    private void initBase(ValueUpdatableGui parent){
        Pattern pattern = getGuiPattern();
        pane = new PatternPane(0, 0, pattern.getLength(), pattern.getHeight(), pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, parent);
        GuiGlobalItems.addBackgroundItem(pane);

        saveItem = GuiGlobalItems.saveItem(this, parent);
        noChangeItem = GuiGlobalItems.noChangeItem();

        pane.bindItem('S', noChangeItem);

    }

    @Override
    public void update() {
        pane.bindItem('S', hadChange() ? saveItem : noChangeItem);
        super.update();
    }

    protected PatternPane getPane() {
        return pane;
    }

    // S, b, 0 is used by: save, back, background
    protected abstract Pattern getGuiPattern();

    public abstract boolean onSave();

    public abstract boolean hadChange();


    public abstract static class SettingGuiFactory{
        protected String configPath;
        protected ConfigHolder config;

        protected SettingGuiFactory(String configPath, ConfigHolder config){
            this.configPath = configPath;
            this.config = config;
        }

        public String getConfigPath() {
            return configPath;
        }

        public ConfigHolder getConfigHolder() {
            return config;
        }

        public abstract AbstractSettingGui create();
    }
}
