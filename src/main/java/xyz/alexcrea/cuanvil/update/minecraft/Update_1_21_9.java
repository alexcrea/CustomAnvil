package xyz.alexcrea.cuanvil.update.minecraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@NotNullByDefault
public class Update_1_21_9 extends MCUpdate {

    public Update_1_21_9() {
        super(new Version(1, 21, 9));
    }

    @Override
    protected void doUpdate(UpdateHandler.UpdatedConfigList toSave) {
        var baseConfig = toSave.use(ConfigHolder.DEFAULT).getConfig();
        var groupConfig = toSave.use(ConfigHolder.ITEM_GROUP).getConfig();
        var unitConfig = toSave.use(ConfigHolder.UNIT_REPAIR).getConfig();

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
    }

    public static void addCopperUnitRepair(FileConfiguration unitConfig) {
        // Add unit repair
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_helmet", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_chestplate", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_leggings", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_boots", 0.25);

        unitConfig.set("minecraft:copper_ingot.minecraft:copper_pickaxe", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_shovel", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_hoe", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_axe", 0.25);
        unitConfig.set("minecraft:copper_ingot.minecraft:copper_sword", 0.25);
    }

}
