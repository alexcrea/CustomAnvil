package xyz.alexcrea.cuanvil.update.minecraft;

import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class Update_1_21 extends MCUpdate {

    public Update_1_21() {
        super(new Version(1, 21));
    }

    @Override
    protected void doUpdate() {
        var baseConfig = ConfigHolder.DEFAULT_CONFIG.getConfig();
        var groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        var conflictConfig = ConfigHolder.CONFLICT_HOLDER.getConfig();
        var unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        // Add mace to groups
        groupConfig.set("mace.type", "include");
        addAbsentToList(groupConfig, "mace.items", "mace");

        addAbsentToList(groupConfig, "can_unbreak.groups", "mace");

        // Add new enchant conflicts
        addAbsentToList(conflictConfig, "restriction_density.enchantments", "minecraft:density");
        addAbsentToList(conflictConfig, "restriction_density.notAffectedGroups", "mace", "enchanted_book");

        addAbsentToList(conflictConfig, "restriction_breach.enchantments", "minecraft:breach");
        addAbsentToList(conflictConfig, "restriction_breach.notAffectedGroups", "mace", "enchanted_book");

        addAbsentToList(conflictConfig, "restriction_wind_burst.enchantments", "minecraft:wind_burst");
        addAbsentToList(conflictConfig, "restriction_wind_burst.notAffectedGroups", "mace", "enchanted_book");

        // Add mace to conflicts
        addAbsentToList(conflictConfig, "restriction_fire_aspect.notAffectedGroups", "mace");
        addAbsentToList(conflictConfig, "restriction_smite.notAffectedGroups", "mace");
        addAbsentToList(conflictConfig, "restriction_bane_of_arthropods.notAffectedGroups", "mace");

        addAbsentToList(conflictConfig, "sword_enchant_conflict.enchantments",
                "minecraft:density", "minecraft:breach");

        // Add level limit
        baseConfig.set("enchant_limits.minecraft:density", 5);
        baseConfig.set("enchant_limits.minecraft:breach", 4);
        baseConfig.set("enchant_limits.minecraft:wind_burst", 3);

        // Add enchant values
        baseConfig.set("enchant_values.minecraft:density.item", 2);
        baseConfig.set("enchant_values.minecraft:density.book", 1);

        baseConfig.set("enchant_values.minecraft:breach.item", 4);
        baseConfig.set("enchant_values.minecraft:breach.book", 2);

        baseConfig.set("enchant_values.minecraft:wind_burst.item", 4);
        baseConfig.set("enchant_values.minecraft:wind_burst.book", 2);

        // Add unit repair for mace
        unitConfig.set("breeze_rod.mace", 0.25);

        // Set version string as current
        baseConfig.set(UpdateUtils.MINECRAFT_VERSION_PATH, version.toString());

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
        ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);
        ConfigHolder.CONFLICT_HOLDER.saveToDisk(true);
        ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(true);

        // imply reload of CONFLICT_HOLDER
        // We also do not need to reload base config as there is no object related to it.
        ConfigHolder.ITEM_GROUP_HOLDER.reload();
    }

}
