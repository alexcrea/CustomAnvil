package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class ValueUpdatableGui extends ChestGui {

    public ValueUpdatableGui(int rows, @NotNull String title, @NotNull Plugin plugin) {
        super(rows, title, plugin);
    }

    public ValueUpdatableGui(int rows, @NotNull TextHolder title, @NotNull Plugin plugin) {
        super(rows, title, plugin);
    }

    public abstract void updateGuiValues();

}
