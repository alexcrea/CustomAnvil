package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher;
import xyz.alexcrea.cuanvil.anvil.AnvilFuseTests;

public class CAEarlyPreAnvilBypassEventTest extends NoStartupTriggerEventTest {

    @Override
    public Class<? extends Event> getEventClass() {
        return CAEarlyPreAnvilBypassEvent.class;
    }

    @Test
    public void startup() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEvent(CAEarlyPreAnvilBypassEvent event){
                event.setCancelled(true);
            }

        }, plugin);

        AnvilFuseTests.mergeFuseData(4, 4, null)
                .executeTest(anvil, player);

        boolean postEventEarly = PluginManagerFiredEventClassMatcher
                .hasFiredEventInstance(CAEarlyPreAnvilBypassEvent.class)
                .matches(server.getPluginManager());

        boolean postEvent = PluginManagerFiredEventClassMatcher
                .hasFiredEventInstance(CAPreAnvilBypassEvent.class)
                .matches(server.getPluginManager());

        Assertions.assertTrue(postEventEarly, "early pre event did not get fired");
        Assertions.assertFalse(postEvent, "pre event did get fired even with early cancellation");
    }

}