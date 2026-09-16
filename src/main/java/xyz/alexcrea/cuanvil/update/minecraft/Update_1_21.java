package xyz.alexcrea.cuanvil.update.minecraft;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@NotNullByDefault
public class Update_1_21 extends MCUpdate {

    public Update_1_21() {
        super(new Version(1, 21));
    }

    @Override
    protected void doUpdate(UpdateHandler.UpdatedConfigList toSave) {
        var baseConfig = toSave.use(ConfigHolder.DEFAULT).getConfig();
        var groupConfig = toSave.use(ConfigHolder.ITEM_GROUP).getConfig();
        var conflictConfig = toSave.use(ConfigHolder.CONFLICT).getConfig();
        var unitConfig = toSave.use(ConfigHolder.UNIT_REPAIR).getConfig();

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
    }

}
