package xyz.alexcrea.cuanvil.api.event;

import io.delilaheve.CustomAnvil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher;
import xyz.alexcrea.cuanvil.tests.SharedOnlyMockBukkit;

public class CAConfigReadyEventTest extends SharedOnlyMockBukkit {

    private CustomAnvil plugin;

    @Test
    public void startup() {
        boolean beforeStart = PluginManagerFiredEventClassMatcher
                .hasNotFiredEventInstance(CAConfigReadyEvent.class)
                .matches(server.getPluginManager());

        Assertions.assertTrue(beforeStart, "Somehow, event fired before plugin being loaded ?");

        // Load the plugin
        plugin = MockBukkit.load(CustomAnvil.class);

        boolean postStart = PluginManagerFiredEventClassMatcher
                .hasNotFiredEventInstance(CAConfigReadyEvent.class)
                .matches(server.getPluginManager());

        Assertions.assertTrue(postStart, "Event fired before plugin finished being loaded");

        // Config load phase
        server.getScheduler().performOneTick();
        boolean postConfig = PluginManagerFiredEventClassMatcher
                .hasFiredEventInstance(CAConfigReadyEvent.class)
                .matches(server.getPluginManager());

        Assertions.assertTrue(postConfig, "Event did not fire after the config phase");
    }

    @AfterEach
    public void pluginTeardown() {
        if (plugin != null) {
            server.getPluginManager().disablePlugin(plugin);
            plugin = null;
        }
    }

}
