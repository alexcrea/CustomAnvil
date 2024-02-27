package xyz.alexcrea.cuanvil.gui.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class GuiGlobalActions {

    public static Consumer<InventoryClickEvent> stayInPlace = (event) -> event.setCancelled(true);


    public static Consumer<InventoryClickEvent> openGuiFactory(
            Class<? extends Gui> clazz,
            Class<?>[] argClass,
            Object... args){
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

    public static Consumer<InventoryClickEvent> openGuiFactory(
            Class<? extends Gui> clazz){
        return openGuiFactory(clazz, new Class<?>[0]);
    }

    public static Consumer<InventoryClickEvent> openGuiFactory(Gui goal) {
        return event -> {
            event.setCancelled(true);
            goal.show(event.getWhoClicked());
        };
    }
}
