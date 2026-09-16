package xyz.alexcrea.cuanvil.update.minecraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.*;

@NotNullByDefault
public class Update_1_20_5 extends MCUpdate {

    public Update_1_20_5() {
        super(new Version(1, 20, 5));
    }

    @Override
    protected void doUpdate(UpdateHandler.UpdatedConfigList toSave) {
        updateName(toSave);
    }

    private static final String SWEEPING_ENCHANTS = "restriction_sweeping_edge.enchantments";

    public static void updateName(UpdateHandler.UpdatedConfigList toSave) {
        var config = toSave.use(ConfigHolder.DEFAULT).getConfig();
        var unitConfig = toSave.use(ConfigHolder.UNIT_REPAIR).getConfig();
        var conflict = toSave.use(ConfigHolder.CONFLICT).getConfig();

        // Rename sweeping to sweeping_edge
        migrateEnchantLimit(config, "minecraft:sweeping");
        migrateEnchantLimit(config, "sweeping");

        migrateEnchantValues(config, "minecraft:sweeping");
        migrateEnchantValues(config, "sweeping");

        if(removeFromList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping"))
            addAbsentToList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping_edge");
        if(removeFromList(conflict, SWEEPING_ENCHANTS, "sweeping"))
            addAbsentToList(conflict, SWEEPING_ENCHANTS, "minecraft:sweeping_edge");

        // Rename scute to turtle_scute
        migrateUnitRepair(unitConfig, "minecraft:scute");
        migrateUnitRepair(unitConfig, "scute");
    }

    private static void migrateEnchantLimit(FileConfiguration config, String path) {
        var finalPath = "enchant_limits." + path;
        if(!config.isInt(finalPath)) return;
        var value = config.getInt(finalPath);

        config.set(finalPath, null);
        if(!config.contains("enchant_limits.minecraft:sweeping_edge")) return;
        if(!config.contains("enchant_limits.sweeping_edge")) return;

        config.set("enchant_limits.minecraft:sweeping_edge", value);
    }

    private static void migrateEnchantValues(FileConfiguration config, String path) {
        migrateEnchantValues(config, path, "item");
        migrateEnchantValues(config, path, "book");
    }

    private static void migrateEnchantValues(FileConfiguration config, String path, String child) {
        var finalPath = "enchant_values." + path + "." + child;
        if(config.isInt(finalPath)) return;
        var value = config.getInt(finalPath);

        config.set(finalPath, null);
        if(!config.contains("enchant_values.minecraft:sweeping_edge." + child)) return;
        if(!config.contains("enchant_values.sweeping_edge." + child)) return;

        config.set("enchant_values.minecraft:sweeping_edge." + child, value);
    }

    private static void migrateUnitRepair(FileConfiguration config, String path) {
        var section = config.getConfigurationSection(path);
        if(section == null) return;

        config.set(path, null);
        var turtle_scute = config.getConfigurationSection("minecraft:turtle_scute");
        if(turtle_scute == null) config.set("minecraft:turtle_scute", section);
        else mergeSections(section, turtle_scute);
    }

}
