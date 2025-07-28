package xyz.alexcrea.cuanvil.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.api.ItemGroupApi;

/**
 * Called when the configuration of CustomAnvil is ready.
 * It is called either on the plugin startup or on the plugin config reload.
 * <p>
 * If you want to listen to the first trigger of this event (first configuration load. aka plugin load)
 * you will need to register the listener on your plugin onEnable or earlier
 * <p>
 * This event indicate that can start to register your recipes, item groups and conflicts.
 * The vanilla and custom enchantments should already have been provided to CustomAnvil.
 * Configuration can be changed any time after this event is triggered but never before.
 * <p>
 * use {@link xyz.alexcrea.cuanvil.api.ConflictAPI ConflictApi},
 * {@link xyz.alexcrea.cuanvil.gui.config.global.CustomRecipeConfigGui CustomRecipeConfigGui},
 * {@link ItemGroupApi ItemGroupApi}
 * and {@link xyz.alexcrea.cuanvil.api.UnitRepairApi UnitRepairApi}
 * to add/remove/edit configurations
 */
public class CAConfigReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
