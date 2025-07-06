package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.jetbrains.annotations.NotNull;

public class CAPreAnvilBypassEvent extends Event implements Cancellable {

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

    @NotNull
    public PrepareAnvilEvent getEvent() {
        return event;
    }

    public CAPreAnvilBypassEvent(@NotNull PrepareAnvilEvent event) {
        this.event = event;
    }

}
