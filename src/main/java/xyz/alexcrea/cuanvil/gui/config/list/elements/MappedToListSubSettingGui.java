package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.lang.MsgUI;

@NotNullByDefault
public abstract class MappedToListSubSettingGui extends ChestGui implements ValueUpdatableGui, ElementMappedToListGui {

    protected MappedToListSubSettingGui(
            int rows,
            String type
    ) {
        super(rows, MsgUI.INSTANCE.getSHARED_TYPED_CONFIG_TITLE().textHolder(type), CustomAnvil.instance);
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
