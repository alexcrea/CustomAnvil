package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public interface ElementMappedToListGui {

    void updateLocal();

    void cleanAndBeUnusable();

    Gui getMappedGui();

}
