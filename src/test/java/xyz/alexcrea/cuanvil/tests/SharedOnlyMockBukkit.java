package xyz.alexcrea.cuanvil.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public class SharedOnlyMockBukkit {

    protected static ServerMock server;

    @BeforeAll
    public static void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
    }

    @AfterAll
    public static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

}
