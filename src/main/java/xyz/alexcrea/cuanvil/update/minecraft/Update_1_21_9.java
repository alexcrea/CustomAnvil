package xyz.alexcrea.cuanvil.update.minecraft;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class Update_1_21_9 extends MCUpdate{

    public Update_1_21_9() {
        super(new Version(1, 21, 9));
    }

    @Override
    protected void doUpdate() {
        var baseConfig = ConfigHolder.DEFAULT_CONFIG.getConfig();
        var groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        var unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        // Add cooper items to groups
        addAbsentToList(groupConfig, "helmets.items", "copper_helmet");
        addAbsentToList(groupConfig, "chestplate.items", "copper_chestplate");
        addAbsentToList(groupConfig, "leggings.items", "copper_leggings");
        addAbsentToList(groupConfig, "boots.items", "copper_boots");

        addAbsentToList(groupConfig, "pickaxes.items", "copper_pickaxe");
        addAbsentToList(groupConfig, "shovels.items", "copper_shovel");
        addAbsentToList(groupConfig, "hoes.items", "copper_hoe");
        addAbsentToList(groupConfig, "axes.items", "copper_axe");
        addAbsentToList(groupConfig, "swords.items", "copper_sword");

        // Add unit repair
        addCopperUnitRepair(unitConfig);

        // Set version string as current
        baseConfig.set(UpdateUtils.MINECRAFT_VERSION_PATH, version.toString());

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
        ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);
        ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(true);

        // imply reload of CONFLICT_HOLDER
        // We also do not need to reload base config as there is no object related to it.
        ConfigHolder.ITEM_GROUP_HOLDER.reload();
    }

    public static void addCopperUnitRepair(FileConfiguration unitConfig) {
        // Add unit repair
        unitConfig.set("copper_ingot.copper_helmet", 0.25);
        unitConfig.set("copper_ingot.copper_chestplate", 0.25);
        unitConfig.set("copper_ingot.copper_leggings", 0.25);
        unitConfig.set("copper_ingot.copper_boots", 0.25);

        unitConfig.set("copper_ingot.copper_pickaxe", 0.25);
        unitConfig.set("copper_ingot.copper_shovel", 0.25);
        unitConfig.set("copper_ingot.copper_hoe", 0.25);
        unitConfig.set("copper_ingot.copper_axe", 0.25);
        unitConfig.set("copper_ingot.copper_sword", 0.25);
    }

}
