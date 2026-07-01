package xyz.alexcrea.cuanvil.update.minecraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.Version;

import java.util.HashSet;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.*;

public class Update_1_20_5 extends MCUpdate {

    public Update_1_20_5() {
        super(new Version(1, 20, 5));
    }

    @Override
    protected void doUpdate() {
        var tosave = new HashSet<ConfigHolder>();
        updateName(tosave);

        for (ConfigHolder holder : tosave) {
            holder.saveToDisk(true);
        }
    }

    private static String SWEEPING_ENCHANTS = "restriction_sweeping_edge.enchantments";

    public static void updateName(@NotNull Set<ConfigHolder> tosave) {
        var config = ConfigHolder.DEFAULT_CONFIG.getConfig();
        var unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        var conflict = ConfigHolder.CONFLICT_HOLDER.getConfig();

        // Rename sweeping to sweeping_edge
        migrateEnchantLimit(config, "minecraft:sweeping");
        migrateEnchantLimit(config, "sweeping");

        migrateEnchantValues(config, "minecraft:sweeping");
        migrateEnchantValues(config, "sweeping");

        if (removeFromList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping"))
            addAbsentToList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping_edge");
        if (removeFromList(conflict, SWEEPING_ENCHANTS, "sweeping"))
            addAbsentToList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping_edge");

        // Rename scute to turtle_scute
        migrateUnitRepair(unitConfig, "minecraft:scute");
        migrateUnitRepair(unitConfig, "scute");

        ConfigHolder.ITEM_GROUP_HOLDER.reload();

        tosave.add(ConfigHolder.DEFAULT_CONFIG);
        tosave.add(ConfigHolder.UNIT_REPAIR_HOLDER);
        tosave.add(ConfigHolder.CONFLICT_HOLDER);
    }

    private static void migrateEnchantLimit(FileConfiguration config, String path) {
        var finalPath = "enchant_limits." + path;
        if (!config.isInt(finalPath)) return;
        var value = config.getInt(finalPath);

        config.set(finalPath, null);
        if (!config.contains("enchant_limits.minecraft:sweeping_edge")) return;
        if (!config.contains("enchant_limits.sweeping_edge")) return;

        config.set("enchant_limits.minecraft:sweeping_edge", value);
    }

    private static void migrateEnchantValues(FileConfiguration config, String path) {
        migrateEnchantValues(config, path, "item");
        migrateEnchantValues(config, path, "book");
    }

    private static void migrateEnchantValues(FileConfiguration config, String path, String child) {
        var finalPath = "enchant_values." + path + "." + child;
        if (config.isInt(finalPath)) return;
        var value = config.getInt(finalPath);

        config.set(finalPath, null);
        if (!config.contains("enchant_values.minecraft:sweeping_edge." + child)) return;
        if (!config.contains("enchant_values.sweeping_edge." + child)) return;

        config.set("enchant_values.minecraft:sweeping_edge." + child, value);
    }

    private static void migrateUnitRepair(FileConfiguration config, String path) {
        var section = config.getConfigurationSection(path);
        if (section == null) return;

        config.set(path, null);
        var turtle_scute = config.getConfigurationSection("minecraft:turtle_scute");
        if (turtle_scute == null) config.set("minecraft:turtle_scute", section);
        else mergeSections(section, turtle_scute);
    }

}
