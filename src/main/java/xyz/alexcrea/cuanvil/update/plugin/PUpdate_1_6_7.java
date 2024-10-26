package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.Set;

public class PUpdate_1_6_7 {

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        // We fix the density enchantment
        String value = config.getString("enchant_values.minecraft:density.item");
        if(value == null) value = config.getString("enchant_values.density.item");

        if(value == null || "1".equalsIgnoreCase(value)){
            config.set("enchant_values.minecraft:density.item", 2);

            toSave.add(ConfigHolder.DEFAULT_CONFIG);
        }


    }

}
