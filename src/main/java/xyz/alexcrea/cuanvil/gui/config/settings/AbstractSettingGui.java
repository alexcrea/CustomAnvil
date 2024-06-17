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

/**
 * An instance gui used to edit a setting.
 */
public abstract class AbstractSettingGui extends ChestGui implements SettingGui {

    protected static final String CLICK_LORE = "\u00A77Click Here to change the value";

    private PatternPane pane;

    /**
     * Prepare necessary object for a setting gui.
     *
     * @param rows   Number of row for this gui.
     * @param title  Title of this gui.
     * @param parent Parent gui to go back when completed.
     */
    protected AbstractSettingGui(int rows, @NotNull TextHolder title, ValueUpdatableGui parent) {
        super(rows, title, CustomAnvil.instance);
        initBase(parent);
    }

    /**
     * Prepare necessary object for a setting gui.
     *
     * @param rows   Number of row for this gui.
     * @param title  Title of this gui.
     * @param parent Parent gui to go back when completed.
     */
    protected AbstractSettingGui(int rows, @NotNull String title, ValueUpdatableGui parent) {
        this(rows, StringHolder.of(title), parent);
    }

    protected GuiItem saveItem;
    protected GuiItem noChangeItem;

    /**
     * Initialise and prepare value for this gui.
     *
     * @param parent Parent gui to go back when completed.
     */
    protected void initBase(ValueUpdatableGui parent) {
        Pattern pattern = getGuiPattern();
        pane = new PatternPane(0, 0, pattern.getLength(), pattern.getHeight(), pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, parent.getConnectedGui());
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
     *
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
     *
     * @return The gui's pattern.
     */
    protected abstract Pattern getGuiPattern();

    /**
     * Most of the time a setting gui will be called from a global gui.
     * <p>
     * It is better to keep a factory that hold setting data than find what parameters to use every time.
     */
    public abstract static class SettingGuiFactory implements SettingGui.SettingGuiFactory {
        @NotNull
        protected String configPath;
        @NotNull
        protected ConfigHolder config;

        /**
         * Constructor for settings gui factory
         *
         * @param configPath Configuration path of this setting.
         * @param config     Configuration holder of this setting.
         */
        protected SettingGuiFactory(@NotNull String configPath, @NotNull ConfigHolder config) {
            this.configPath = configPath;
            this.config = config;
        }

        /**
         * @return Configuration path of this setting.
         */
        @NotNull
        public String getConfigPath() {
            return configPath;
        }

        /**
         * @return Configuration holder of this setting.
         */
        @NotNull
        public ConfigHolder getConfigHolder() {
            return config;
        }

    }
}
