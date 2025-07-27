package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.group.AbstractItemTypeGroup;
import xyz.alexcrea.cuanvil.group.ExcludeItemTypeGroup;
import xyz.alexcrea.cuanvil.group.IncludeItemTypeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;

import java.util.*;

/**
 * Custom Anvil api for material group registry.
 */
@SuppressWarnings({"unused", "UnstableApiUsage"})
public class MaterialGroupApi {

    private MaterialGroupApi() {
    }

    private static Object saveChangeTask = null;
    private static Object reloadChangeTask = null;

    /**
     * Write and add a group.
     * Will not write the group if it already exists.
     * Will not be successful if the group is empty.
     *
     * @param group The group to add
     * @return true if successful.
     */
    public static boolean addMaterialGroup(@NotNull AbstractItemTypeGroup group) {
        return addMaterialGroup(group, false);
    }

    /**
     * Write and add a group.
     * Will not write the group if it already exists.
     * Will not be successful if the group is empty.
     *
     * @param group           The group to add
     * @param overrideDeleted If we should write even if the group was previously deleted.
     * @return true if successful.
     */
    public static boolean addMaterialGroup(@NotNull AbstractItemTypeGroup group, boolean overrideDeleted) {
        ItemGroupManager itemGroupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();

        // Test if it exists/existed
        if (!overrideDeleted && ConfigHolder.ITEM_GROUP_HOLDER.isDeleted(group.getName())) return false;
        if (itemGroupManager.get(group.getName()) != null) return false;

        // Add group
        itemGroupManager.getGroupMap().put(group.getName(), group);

        if (!writeMaterialGroup(group, false)) return false;

        if (group instanceof IncludeItemTypeGroup includeGroup) {
            GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
            if (configGui != null) configGui.updateValueForGeneric(includeGroup, true);
        }

        if (ConfigOptions.INSTANCE.getVerboseDebugLog()) {
            CustomAnvil.instance.getLogger().info("Registered group " + group.getName());
        }

        return true;
    }

    /**
     * Write a material group to the config file and plan an update of groups.
     * <p>
     * You may want to use {@link #addMaterialGroup(AbstractItemTypeGroup)} instead as it is more performance in most case as this function will reload every conflict.
     *
     * @param group the group to write
     * @return true if was written successfully.
     */
    public static boolean writeMaterialGroup(@NotNull AbstractItemTypeGroup group) {
        return writeMaterialGroup(group, true);
    }

    /**
     * Write a material group to the config file.
     * <p>
     * You should use {@link #addMaterialGroup(AbstractItemTypeGroup)} or {@link #writeMaterialGroup(AbstractItemTypeGroup)} instead
     *
     * @param group         the group to write
     * @param updatePlanned if we should plan a global update for material groups
     * @return true if was written successfully.
     */
    public static boolean writeMaterialGroup(@NotNull AbstractItemTypeGroup group, boolean updatePlanned) {
        String name = group.getName();
        if (name.contains(".")) {
            CustomAnvil.instance.getLogger().warning("Group " + name + " contain . in its name but should not. this material group is ignored.");
            return false;
        }

        boolean changed;
        if (group instanceof IncludeItemTypeGroup includeGroup) {
            changed = writeKnownGroup("include", includeGroup);
        } else if (group instanceof ExcludeItemTypeGroup excludeGroup) {
            //TODO work on it when exclude group is reworked
            throw new UnsupportedOperationException("exclude group is temporarily disable for the time being. sorry");
            // This code do not do what is intended ? idk why do it exist
            //changed = writeKnownGroup("exclude", excludeGroup);
        } else {
            changed = writeUnknownGroup(group);
        }
        if (!changed) return false;

        prepareSaveTask();
        if (updatePlanned) prepareUpdateTask();

        return true;
    }

    private static boolean writeKnownGroup(@NotNull String groupType, @NotNull AbstractItemTypeGroup group) {
        FileConfiguration config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();

        String basePath = group.getName() + ".";
        Set<ItemType> itemSets = group.getNonGroupInheritedMaterials();
        Set<AbstractItemTypeGroup> groupSet = group.getGroups();

        boolean empty = true;
        if (!itemSets.isEmpty()) {
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, itemTypesSetToStringList(itemSets));
            empty = false;
        } else {
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, null);
        }
        if (!groupSet.isEmpty()) {
            config.set(basePath + ItemGroupManager.GROUP_LIST_PATH, materialGroupSetToStringList(groupSet));
            empty = false;
        } else {
            config.set(basePath + ItemGroupManager.GROUP_LIST_PATH, null);
        }

        if (empty) {
            config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, null);
            return false;
        }

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, groupType);
        return true;
    }

    private static boolean writeUnknownGroup(@NotNull AbstractItemTypeGroup group) {
        FileConfiguration config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();

        String basePath = group.getName() + ".";
        Set<ItemType> itemTypes = group.getItemTypes();

        if (itemTypes.isEmpty()) return false;

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, "include");
        config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, itemTypesSetToStringList(itemTypes));

        return true;
    }

    public static List<String> itemTypesSetToStringList(@NotNull Set<ItemType> types) {
        return types.stream().map(item -> item.getKey().getKey().toLowerCase()).toList();
    }

    public static List<String> materialGroupSetToStringList(@NotNull Set<AbstractItemTypeGroup> groups) {
        return groups.stream().map(AbstractItemTypeGroup::getName).toList();
    }

    /**
     * Remove a material group.
     * Caution ! It will not be removed from depending conflict or other material group at runtime.
     * For that reason, it is not recommended to use this function.
     *
     * @param group The recipe to remove
     * @return True if the group was present.
     */
    public static boolean removeGroup(@NotNull AbstractItemTypeGroup group) {
        // Remove from registry
        AbstractItemTypeGroup removed = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().groupMap.remove(group.getName());
        if (removed == null) return false;

        // Delete and save to file
        ConfigHolder.ITEM_GROUP_HOLDER.delete(group.getName());
        prepareSaveTask();

        // Remove from gui
        if (group instanceof IncludeItemTypeGroup includeGroup) {
            GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
            if (configGui != null) configGui.removeGeneric(includeGroup);
        }

        return true;
    }

    /**
     * Prepare a task to reload every conflict.
     */
    private static void prepareSaveTask() {
        if (saveChangeTask != null) return;

        saveChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);
            saveChangeTask = null;
        });
    }

    /**
     * Prepare a task to save configuration.
     */
    private static void prepareUpdateTask() {
        if (reloadChangeTask != null) return;

        reloadChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            ConfigHolder.ITEM_GROUP_HOLDER.reload();

            GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
            if (configGui != null) configGui.reloadValues();

            reloadChangeTask = null;
        });

    }

    /**
     * Get by name a group.
     *
     * @param groupName the group name used to fetch
     * @return the abstract group of this name. null if not found.
     */
    @Nullable
    public static AbstractItemTypeGroup getGroup(@NotNull String groupName) {
        return ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().get(groupName);
    }

    /**
     * Get every registered material groups.
     *
     * @return An immutable map of group name as its key and group as mapped value.
     */
    @NotNull
    public static Map<String, AbstractItemTypeGroup> getRegisteredGroups() {
        return Collections.unmodifiableMap(ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap());
    }

}
