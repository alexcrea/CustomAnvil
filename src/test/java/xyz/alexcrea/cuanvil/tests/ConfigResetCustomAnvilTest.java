package xyz.alexcrea.cuanvil.tests;

import org.junit.jupiter.api.AfterEach;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import java.io.File;

public abstract class ConfigResetCustomAnvilTest extends DefaultCustomAnvilTest {

    @Override
    @AfterEach
    public void tearDown() {
        // Destroy saved config file
        String[] configs = new String[]{
                "config.yml",
                "item_groups.yml",
                "enchant_conflict.yml",
                "unit_repair_item.yml",
                "custom_recipes.yml"
        };
        for (String config : configs) {
            File configFile = new File(plugin.getDataFolder(), config);
            configFile.delete();
        }

        // Set config to null
        ConfigHolder.DEFAULT_CONFIG = null;
        ConfigHolder.ITEM_GROUP_HOLDER = null;
        ConfigHolder.CONFLICT_HOLDER = null;
        ConfigHolder.UNIT_REPAIR_HOLDER = null;
        ConfigHolder.CUSTOM_RECIPE_HOLDER = null;

        // Do parent works
        super.tearDown();
    }


}
