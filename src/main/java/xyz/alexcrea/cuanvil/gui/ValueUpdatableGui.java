package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public interface ValueUpdatableGui {

    void updateGuiValues();

    Gui getConnectedGui();

}
