package xyz.alexcrea.cuanvil.gui.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.alexcrea.cuanvil.gui.gui.config.BasicConfigGui;

public class MainConfigGui extends ChestGui {

    public final static MainConfigGui INSTANCE = new MainConfigGui();

    private MainConfigGui() {
        super(3, "§8Anvil Config", CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "111111111",
                "112345611",
                "111111111"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        ItemStack backgroundBukkit = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        GuiItem background = new GuiItem(backgroundBukkit,GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        pane.bindItem('1', background);


        ItemStack stonePlaceholder = new ItemStack(Material.STONE);
        GuiItem placeholder1 = GuiGlobalItems.toGuiItem(stonePlaceholder, BasicConfigGui.INSTANCE);
        GuiItem placeholder2 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder3 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder4 = new GuiItem(stonePlaceholder,CustomAnvil.instance);
        GuiItem placeholder5 = new GuiItem(stonePlaceholder,CustomAnvil.instance);


        pane.bindItem('2', placeholder1);
        pane.bindItem('3', placeholder2);
        pane.bindItem('4', placeholder3);
        pane.bindItem('5', placeholder4);
        pane.bindItem('6', placeholder5);

    }



}
