package xyz.alexcrea.cuanvil.update.minecraft;

import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class Update_1_21_11 extends MCUpdate{

    public Update_1_21_11() {
        super(new Version(1, 21, 11));
    }

    @Override
    protected void doUpdate() {
        var baseConfig = ConfigHolder.DEFAULT_CONFIG.getConfig();
        var groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        var conflictConfig = ConfigHolder.CONFLICT_HOLDER.getConfig();
        var unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        // Create spear group
        groupConfig.set("spears.type", "include");
        addAbsentToList(groupConfig, "spears.items",
                "wooden_spear",
                "golden_spear",
                "stone_spear",
                "copper_spear",
                "iron_spear",
                "diamond_spear",
                "netherite_spear");

        // Add spear group to super group and enchantments
        addAbsentToList(groupConfig, "melee_weapons.groups", "spears");

        addAbsentToList(conflictConfig, "restriction_looting.notAffectedGroups", "spears");
        addAbsentToList(conflictConfig, "restriction_knockback.notAffectedGroups", "spears");
        addAbsentToList(conflictConfig, "restriction_fire_aspect.notAffectedGroups", "spears");

        // Unit repair for spears
        unitConfig.set("gold_ingot.golden_spear", 0.25);
        unitConfig.set("copper_ingot.copper_spear", 0.25);
        unitConfig.set("iron_ingot.iron_spear", 0.25);
        unitConfig.set("diamond.diamond_spear", 0.25);
        unitConfig.set("netherite_ingot.netherite_spear", 0.25);

        unitConfig.set("cobblestone.stone_spear", 0.25);
        unitConfig.set("cobbled_deepslate.stone_spear", 0.25);

        unitConfig.set("oak_planks.wooden_spear", 0.25);
        unitConfig.set("spruce_planks.wooden_spear", 0.25);
        unitConfig.set("birch_planks.wooden_spear", 0.25);
        unitConfig.set("jungle_planks.wooden_spear", 0.25);
        unitConfig.set("acacia_planks.wooden_spear", 0.25);
        unitConfig.set("dark_oak_planks.wooden_spear", 0.25);
        unitConfig.set("mangrove_planks.wooden_spear", 0.25);
        unitConfig.set("cherry_planks.wooden_spear", 0.25);
        unitConfig.set("bamboo_planks.wooden_spear", 0.25);
        unitConfig.set("crimson_planks.wooden_spear", 0.25);
        unitConfig.set("warped_planks.wooden_spear", 0.25);

        // Create lunge enchant value and group
        baseConfig.set("enchant_limits.minecraft:lunge", 3);
        baseConfig.set("enchant_values.minecraft:lunge.item", 2);
        baseConfig.set("enchant_values.minecraft:lunge.book", 1);

        addAbsentToList(conflictConfig, "restriction_lunge.enchantments", "minecraft:lunge");
        addAbsentToList(conflictConfig, "restriction_lunge.notAffectedGroups", "spears", "enchanted_book");

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
