package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called before custom anvil process the prepare anvil event.
 * <p>
 * This event will always get called when CustomAnvil need to handle
 * <p>
 * This event being cancelled will make CustomAnvil abort the anvil process.
 * <p>
 * You should also use {@link CAClickResultBypassEvent} if you want to use this event for something useful.
 * <p>
 * It is also recommended that you read about {@link CAPreAnvilBypassEvent} and {@link CATreatAnvilResultEvent}
 * as your use case may be more prone to use theses.
 * <p>
 * This is part of {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#LISTENER_EVENTS_V1 LISTENER_EVENTS_V1}
 */
public class CAEarlyPreAnvilBypassEvent extends Event implements Cancellable {

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
    private final PrepareAnvilEvent event;

    /**
     * Get the bukkit pre anvil event causing this event
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#LISTENER_EVENTS_V1 LISTENER_EVENTS_V1}
     *
     * @return The pre anvil event causing to this event
     */
    @NotNull
    public PrepareAnvilEvent getEvent() {
        return event;
    }

    public CAEarlyPreAnvilBypassEvent(@NotNull PrepareAnvilEvent event) {
        this.event = event;
    }

}
