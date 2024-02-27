package xyz.alexcrea.cuanvil.gui.gui.config;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.gui.gui.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.gui.MainConfigGui;

public class BasicConfigGui extends ChestGui {

    public final static BasicConfigGui INSTANCE = new BasicConfigGui();

    private BasicConfigGui(){
        super(3, "Basic Config GUI", CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "111111111",
                "111111111",
                "B11111111"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);

    }

}
