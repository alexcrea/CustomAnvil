package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConflictGui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom Anvil api for conflict registry.
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "SameReturnValue"})
@NotNullByDefault
public class ConflictAPI {

    private ConflictAPI() {
    }

    private static volatile @Nullable Object saveChangeTask = null;
    private static volatile @Nullable Object reloadChangeTask = null;

    /**
     * Write and add a conflict.
     * Will not write the conflict if it already exists.
     * Will not be successful if the conflict is empty.
     *
     * @param builder The conflict builder to be based on
     * @return True if successful.
     */
    public static boolean addConflict(ConflictBuilder builder) {
        return addConflict(builder, false);
    }

    /**
     * Write and add a conflict.
     * Will not write the conflict if it already exists.
     * Will not be successful if the conflict is empty.
     *
     * @param builder         The conflict builder to be based on
     * @param overrideDeleted If we should write even if the conflict was previously deleted.
     * @return True if successful.
     */
    public static boolean addConflict(ConflictBuilder builder, boolean overrideDeleted) {
        try(var lock = ConfigHolder.CONFLICT.write) {
            var holder = lock.get();
            var config = holder.getConfig();

            // Test if conflict can be added
            if(!overrideDeleted && holder.isDeleted(builder.getName())) return false;
            if(config.contains(builder.getName())) return false;


            if(!writeConflict(builder, false)) return false;

            EnchantConflictGroup conflict = builder.build();
            // Register conflict
            holder.getConflictManager().addConflict(conflict);

            // Add conflict to gui
            EnchantConflictGui conflictGui = EnchantConflictGui.getCurrentInstance();
            if(conflictGui != null) conflictGui.updateValueForGeneric(conflict, true);
        }

        return true;
    }

    /**
     * Write a conflict to the config file and plan an update of conflicts.
     * <p>
     * You may want to use {@link #addConflict(ConflictBuilder)} instead as it is more performance in most case as this function will reload every conflict.
     *
     * @param builder the builder
     * @return true if was written successfully.
     */
    public static boolean writeConflict(ConflictBuilder builder) {
        return writeConflict(builder, true);
    }


    /**
     * Write a conflict to the config file.
     * <p>
     * You should use {@link #addConflict(ConflictBuilder)} or {@link #writeConflict(ConflictBuilder)} instead
     *
     * @param builder       The builder
     * @param updatePlanned If we should plan a global update for conflicts
     * @return true if was written successfully.
     */
    public static boolean writeConflict(ConflictBuilder builder, boolean updatePlanned) {
        try(var lock = ConfigHolder.CONFLICT.write) {
            var config = lock.get().getConfig();
            return writeConflict(config, builder, updatePlanned);
        }
    }

    public static boolean writeConflict(FileConfiguration config, ConflictBuilder builder, boolean updatePlanned) {
        String name = builder.getName();
        if(name.contains(".")) {
            CustomAnvil.instance.getLogger().warning("Conflict " + name + " contain \".\" in its name but should not. this conflict is ignored.");
            logConflictOrigin(builder);
            return false;
        }

        String basePath = name + ".";

        List<String> enchantments = extractEnchantments(builder);
        List<String> excludedGroups = new ArrayList<>(builder.getExcludedGroupNames());
        if(!enchantments.isEmpty()) config.set(basePath + "enchantments", enchantments);
        if(!excludedGroups.isEmpty()) config.set(basePath + "notAffectedGroups", excludedGroups);
        if(builder.getMaxBeforeConflict() > 0)
            config.set(basePath + "maxEnchantmentBeforeConflict", builder.getMaxBeforeConflict());

        if(!config.isConfigurationSection(name)) return false;

        prepareSaveTask();
        if(updatePlanned) prepareUpdateTask();

        return true;
    }

    /**
     * Extract every enchantment names from a builder.
     *
     * @param builder The builder storing the enchantments
     * @return Builder's stored enchantment.
     */
    private static List<String> extractEnchantments(ConflictBuilder builder) {
        var result = new ArrayList<>(builder.getEnchantmentNames());
        for(NamespacedKey enchantmentKey : builder.getEnchantmentKeys()) {
            result.add(enchantmentKey.toString());
        }

        return result;
    }

    /**
     * Remove a conflict.
     *
     * @param conflict The conflict to remove
     * @return True if successful.
     */
    public static boolean removeConflict(EnchantConflictGroup conflict) {
        try(var lock = ConfigHolder.CONFLICT.write) {
            var holder = lock.get();

            // Remove from registry
            holder.getConflictManager().removeConflict(conflict);

            // Delete and save to file
            holder.delete(conflict.getName());
        }

        prepareSaveTask();

        // Remove from gui
        EnchantConflictGui conflictGui = EnchantConflictGui.getCurrentInstance();
        if(conflictGui != null) conflictGui.removeGeneric(conflict);

        return true;
    }

    /**
     * Prepare a task to save conflict configuration.
     */
    private static void prepareSaveTask() {
        //noinspection DuplicatedCode
        if(saveChangeTask != null) return;

        @SuppressWarnings("UnnecessaryLocalVariable")
        var task = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            try(var lock = ConfigHolder.CONFLICT.write) {
                lock.get().saveToDisk(true);
                saveChangeTask = null;
            }
        });

        saveChangeTask = task;
    }

    /**
     * Prepare a task to reload every conflict.
     */
    private static void prepareUpdateTask() {
        if(reloadChangeTask != null) return;

        @SuppressWarnings("UnnecessaryLocalVariable")
        var task = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            try(var lock = ConfigHolder.CONFLICT.write) {
                lock.get().reload();
                EnchantConflictGui conflictGui = EnchantConflictGui.getCurrentInstance();
                if(conflictGui != null) conflictGui.reloadValues();

                reloadChangeTask = null;
            }
        });

        reloadChangeTask = task;
    }

    static void logConflictOrigin(ConflictBuilder builder) {
        CustomAnvil.instance.getLogger().warning("Conflict " + builder.getName() + " came from " + builder.getSourceName() + ".");
    }

    /**
     * Get every registered conflict.
     *
     * @return An immutable collection of conflict.
     */
    public static List<EnchantConflictGroup> getRegisteredConflict() {
        try(var lock = ConfigHolder.CONFLICT.read) {
            var mutableList = lock.get().getConflictManager().getConflictList();
            return Collections.unmodifiableList(mutableList);
        }
    }

}
