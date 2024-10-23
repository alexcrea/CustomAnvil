package io.delilaheve;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomAnvilTests {

    private ServerMock server;

    private CustomAnvil plugin;

    @BeforeEach
    public void setUp() {

        // Start the mock server
        server = MockBukkit.mock();

        // Load your plugin
        plugin = MockBukkit.load(CustomAnvil.class);



    }


    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();

    }


    @Test
    public void simpleInitTest() {
        // Continue initialization of the plugin
        server.getScheduler().performOneTick();

    }

}
