package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class PUpdate_1_15_5 {

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();

        if (config.isConfigurationSection("restriction_luck_of_the_sea")) return;

        // We fix the luck of the see enchantment
        addAbsentToList(config, "restriction_luck_of_the_sea.enchantments",
                "minecraft:luck_of_the_sea");
        addAbsentToList(config, "restriction_luck_of_the_sea.notAffectedGroups",
                "enchanted_book", "fishing_rod");

        toSave.add(ConfigHolder.CONFLICT_HOLDER);
    }

}
