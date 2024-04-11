package xyz.alexcrea.cuanvil.gui.config.ask;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

public abstract class AbstractAskGui extends ChestGui {

    protected PatternPane pane;
    AbstractAskGui(int rows, @NotNull String name,
                   Gui backOnCancel){
        super(rows, name, CustomAnvil.instance);

        Pattern pattern = getGuiPattern();
        this.pane = new PatternPane(0, 0, pattern.getLength(), pattern.getHeight(), pattern);
        addPane(this.pane);

        this.pane.bindItem('0', GuiGlobalItems.backgroundItem());
        this.pane.bindItem('B', new GuiItem(GuiSharedConstant.CANCEL_ITEM, GuiGlobalActions.openGuiAction(backOnCancel), CustomAnvil.instance));

    }

    /**
     * Used to get the gui pattern.
     * Reserved character are:
     * <ul>
     * <li><b>B</b>: "cancel" button.</li>
     * <li><b>0</b>: default background item.</li>
     * </ul>
     *
     * @return The gui's pattern.
     */
    protected abstract Pattern getGuiPattern();

}
