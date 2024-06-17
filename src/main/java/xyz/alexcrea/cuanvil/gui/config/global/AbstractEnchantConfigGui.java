package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.list.SettingGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.SettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract Global Config gui for enchantment setting configuration.
 *
 * @param <T> Type of the factory of the type of setting the gui should edit.
 */
public abstract class AbstractEnchantConfigGui<T extends SettingGui.SettingGuiFactory> extends SettingGuiListConfigGui<WrappedEnchantment, T> implements ValueUpdatableGui {

    /**
     * Constructor for a gui displaying available enchantment to edit a enchantment setting.
     *
     * @param title Title of the gui.
     */
    protected AbstractEnchantConfigGui(String title) {
        super(title);
    }

    @Override
    public void updateGuiValues() { //TODO maybe optimise it.
        reloadValues();
    }

    @Override
    protected List<WrappedEnchantment> getEveryDisplayableInstanceOfGeneric() {
        return GuiSharedConstant.SORTED_ENCHANTMENT_LIST;
    }

    @Override
    protected Pattern getBackgroundPattern(){
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B11L1R111"
        );
    }


    // Unused methods
    @Override
    protected GuiItem prepareCreateNewItem() {
        return null;
    }

    @Override
    protected List<String> getCreateItemLore() {
        return Collections.emptyList();
    }
    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return null;
    }

    @Override
    protected String createItemName() {
        return null;
    }

}
