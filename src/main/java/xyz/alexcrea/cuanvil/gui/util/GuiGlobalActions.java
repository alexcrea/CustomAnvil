package xyz.alexcrea.cuanvil.gui.util;

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

/**
 * A utility class to store function that create generic GUI actions.
 */
public class GuiGlobalActions {

    public final static String NO_EDIT_PERM = "§cYou do not have permission to edit the config";

    /**
     * A Consumer that should be used if the item goal is to do nothing on click.
     */
    public final static Consumer<InventoryClickEvent> stayInPlace = (event) -> event.setCancelled(true);

    /**
     * Create a consumer to create and open a new GUI.
     * Used with InventoryClickEvent as the consumer argument as it is planned to be used on click on an GuiItem.
     *
     * @param clazz    The class of the gui to open.
     *                 It is assumed this class contain a constructor requiring arguments of argClass in the same order as argClass array.
     * @param argClass Classes of the argument that will be passed to the constructor of the GUI class.
     * @param args     Arguments for the constructor the GUI class.
     * @return A consumer to create a new gui and open it.
     */
    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(
            @NotNull Class<? extends Gui> clazz,
            @NotNull Class<?>[] argClass,
            @NotNull Object... args) {
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
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

    /**
     * Create a consumer to create and open a new GUI.
     * Used with InventoryClickEvent as the consumer argument as it is planned to be used on click on an GuiItem.
     *
     * @param clazz The class of the gui to open.
     *              It is assumed this class contain a constructor with no argument.
     * @return A consumer to create a new gui and open it.
     */
    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(
            @NotNull Class<? extends Gui> clazz) {
        return openGuiAction(clazz, new Class<?>[0]);
    }

    /**
     * Create a consumer to open a setting gui from a setting GUI factory.
     * Used with InventoryClickEvent as the consumer argument as it is planned to be used on click on an GuiItem.
     *
     * @param factory The setting gui factory.
     * @return A consumer to create and open a new setting GUI.
     */
    public static @NotNull Consumer<InventoryClickEvent> openSettingGuiAction(AbstractSettingGui.SettingGuiFactory factory) {
        return event -> {
            event.setCancelled(true);
            Gui gui = factory.create();
            gui.show(event.getWhoClicked());
        };
    }

    /**
     * Create a consumer to open a global GUI.
     * Used with InventoryClickEvent as the consumer argument as it is planned to be used on click on an GuiItem.
     *
     * @param goal The gui to open when consumer is run.
     * @return A consumer to open a global GUI.
     */
    public static @NotNull Consumer<InventoryClickEvent> openGuiAction(@NotNull Gui goal) {
        return event -> {
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(NO_EDIT_PERM);
                return;
            }
            event.setCancelled(true);
            goal.show(player);
        };
    }

    /**
     * Create a consumer to update and open an updatable GUI.
     * Used with InventoryClickEvent as the consumer argument as it is planned to be used on click on an GuiItem.
     * This consumer check if the player who interacted with the item have the permission to save before saving.
     *
     * @param setting The gui that contain the modified setting.
     * @param goal    The gui to update and open when consumer is run.
     * @return A consumer to open a global GUI.
     */
    public static @NotNull Consumer<InventoryClickEvent> saveSettingAction(
            @NotNull AbstractSettingGui setting,
            @NotNull ValueUpdatableGui goal) {
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to save configuration if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(NO_EDIT_PERM);
                return;
            }

            // Save setting
            if (!setting.onSave()) {
                player.sendMessage("\u00A7cSomething went wrong while saving the change of value.");
            }
            // Update gui for those who have it open.
            goal.updateGuiValues();
            // Then show
            goal.show(player);
        };
    }

}
