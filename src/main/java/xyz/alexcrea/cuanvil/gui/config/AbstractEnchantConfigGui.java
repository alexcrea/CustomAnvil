package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Global Config gui for enchantment setting configuration.
 * @param <T> Type of the factory of the type of setting the gui should edit.
 */
public abstract class AbstractEnchantConfigGui<T extends AbstractSettingGui.SettingGuiFactory> extends ValueUpdatableGui {

    private final Gui backGui;

    /**
     * Constructor for a gui displaying available enchantment to edit a enchantment setting.
     * @param title Title of the gui.
     * @param backGui Gui to go back on click on the "back" button.
     */
    protected AbstractEnchantConfigGui(String title, Gui backGui){
        super(6, title, CustomAnvil.instance);
        this.backGui = backGui;
    }
    /**
     * Constructor for a gui displaying available enchantment to edit a enchantment setting.
     * @param title Title of the gui.
     */
    protected AbstractEnchantConfigGui(String title){
        this(title, MainConfigGui.INSTANCE);
    }

    PatternPane backgroundItems;
    OutlinePane filledEnchant;

    // Why is called like it is rn
    /**
     * Initialise value updatable gui pattern
      */
    protected void init(){
        // Back item panel
        Pattern pattern = new Pattern(
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "B11111111"
        );
        this.backgroundItems = new PatternPane(0, 0, 9, 6, Pane.Priority.LOW, pattern);
        addPane(this.backgroundItems);

        GuiGlobalItems.addBackItem(this.backgroundItems, this.backGui);

        GuiGlobalItems.addBackgroundItem(this.backgroundItems);
        this.backgroundItems.bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);

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
    protected void prepareValues(){
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
