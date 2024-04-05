package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import io.delilaheve.CustomAnvil;
import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Global Config gui for enchantment setting configuration.
 *
 * @param <T> Type of the factory of the type of setting the gui should edit.
 */
public abstract class AbstractEnchantConfigGui<T extends AbstractSettingGui.SettingGuiFactory> extends ValueUpdatableGui {

    /**
     * Constructor for a gui displaying available enchantment to edit a enchantment setting.
     *
     * @param title Title of the gui.
     */
    protected AbstractEnchantConfigGui(String title) {
        super(6, title, CustomAnvil.instance);
    }

    private OutlinePane filledEnchant;

    /**
     * Initialise value updatable gui pattern
     */
    protected void init() {
        // Back item panel
        addPane(GuiSharedConstant.BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE);

        // enchant item panel
        this.filledEnchant = new OutlinePane(0, 0, 9, 5);
        this.filledEnchant.align(OutlinePane.Alignment.BEGIN);
        this.filledEnchant.setOrientation(Orientable.Orientation.HORIZONTAL);
        addPane(this.filledEnchant);

        prepareValues();
        updateGuiValues();
    }

    private List<T> bookItemFactoryList;

    /**
     * Prepare enchantment config gui displayed items factory.
     */
    protected void prepareValues() {
        bookItemFactoryList = new ArrayList<>();

        for (Enchantment enchant : GuiSharedConstant.SORTED_ENCHANTMENT_LIST) {
            T factory = getFactoryFromEnchant(enchant);

            bookItemFactoryList.add(factory);
        }
    }

    @Override
    public void updateGuiValues() {

        // probably not the most efficient but hey ! it do the work
        // TODO optimise one day.. maybe

        this.filledEnchant.clear();

        for (T inventoryFactory : this.bookItemFactoryList) {
            GuiItem item = getItemFromFactory(inventoryFactory);

            this.filledEnchant.addItem(item);
        }

        update();
    }


    public abstract T getFactoryFromEnchant(Enchantment enchant);

    public abstract GuiItem getItemFromFactory(T inventoryFactory);

}
