package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.function.Supplier;

@NotNullByDefault
public interface ValueUpdatableGui {

    void updateGuiValues();

    Supplier<? extends Gui> getConnectedGui();

}
