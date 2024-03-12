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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Collections;
import java.util.List;

/**
 * An instance gui used to edit a setting.
 */
public abstract class AbstractSettingGui extends ChestGui {

    // Temporary values, until I get something better.
    public static final boolean TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE = true;
    public static final boolean TEMPORARY_DO_BACKUP_EVERY_SAVE = true;

    protected final static List<String> CLICK_LORE = Collections.singletonList("\u00A77Click Here to change the value");

    private PatternPane pane;

    /**
     * Prepare necessary object for a setting gui.
     * @param rows Number of row for this gui.
     * @param title Title of this gui.
     * @param parent Parent gui to go back when completed.
     */
    public AbstractSettingGui(int rows, @NotNull TextHolder title, ValueUpdatableGui parent) {
        super(rows, title, CustomAnvil.instance);
        initBase(parent);
    }

    /**
     * Prepare necessary object for a setting gui.
     * @param rows Number of row for this gui.
     * @param title Title of this gui.
     * @param parent Parent gui to go back when completed.
     */
    public AbstractSettingGui(int rows, @NotNull String title, ValueUpdatableGui parent) {
        this(rows, StringHolder.of(title), parent);
    }

    protected GuiItem saveItem;
    protected GuiItem noChangeItem;

    /**
     * Initialise and prepare value for this gui.
     * @param parent Parent gui to go back when completed.
     */
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

    /**
     * Get main pane for this setting gui.
     * @return Main pattern pain of this gui.
     */
    protected PatternPane getPane() {
        return pane;
    }

    /**
     * Used to get the gui pattern.
     * Reserved character are:
     * <ul>
     * <li><b>S</b>: save setting button.</li>
     * <li><b>B</b>: "back to previous gui" button.</li>
     * <li><b>0</b>: default background item.</li>
     * </ul>
     * @return The gui's pattern.
     */
    protected abstract Pattern getGuiPattern();

    /**
     * Called when the associated setting need to be saved.
     * @return true if the save was successful. false otherwise
     */
    public abstract boolean onSave();

    /**
     * If this function return true
     * the gui assume the associated setting can be saved.
     * @return true if there is a change to the setting. false otherwise
     */
    public abstract boolean hadChange();

    /**
     * Most of the time a setting gui will be called from a global gui.
     * <p>
     * It is better to keep a factory that hold setting data than find what parameters to use every time.
     */
    public abstract static class SettingGuiFactory{
        protected String configPath;
        protected ConfigHolder config;

        /**
         * Constructor for settings gui factory
         * @param configPath Configuration path of this setting.
         * @param config Configuration holder of this setting.
         */
        protected SettingGuiFactory(String configPath, ConfigHolder config){
            this.configPath = configPath;
            this.config = config;
        }

        /**
         * @return Configuration path of this setting.
         */
        public String getConfigPath() {
            return configPath;
        }

        /**
         * @return Configuration holder of this setting.
         */
        public ConfigHolder getConfigHolder() {
            return config;
        }

        /**
         * Create a gui using setting parameters and current setting value.
         * @return A new instance of the implemented setting gui.
         */
        public abstract AbstractSettingGui create();
    }
}
