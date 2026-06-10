package io.delilaheve;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.tests.DefaultCustomAnvilTest;

public class CustomAnvilTests extends DefaultCustomAnvilTest {

    @Test
    public void simpleInitTest() {
        try {
            Assertions.assertNotNull(server);
            Assertions.assertNotNull(plugin);

            // Test shutdown
            plugin.onDisable();
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

}
