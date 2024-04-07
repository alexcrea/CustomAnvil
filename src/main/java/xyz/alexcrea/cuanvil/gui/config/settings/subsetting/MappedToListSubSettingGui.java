package xyz.alexcrea.cuanvil.gui.config.settings.subsetting;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;

public abstract class MappedToListSubSettingGui extends ValueUpdatableGui {

    private final GuiItem item;
    public MappedToListSubSettingGui(
            GuiItem item,
            int rows,
            @NotNull String title) {
        super(rows, title, CustomAnvil.instance);
        this.item = item;
    }


    public GuiItem getParentItemForThisGui() {
        return item;
    }


    public abstract void updateLocal(); // TODO

    public abstract void cleanUnused(); // TODO
}
