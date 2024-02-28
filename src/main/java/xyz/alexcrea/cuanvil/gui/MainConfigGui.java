package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.alexcrea.cuanvil.gui.config.BasicConfigGui;

public class MainConfigGui extends ChestGui {

    public final static MainConfigGui INSTANCE = new MainConfigGui();

    static {
        INSTANCE.init();
    }
    private MainConfigGui() {
        super(3, "\u00A7cAnvil Config", CustomAnvil.instance);

    }

    private void init(){
        Pattern pattern = new Pattern(
                "000000000",
                "001234500",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);


        ItemStack stonePlaceholder = new ItemStack(Material.STONE);
        GuiItem placeholder1 = GuiGlobalItems.toGuiItem(stonePlaceholder, BasicConfigGui.INSTANCE);
        GuiItem placeholder2 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder3 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder4 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder5 = new GuiItem(stonePlaceholder,CustomAnvil.instance);


        pane.bindItem('1', placeholder1);
        pane.bindItem('2', placeholder2);
        pane.bindItem('3', placeholder3);
        pane.bindItem('4', placeholder4);
        pane.bindItem('5', placeholder5);

        // quit item
        ItemStack quitItemstack = new ItemStack(Material.BARRIER);
        GuiItem quitItem = new GuiItem(quitItemstack, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        },CustomAnvil.instance);
        pane.bindItem('Q', quitItem);

    }

}
