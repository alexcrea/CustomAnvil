package xyz.alexcrea.cuanvil.tests;

import io.delilaheve.CustomAnvil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultCustomAnvilTest {

    protected static ServerMock server;
    protected CustomAnvil plugin;

    @BeforeAll
    public static void setupMock() {
        server = MockBukkit.mock();
    }

    @BeforeEach
    public void setUp() {
        // Load your plugin
        plugin = MockBukkit.load(CustomAnvil.class);
        // Continue initialization of the plugin
        server.getScheduler().performOneTick();
    }

    @AfterEach
    public void tearDown() {
        // Unregister enchantments
        List<CAEnchantment> toUnregister = new ArrayList<>(
                CAEnchantmentRegistry.getInstance().values()
        );

        for (CAEnchantment caEnchantment : toUnregister) {
            CAEnchantmentRegistry.getInstance().unregister(caEnchantment);
        }

        server.getPluginManager().disablePlugin(plugin);
    }

    @AfterAll
    public static void tearDownMock() {
        // Stop the mock server
        MockBukkit.unmock();
    }

}
