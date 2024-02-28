package xyz.alexcrea.cuanvil.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class GuiGlobalActions {

    public static Consumer<InventoryClickEvent> stayInPlace = (event) -> event.setCancelled(true);


    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(
            @NotNull Class<? extends Gui> clazz,
            @NotNull Class<?>[] argClass,
            @NotNull Object... args){
        return event -> {
            event.setCancelled(true);
            try {
                Constructor<? extends Gui> constructor = clazz.getConstructor(argClass);
                // Assume constructor is accessible
                Gui gui = constructor.newInstance(args);
                gui.show(event.getWhoClicked());

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(
            @NotNull Class<? extends Gui> clazz){
        return openGuiAction(clazz, new Class<?>[0]);
    }

    public static @NotNull Consumer<InventoryClickEvent> openSettingGuiAction(AbstractSettingGui.SettingGuiFactory factory){
        return event -> {
            event.setCancelled(true);
            Gui gui = factory.create();
            gui.show(event.getWhoClicked());
        };
    }

    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(@NotNull Gui goal) {
        return event -> {
            event.setCancelled(true);
            goal.show(event.getWhoClicked());
        };
    }

    public static @NotNull Consumer<InventoryClickEvent> saveSettingAction(
            @NotNull AbstractSettingGui setting,
            @NotNull Gui goal) {
        return event -> {
            event.setCancelled(true);
            // Save setting
            setting.onSave();
            // Then show
            goal.show(event.getWhoClicked());
        };
    }
}
