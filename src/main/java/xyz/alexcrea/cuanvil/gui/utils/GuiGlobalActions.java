package xyz.alexcrea.cuanvil.gui.utils;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class GuiGlobalActions {

    public final static String NO_EDIT_PERM = "§cYou do not have permission to edit the config";
    public final static Consumer<InventoryClickEvent> stayInPlace = (event) -> event.setCancelled(true);


    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(
            @NotNull Class<? extends Gui> clazz,
            @NotNull Class<?>[] argClass,
            @NotNull Object... args){
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(NO_EDIT_PERM);
                return;
            }
            try {
                Constructor<? extends Gui> constructor = clazz.getConstructor(argClass);
                // Assume constructor is accessible
                Gui gui = constructor.newInstance(args);
                gui.show(player);

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
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(NO_EDIT_PERM);
                return;
            }
            event.setCancelled(true);
            goal.show(player);
        };
    }

    public static @NotNull Consumer<InventoryClickEvent> saveSettingAction(
            @NotNull AbstractSettingGui setting,
            @NotNull ValueUpdatableGui goal) {
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to save configuration if player do not have edit configuration permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(NO_EDIT_PERM);
                return;
            }

            // Save setting
            if(!setting.onSave()){
                player.sendMessage("\u00A7cSomething wrong happen while saving the change of value.");
            }
            // Update gui for the one who have it open
            goal.updateGuiValues();
            // Then show
            goal.show(player);
        };
    }
}
