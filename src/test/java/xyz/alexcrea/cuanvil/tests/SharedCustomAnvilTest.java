package xyz.alexcrea.cuanvil.tests;

import io.delilaheve.CustomAnvil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;

import java.util.ArrayList;
import java.util.List;

public abstract class SharedCustomAnvilTest {

    protected static ServerMock server;
    protected static CustomAnvil plugin;

    @BeforeAll
    public static void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
        // Load your plugin
        plugin = MockBukkit.load(CustomAnvil.class);
        // Continue initialization of the plugin
        server.getScheduler().performOneTick();
    }

    @AfterAll
    public static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();

        // Unregister enchantments
        List<CAEnchantment> toUnregister = new ArrayList<>(
                CAEnchantmentRegistry.getInstance().values()
        );

        for (CAEnchantment caEnchantment : toUnregister) {
            CAEnchantmentRegistry.getInstance().unregister(caEnchantment);
        }

    }

}
