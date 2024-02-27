package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

// maybe use builder patern ?
public class GuiGlobalItems {

    // return
    public static GuiItem toGuiItem(@NotNull ItemStack item, @NotNull Gui goal){
        return new GuiItem(item, GuiGlobalActions.openGuiFactory(goal), CustomAnvil.instance);
    }

    // statically create back itemstack
    private static final ItemStack BACK_ITEM = new ItemStack(Material.BARRIER);
    static {
        // todo add what I need to add to the back item
        ItemMeta meta = BACK_ITEM.getItemMeta();
        meta.setDisplayName("\u00A7cBack");
        BACK_ITEM.setItemMeta(meta);
    }
    public static GuiItem backItem(@NotNull Gui goal){
        // simple go back item
        return toGuiItem(BACK_ITEM, goal);
    }
    public static void addBackItem(@NotNull PatternPane target,
                                   @NotNull Gui goal){
        target.bindItem('B', backItem(goal));
    }

    private static final Material DEFAULT_BACKGROUND_MAT = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
    public static GuiItem backgroundItem(Material backgroundMat){
        ItemStack item = new ItemStack(backgroundMat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A7c");
        item.setItemMeta(meta);
        return new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }
    public static GuiItem backgroundItem(){
        return backgroundItem(DEFAULT_BACKGROUND_MAT);
    }
    public static void addBackgroundItem(@NotNull PatternPane target,
                                            @NotNull Material backgroundMat){
        target.bindItem('0', backgroundItem(backgroundMat));
    }
    public static void addBackgroundItem(@NotNull PatternPane target){
        addBackgroundItem(target, DEFAULT_BACKGROUND_MAT);
    }

}
