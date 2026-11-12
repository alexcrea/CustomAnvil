package xyz.alexcrea.cuanvil.gui.config.ask;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.lang.Message;

import java.util.function.Supplier;

@NotNullByDefault
public abstract class AbstractAskGui extends ChestGui {

    protected final PatternPane pane;

    AbstractAskGui(
            int rows,
            Message name, String param,
            Supplier<? extends Gui> backOnCancel
    ) {
        super(rows, name.textHolder(param), CustomAnvil.instance);

        Pattern pattern = getGuiPattern();
        this.pane = new PatternPane(pattern.getLength(), pattern.getHeight(), pattern);
        addPane(Slot.fromXY(0, 0), this.pane);

        this.pane.bindItem('0', GuiGlobalItems.backgroundItem());
        this.pane.bindItem('B', GuiGlobalItems.cancelAndGoBackItem(backOnCancel));
    }

    /**
     * Used to get the gui pattern.
     * Reserved character are:
     * <ul>
     * <li><b>B</b>: "cancel" button.</li>
     * <li><b>0</b>: default background item.</li>
     * </ul>
     *
     * @return The GUI's pattern.
     */
    protected abstract Pattern getGuiPattern();

}
