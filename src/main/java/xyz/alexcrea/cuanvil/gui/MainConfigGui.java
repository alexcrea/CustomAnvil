package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.gui.config.BasicConfigGui;
import xyz.alexcrea.cuanvil.gui.utils.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.utils.GuiGlobalItems;

import java.util.Collections;

public class MainConfigGui extends ChestGui {

    public final static MainConfigGui INSTANCE = new MainConfigGui();

    static {
        INSTANCE.init();
    }
    private MainConfigGui() {
        super(3, "\u00A78Anvil Config", CustomAnvil.instance);

    }

    private void init(){
        Pattern pattern = new Pattern(
                "000000000",
                "012304560",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        ItemStack basicConfigItemstack = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta basicConfigMeta = basicConfigItemstack.getItemMeta();

        basicConfigMeta.setDisplayName("\u00A7aBasic Config Menu");
        basicConfigMeta.setLore(Collections.singletonList("\u00A77Click here to open basic config menu"));
        basicConfigItemstack.setItemMeta(basicConfigMeta);

        GuiItem placeholder1 = GuiGlobalItems.goToGuiItem(basicConfigItemstack, BasicConfigGui.INSTANCE);
        pane.bindItem('1', placeholder1);

        // WIP configuration items
        ItemStack wipItemstack = new ItemStack(Material.BARRIER);
        ItemMeta wipMeta = wipItemstack.getItemMeta();
        wipMeta.setDisplayName("\u00A7cWIP");
        wipItemstack.setItemMeta(wipMeta);

        GuiItem wip2 = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
        GuiItem wip3 = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
        GuiItem wip4 = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
        GuiItem wip5 = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
        GuiItem wip6 = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);


        pane.bindItem('2', wip2);
        pane.bindItem('3', wip3);
        pane.bindItem('4', wip4);
        pane.bindItem('5', wip5);
        pane.bindItem('6', wip6);

        // quit item
        ItemStack quitItemstack = new ItemStack(Material.BARRIER);
        ItemMeta quitMeta = quitItemstack.getItemMeta();
        quitMeta.setDisplayName("\u00A7cQuit");
        quitItemstack.setItemMeta(quitMeta);

        GuiItem quitItem = new GuiItem(quitItemstack, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        },CustomAnvil.instance);
        pane.bindItem('Q', quitItem);

    }

}
