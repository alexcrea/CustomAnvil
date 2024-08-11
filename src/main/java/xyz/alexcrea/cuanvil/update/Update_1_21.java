package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addToStringList;

// This is a temporary class that aim to handle 1.21 update.
// It will be replaced by a better system later.
public class Update_1_21 {

    public static void handleUpdate(){
        // Assume if version path is not null then it's 1.21
        String oldVersion = ConfigHolder.DEFAULT_CONFIG.getConfig().getString(UpdateUtils.MINECRAFT_VERSION_PATH);
        if(oldVersion != null){
            int[] versionParts = UpdateUtils.readVersionFromString(oldVersion);

            // Test 1.21
            if((versionParts[0] >= 1) && (versionParts[1] >= 21)){
                return;
            }
        }

        int[] versionParts = UpdateUtils.currentMinecraftVersion();

        // Test 1.21
        if((versionParts[0] >= 1) && (versionParts[1] >= 21)){
            doUpdate();
        }

    }

    private static void doUpdate() {
        CustomAnvil.instance.getLogger().info("Updating config to support 1.21 ...");

        FileConfiguration baseConfig = ConfigHolder.DEFAULT_CONFIG.getConfig();
        FileConfiguration groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        FileConfiguration conflictConfig = ConfigHolder.CONFLICT_HOLDER.getConfig();

        // Add mace to groups
        groupConfig.set("mace.type", "include");
        addToStringList(groupConfig, "mace.items", "mace");

        addToStringList(groupConfig, "can_unbreak.groups", "mace");

        // Add new enchant conflicts
        addToStringList(conflictConfig, "restriction_density.enchantments", "density");
        addToStringList(conflictConfig, "restriction_density.notAffectedGroups", "mace");

        addToStringList(conflictConfig, "restriction_breach.enchantments", "breach");
        addToStringList(conflictConfig, "restriction_breach.notAffectedGroups", "mace");

        addToStringList(conflictConfig, "restriction_wind_burst.enchantments", "wind_burst");
        addToStringList(conflictConfig, "restriction_wind_burst.notAffectedGroups", "mace");

        // Add mace to conflicts
        addToStringList(conflictConfig, "restriction_fire_aspect.notAffectedGroups", "mace");
        addToStringList(conflictConfig, "restriction_smite.notAffectedGroups", "mace");
        addToStringList(conflictConfig, "restriction_bane_of_arthropods.notAffectedGroups", "mace");

        addToStringList(conflictConfig, "mace_enchant_conflict.enchantments", "density", "breach", "smite", "bane_of_arthropods");
        conflictConfig.set("mace_enchant_conflict.maxEnchantmentBeforeConflict", 1);

        // Add level limit
        baseConfig.set("enchant_limits.density", 5);
        baseConfig.set("enchant_limits.breach", 4);
        baseConfig.set("enchant_limits.wind_burst", 3);

        // Add enchant values
        baseConfig.set("enchant_values.density.item", 1);
        baseConfig.set("enchant_values.density.book", 1);

        baseConfig.set("enchant_values.breach.item", 4);
        baseConfig.set("enchant_values.breach.book", 2);

        baseConfig.set("enchant_values.wind_burst.item", 4);
        baseConfig.set("enchant_values.wind_burst.book", 2);

        // Set version string as 1.21
        baseConfig.set(UpdateUtils.MINECRAFT_VERSION_PATH, "1.21");

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
        ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);
        ConfigHolder.CONFLICT_HOLDER.saveToDisk(true);

        // imply reload of CONFLICT_HOLDER
        // We also do not need to reload base config as there is no object related to it.
        ConfigHolder.ITEM_GROUP_HOLDER.reload();

        CustomAnvil.instance.getLogger().info("Updating Done !");

    }

}
