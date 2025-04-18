package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;

public abstract class MappedToListSubSettingGui extends ChestGui implements ValueUpdatableGui, ElementMappedToListGui {

    protected MappedToListSubSettingGui(
            int rows,
            @NotNull String title) {
        super(rows, title, CustomAnvil.instance);
    }

    @Override
    public Gui getMappedGui() {
        return this;
    }

    @Override
    public Gui getConnectedGui() {
        return this;
    }

}
