package xyz.alexcrea.cuanvil.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when custom anvil is ready to accept registration on custom enchantment.
 * <p>
 * If you want to listen this event
 * you will need to register the listener on your plugin onEnable or earlier
 * <p>
 * Custom enchantments may be registered later but may cause issue if registered too later
 * (after configuration loading phase. see {@link CAConfigReadyEvent})
 * <p>
 * use {@link xyz.alexcrea.cuanvil.api.EnchantmentApi EnchantmentApi} to register and unregister your custom enchantments
 */
public class CAEnchantRegistryReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
