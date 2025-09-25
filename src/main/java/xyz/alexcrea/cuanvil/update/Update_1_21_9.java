package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class Update_1_21_9 {

    private static final Version V1_21_9 = new Version(1, 21, 9);

    public static boolean handleUpdate(Version current){
        // Test if we are running in 1.21.9 or better
        if(V1_21_9.greaterThan(current))
            return false;

        // if version path is not null then check if its it's before 1.21.9
        String oldVersion = ConfigHolder.DEFAULT_CONFIG.getConfig().getString(UpdateUtils.MINECRAFT_VERSION_PATH);
        if(oldVersion != null){
            var version = Version.fromString(oldVersion);
            if(V1_21_9.lesserEqual(version)) return false;
        }

        doUpdate();
        return true;
    }

    private static void doUpdate() {
        CustomAnvil.instance.getLogger().info("Updating config to support 1.21.9 ...");

        var baseConfig = ConfigHolder.DEFAULT_CONFIG.getConfig();
        var groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();

        // Add mace to groups
        addAbsentToList(groupConfig, "helmets.items", "copper_helmet");
        addAbsentToList(groupConfig, "chestplate.items", "copper_chestplate");
        addAbsentToList(groupConfig, "leggings.items", "copper_leggings");
        addAbsentToList(groupConfig, "boots.items", "copper_boots");

        addAbsentToList(groupConfig, "pickaxes.items", "copper_pickaxe");
        addAbsentToList(groupConfig, "shovels.items", "copper_shovel");
        addAbsentToList(groupConfig, "hoes.items", "copper_hoe");
        addAbsentToList(groupConfig, "axes.items", "copper_axe");
        addAbsentToList(groupConfig, "swords.items", "copper_sword");

        // Set version string as 1.21
        baseConfig.set(UpdateUtils.MINECRAFT_VERSION_PATH, V1_21_9.toString());

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
        ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);

        // imply reload of CONFLICT_HOLDER
        // We also do not need to reload base config as there is no object related to it.
        ConfigHolder.ITEM_GROUP_HOLDER.reload();
    }

}
