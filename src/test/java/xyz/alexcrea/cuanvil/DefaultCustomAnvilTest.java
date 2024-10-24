package xyz.alexcrea.cuanvil;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.delilaheve.CustomAnvil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;

import java.util.ArrayList;
import java.util.List;

public class DefaultCustomAnvilTest {

    protected ServerMock server;
    protected CustomAnvil plugin;

    @BeforeEach
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
        // Load your plugin
        plugin = MockBukkit.load(CustomAnvil.class);
        // Continue initialization of the plugin
        server.getScheduler().performOneTick();
    }

    @AfterEach
    public void tearDown() {
        // Set config to null
        ConfigHolder.DEFAULT_CONFIG = null;
        ConfigHolder.ITEM_GROUP_HOLDER = null;
        ConfigHolder.CONFLICT_HOLDER = null;
        ConfigHolder.UNIT_REPAIR_HOLDER = null;
        ConfigHolder.CUSTOM_RECIPE_HOLDER = null;
        
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
