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


    private final HashMap<EnchantConflictGroup, EnchantConflictSubSettingGui> conflictGuiMap;
    static {
        INSTANCE.init();
    }

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

        // Create new sub setting gui
        for (EnchantConflictGroup conflict : ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList()) {
            EnchantConflictSubSettingGui conflictGui = new EnchantConflictSubSettingGui(this, conflict);

            // Temporaire, il faut faire un item avec le conflict et donc un generateur arbitraire

            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            filledEnchant.addItem(new GuiItem(item, GuiGlobalActions.openGuiAction(conflictGui), CustomAnvil.instance));
        }

    }

    public void updateValueForConflict(EnchantConflictGroup conflict){

    }

    public void removeConflict(EnchantConflictGroup conflict){

    }

}
