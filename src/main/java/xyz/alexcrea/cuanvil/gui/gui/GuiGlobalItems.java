package xyz.alexcrea.cuanvil.gui.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiGlobalItems {

    // return
    public static GuiItem toGuiItem(ItemStack item, Gui goal){
        return new GuiItem(item, GuiGlobalActions.openGuiFactory(goal), CustomAnvil.instance);
    }

    // statically create back itemstack
    private static final ItemStack BACK_ITEM = new ItemStack(Material.BARRIER);
    static {
        // todo add what I need to add to the back item
        ItemMeta meta = BACK_ITEM.getItemMeta();
        meta.setDisplayName("§cBack");
        BACK_ITEM.setItemMeta(meta);
    }
    public static GuiItem backItem(Gui goal){
        // simple go back item
        return toGuiItem(BACK_ITEM, goal);
    }
    public static void addBackItem(PatternPane target, Gui goal){
        target.bindItem('B', backItem(goal));
    }

}
