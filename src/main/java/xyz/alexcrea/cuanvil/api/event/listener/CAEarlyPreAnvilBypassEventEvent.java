package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.jetbrains.annotations.NotNull;

public class CAEarlyPreAnvilBypassEventEvent extends CAPreAnvilBypassEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public CAEarlyPreAnvilBypassEventEvent(@NotNull PrepareAnvilEvent event) {
        super(event);
    }

}
