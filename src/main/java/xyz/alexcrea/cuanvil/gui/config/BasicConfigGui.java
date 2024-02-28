package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.alexcrea.cuanvil.gui.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;

public class BasicConfigGui extends ChestGui {

    public final static BasicConfigGui INSTANCE = new BasicConfigGui();

    static {
        INSTANCE.init();
    }

    private BasicConfigGui(){
        super(3, "\u00A7cBasic Config GUI", CustomAnvil.instance);

    }

    private void init(){
        Pattern pattern = new Pattern(
                "000000000",
                "012000000",
                "B00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);
        GuiGlobalItems.addBackgroundItem(pane);

        ItemStack setting1Item = new ItemStack(Material.STONE);
        AbstractSettingGui.SettingGuiFactory factory1 = IntSettingsGui.factory( "Test GUI", this, "test", CustomAnvil.instance.getConfig(), 0,42,2,1);
        GuiItem setting1 = GuiGlobalItems.openSettingGuiItem(setting1Item, factory1);
        pane.bindItem('1', setting1);

        ItemStack setting2Item = new ItemStack(Material.STONE);
        AbstractSettingGui.SettingGuiFactory factory2 = BoolSettingsGui.factory("Test Gui bool",this, "test2", CustomAnvil.instance.getConfig(), false);
        GuiItem setting2 = GuiGlobalItems.openSettingGuiItem(setting2Item, factory2);
        pane.bindItem('2', setting2);
    }

}
