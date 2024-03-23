package xyz.alexcrea.cuanvil.gui.config.openable;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.gui.config.settings.subsetting.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.HashMap;

public class EnchantConflictGui extends ChestGui {

    public final static EnchantConflictGui INSTANCE = new EnchantConflictGui();
    static {
        INSTANCE.init();
    }

    private final HashMap<EnchantConflictGroup, EnchantConflictSubSettingGui> conflictGuiMap;

    private EnchantConflictGui() {
        super(6, "§eConflict Config", CustomAnvil.instance);
        this.conflictGuiMap = new HashMap<>();
    }

    private OutlinePane filledEnchant;

    private void init(){
        // Back item panel
        addPane(GuiSharedConstant.BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE);

        // enchant item panel
        this.filledEnchant = new OutlinePane(0, 0, 9, 5);
        this.filledEnchant.align(OutlinePane.Alignment.BEGIN);
        this.filledEnchant.setOrientation(Orientable.Orientation.HORIZONTAL);
        addPane(this.filledEnchant);

        reloadValues();
    }

    public void reloadValues(){
        this.conflictGuiMap.forEach((conflict, gui) -> gui.cleanUnused());
        this.conflictGuiMap.clear();
        this.filledEnchant.clear();

        for (EnchantConflictGroup conflict : ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList()) {
            updateValueForConflict(conflict, false);
        }

        update();
    }

    public ItemStack createItemForConflict(EnchantConflictGroup conflict){
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        //TODO item

        return item;
    }

    public void updateValueForConflict(EnchantConflictGroup conflict, boolean shouldUpdate){
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);
        ItemStack item = createItemForConflict(conflict);

        GuiItem guiItem;
        if(gui == null){
            // Create new sub setting gui
            guiItem = new GuiItem(item, CustomAnvil.instance);
            gui = new EnchantConflictSubSettingGui(this, conflict, guiItem);

            guiItem.setAction(GuiGlobalActions.openGuiAction(gui));

            this.conflictGuiMap.put(conflict, gui);
            this.filledEnchant.addItem(guiItem);
        }else{
            // replace item with the updated one
            guiItem = gui.getParentItemForThisGui();
            guiItem.setItem(item);
        }

        gui.updateLocal();
        if(shouldUpdate){
            update();
        }

    }

    public void removeConflict(EnchantConflictGroup conflict){
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);
        if(gui == null) return;

        this.filledEnchant.removeItem(gui.getParentItemForThisGui());
        this.conflictGuiMap.remove(conflict);
        update();
    }

}
