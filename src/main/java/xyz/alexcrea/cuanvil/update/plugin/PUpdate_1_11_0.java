package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.api.MaterialGroupApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractItemTypeGroup;
import xyz.alexcrea.cuanvil.group.IncludeItemTypeGroup;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@SuppressWarnings("UnstableApiUsage")
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

    private static final ItemType[] PICKAXES = new ItemType[]{
            ItemType.WOODEN_PICKAXE, ItemType.STONE_PICKAXE,
            ItemType.IRON_PICKAXE, ItemType.DIAMOND_PICKAXE,
            ItemType.GOLDEN_PICKAXE, ItemType.NETHERITE_PICKAXE
    };

    private static final ItemType[] SHOVELS = new ItemType[]{
            ItemType.WOODEN_SHOVEL, ItemType.STONE_SHOVEL,
            ItemType.IRON_SHOVEL, ItemType.DIAMOND_SHOVEL,
            ItemType.GOLDEN_SHOVEL, ItemType.NETHERITE_SHOVEL
    };

    private static final ItemType[] HOES = new ItemType[]{
            ItemType.WOODEN_HOE, ItemType.STONE_HOE,
            ItemType.IRON_HOE, ItemType.DIAMOND_HOE,
            ItemType.GOLDEN_HOE, ItemType.NETHERITE_HOE
    };

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        handleToolsMigration();
        handleMaceMigration(toSave);
    }

    private static void handleToolsMigration() {
        // We migrate the mace conflict if exist and unmodified
        AbstractItemTypeGroup tools = MaterialGroupApi.getGroup("tools");

        migrateTools(tools, "pickaxes", PICKAXES);
        migrateTools(tools, "shovels", SHOVELS);
        migrateTools(tools, "hoes", HOES);
    }

    private static void migrateTools(
            @Nullable AbstractItemTypeGroup tools,
            @NotNull String toolset,
            @NotNull ItemType[] toolMats) {

        // Create new group
        IncludeItemTypeGroup group = new IncludeItemTypeGroup(toolset);
        group.addAll(toolMats);

        MaterialGroupApi.addMaterialGroup(group, true);

        // Try to see if all the materials was in the tools group. and if so, replace it with the new group
        if (tools == null) return;
        if (!(tools instanceof IncludeItemTypeGroup include)) return;

        List<ItemType> types = List.of(toolMats);
        Set<ItemType> typeSet = include.getNonGroupInheritedMaterials();
        if (!typeSet.containsAll(types)) return;

        types.forEach(typeSet::remove);
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
        if (sword_conflict == null) return;

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
