package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;

public interface ElementMappedToListGui {

    GuiItem getParentItemForThisGui();

    void updateLocal();

    void cleanUnused();

    Gui getMappedGui();

}
