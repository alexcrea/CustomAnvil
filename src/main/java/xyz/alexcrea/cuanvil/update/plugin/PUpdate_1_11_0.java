package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addToStringList;

public class PUpdate_1_11_0 {

    private static final List<String> mace_expected = List.of(
            "density",
            "breach",
            "smite",
            "bane_of_arthropods"
    );
    private static final List<String> sword_expected = List.of(
            "sharpness",
            "smite",
            "bane_of_arthropods"
    );

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        // We migrate the mace conflict if exist and unmodified
        if (!config.isConfigurationSection("sword_enchant_conflict")) return;
        if (!config.isConfigurationSection("mace_enchant_conflict")) return;

        ConfigurationSection mace_conflict = config.getConfigurationSection("mace_enchant_conflict");
        // Test mace conflict if default
        if (mace_conflict == null) return;
        if (mace_conflict.getInt("maxEnchantmentBeforeConflict", 0) != 1) return;

        if (mace_conflict.isList("notAffectedGroups") && !mace_conflict.getList("notAffectedGroups").isEmpty()) return;

        List<String> enchantments = mace_conflict.getStringList("enchantments");
        if (enchantments.size() != 4) return;
        for (String ench : mace_expected) {
            if(!enchantments.contains(ench) && !enchantments.contains("minecraft:" + ench)) return;
        }

        // Test sword_enchant_conflict is default
        ConfigurationSection sword_conflict = config.getConfigurationSection("sword_enchant_conflict");
        if (sword_conflict.getInt("maxEnchantmentBeforeConflict", 0) != 1) return;

        if (sword_conflict.isList("notAffectedGroups") && !sword_conflict.getList("notAffectedGroups").isEmpty()) return;

        enchantments = sword_conflict.getStringList("enchantments");
        if (enchantments.size() != 3) return;
        for (String ench : sword_expected) {
            if(!enchantments.contains(ench) && !enchantments.contains("minecraft:" + ench)) return;
        }

        // Finally we know both conflict are default. so we fix
        addToStringList(config, "sword_enchant_conflict.enchantments",
                "minecraft:density", "minecraft:breach");

        config.set("mace_enchant_conflict", null);

    }

}
