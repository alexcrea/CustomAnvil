package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.api.MaterialGroupApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

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

    private static final Material[] PICKAXES = new Material[]{
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE,
            Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.NETHERITE_PICKAXE
    };

    private static final Material[] SHOVELS = new Material[]{
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL,
            Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.NETHERITE_SHOVEL
    };

    private static final Material[] HOES = new Material[]{
            Material.WOODEN_HOE, Material.STONE_HOE,
            Material.IRON_HOE, Material.DIAMOND_HOE,
            Material.GOLDEN_HOE, Material.NETHERITE_HOE
    };

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        handleToolsMigration();
        handleMaceMigration(toSave);
    }

    private static void handleToolsMigration() {
        // We migrate the mace conflict if exist and unmodified
        AbstractMaterialGroup tools = MaterialGroupApi.getGroup("tools");

        migrateTools(tools, "pickaxes", PICKAXES);
        migrateTools(tools, "shovels", SHOVELS);
        migrateTools(tools, "hoes", HOES);
    }

    private static void migrateTools(
            @Nullable AbstractMaterialGroup tools,
            @NotNull String toolset,
            @NotNull Material[] toolMats) {

        // Create new group
        IncludeGroup group = new IncludeGroup(toolset);
        group.addAll(toolMats);

        MaterialGroupApi.addMaterialGroup(group, true);

        // Try to see if all the materials was in the tools group. and if so, replace it with the new group
        if (tools == null) return;
        if (!(tools instanceof IncludeGroup include)) return;

        List<Material> mats = List.of(toolMats);
        Set<Material> matSet = include.getNonGroupInheritedMaterials();
        if (!matSet.containsAll(mats)) return;

        mats.forEach(matSet::remove);
        tools.addToPolicy(group);
        MaterialGroupApi.writeMaterialGroup(tools);
    }

    private static void handleMaceMigration(@Nonnull Set<ConfigHolder> toSave) {
        // We migrate the mace conflict if exist and unmodified
        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();

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
            if (!enchantments.contains(ench) && !enchantments.contains("minecraft:" + ench)) return;
        }

        // Test sword_enchant_conflict is default
        ConfigurationSection sword_conflict = config.getConfigurationSection("sword_enchant_conflict");
        if (sword_conflict.getInt("maxEnchantmentBeforeConflict", 0) != 1) return;

        if (sword_conflict.isList("notAffectedGroups") && !sword_conflict.getList("notAffectedGroups").isEmpty())
            return;

        enchantments = sword_conflict.getStringList("enchantments");
        if (enchantments.size() != 3) return;
        for (String ench : sword_expected) {
            if (!enchantments.contains(ench) && !enchantments.contains("minecraft:" + ench)) return;
        }

        // Finally we know both conflict are default. so we fix
        addAbsentToList(config, "sword_enchant_conflict.enchantments",
                "minecraft:density", "minecraft:breach");

        config.set("mace_enchant_conflict", null);
        toSave.add(ConfigHolder.CONFLICT_HOLDER);
    }

}
