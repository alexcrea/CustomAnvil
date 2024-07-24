package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.list.SettingGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.SettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract Global Config gui for enchantment setting configuration.
 *
 * @param <T> Type of the factory of the type of setting the gui should edit.
 */
public abstract class AbstractEnchantConfigGui<T extends SettingGui.SettingGuiFactory> extends SettingGuiListConfigGui<CAEnchantment, T> implements ValueUpdatableGui {

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
    protected Collection<CAEnchantment> getEveryDisplayableInstanceOfGeneric() {
        return CAEnchantmentRegistry.getInstance().getNameSortedEnchantments();
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

    @Override
    public void updateValueForGeneric(CAEnchantment generic, boolean shouldUpdate) {
        updateValueForGeneric(generic, shouldUpdate, true);
    }

    public void updateValueForGeneric(CAEnchantment generic, boolean shouldUpdate, boolean prepareSorting) {
        if(!prepareSorting) {
            super.updateValueForGeneric(generic, shouldUpdate);
            return;
        }

        if(!this.factoryMap.containsKey(generic)){
            // We need to sort elements again
            super.updateValueForGeneric(generic, false);

            // Clear page then refill all of them
            this.firstPage.clear();
            this.pages.clear();
            this.pages.add(this.firstPage);

            for (CAEnchantment enchantment : getEveryDisplayableInstanceOfGeneric()) {
                GuiItem item = this.guiItemMap.get(enchantment);

                if(item == null) {
                    updateValueForGeneric(enchantment, false, false);
                }else {
                    addToPage(item);
                }

            }

            if(shouldUpdate) update();

        }else{
            super.updateValueForGeneric(generic, shouldUpdate);

        }

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
