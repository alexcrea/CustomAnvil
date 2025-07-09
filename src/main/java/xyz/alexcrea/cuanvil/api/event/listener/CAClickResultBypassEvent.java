package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called before custom anvil process the click on the result on the anvil inventory.
 * <p>
 * This event is called after checking that the inventory is an anvil inventory and that the click is on the result slot
 * but before checking if the player has the custom anvil affected permission.
 * <p>
 * This event being cancelled will make CustomAnvil abort the click on result process.
 * <p>
 * Most of the time you would likely need {@link CAPreAnvilBypassEvent} or {@link CAEarlyPreAnvilBypassEvent}
 * for this event to be useful.
 * <p>
 * There is also {@link CATreatAnvilResultEvent} that may be better for some use case.
 * <p>
 * This is part of
 * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#LISTENER_EVENTS_V1 LISTENER_EVENTS_V1}
 */
public class CAClickResultBypassEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    private final InventoryClickEvent event;

    /**
     * Get the bukkit inventory click event causing to this event
     *
     * @return The click event causing to this event
     */
    @NotNull
    public InventoryClickEvent getEvent() {
        return event;
    }

    public CAClickResultBypassEvent(@NotNull InventoryClickEvent event) {
        this.event = event;
    }
}
