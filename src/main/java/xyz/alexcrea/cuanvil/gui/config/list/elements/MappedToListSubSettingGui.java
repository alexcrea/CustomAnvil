package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;

public abstract class MappedToListSubSettingGui extends ValueUpdatableGui implements ElementMappedToListGui {

    private final GuiItem item;
    public MappedToListSubSettingGui(
            GuiItem item,
            int rows,
            @NotNull String title) {
        super(rows, title, CustomAnvil.instance);
        this.item = item;
    }

    @Override
    public GuiItem getParentItemForThisGui() {
        return item;
    }

    @Override
    public Gui getMappedGui() {
        return this;
    }


}
