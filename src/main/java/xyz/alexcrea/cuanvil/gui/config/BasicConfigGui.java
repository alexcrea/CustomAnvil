package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.GuiGlobalItems;

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
                "000000000",
                "B00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);
        GuiGlobalItems.addBackgroundItem(pane);

    }

}
