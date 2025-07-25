package xyz.alexcrea.cuanvil.api.event.listener;

import io.delilaheve.CustomAnvil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher;
import xyz.alexcrea.cuanvil.tests.SharedOnlyMockBukkit;

public abstract class NoStartupTriggerEventTest extends SharedOnlyMockBukkit {

    public abstract Class<? extends Event> getEventClass();

    public CustomAnvil plugin;
    public Player player;
    public AnvilInventory anvil;

    @BeforeEach
    public void setUpPost() {
        Class<? extends Event> eventClass = getEventClass();
        PluginManagerFiredEventClassMatcher matcher = PluginManagerFiredEventClassMatcher
                .hasFiredEventInstance(eventClass);

        boolean beforeStart = matcher.matches(server.getPluginManager());
        Assertions.assertFalse(beforeStart, "Somehow, event fired before plugin being loaded ?");

        // Load the plugin
        plugin = MockBukkit.load(CustomAnvil.class);

        // Config load phase
        server.getScheduler().performOneTick();

        boolean postConfig = matcher.matches(server.getPluginManager());
        Assertions.assertFalse(postConfig, "Event fired before plugin finished being loaded");

        // Simple pre anvil test
        player = server.addPlayer();
        anvil = (AnvilInventory) server.createInventory(player, InventoryType.ANVIL);
        player.openInventory(anvil);
    }

    @AfterEach
    public void tearDownPost() {
        if (plugin != null) {
            server.getPluginManager().disablePlugin(plugin);
            plugin = null;
        }
    }

}
