package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.ExcludeGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom Anvil api for material group registry.
 */
@SuppressWarnings({"unused", "SameParameterValue"})
@NotNullByDefault
public class MaterialGroupApi {

    private MaterialGroupApi() {
    }

    private static volatile @Nullable Object saveChangeTask = null;
    private static volatile @Nullable Object reloadChangeTask = null;

    /**
     * Write and add a group.
     * Will not write the group if it already exists.
     * Will not be successful if the group is empty.
     *
     * @param group The group to add
     * @return true if successful.
     */
    public static boolean addMaterialGroup(AbstractMaterialGroup group) {
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
    public static boolean addMaterialGroup(AbstractMaterialGroup group, boolean overrideDeleted) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return addMaterialGroup(lock.get(), group, overrideDeleted);
        }
    }

    private static boolean addMaterialGroup(ConfigHolder.ItemGroupConfigHolder holder, AbstractMaterialGroup group, boolean overrideDeleted) {
        ItemGroupManager itemGroupManager = holder.getItemGroupsManager();

        // Test if it exists/existed
        if(!overrideDeleted && holder.isDeleted(group.getName())) return false;
        if(itemGroupManager.get(group.getName()) != null) return false;

        // Add group
        itemGroupManager.getGroupMap().put(group.getName(), group);

        if(!writeMaterialGroup(group, false)) return false;

        if(group instanceof IncludeGroup includeGroup) {
            GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
            if(configGui != null) configGui.updateValueForGeneric(includeGroup, true);
        }

        if(ConfigOptions.INSTANCE.getVerboseDebugLog()) {
            CustomAnvil.instance.getLogger().info("Registered group " + group.getName());
        }

        return true;
    }

    /**
     * Write a material group to the config file and plan an update of groups.
     * <p>
     * You may want to use {@link #addMaterialGroup(AbstractMaterialGroup)} instead as it is more performance in most case as this function will reload every conflict.
     *
     * @param group the group to write
     * @return true if was written successfully.
     */
    public static boolean writeMaterialGroup(AbstractMaterialGroup group) {
        return writeMaterialGroup(group, true);
    }

    /**
     * Write a material group to the config file.
     * <p>
     * You should use {@link #addMaterialGroup(AbstractMaterialGroup)} or {@link #writeMaterialGroup(AbstractMaterialGroup)} instead
     *
     * @param group         the group to write
     * @param updatePlanned if we should plan a global update for material groups
     * @return true if was written successfully.
     */
    public static boolean writeMaterialGroup(AbstractMaterialGroup group, boolean updatePlanned) {
        String name = group.getName();
        if(name.contains(".")) {
            CustomAnvil.instance.getLogger().warning("Group " + name + " contain . in its name but should not. this material group is ignored.");
            return false;
        }

        boolean changed;
        if(group instanceof IncludeGroup includeGroup) {
            changed = writeKnownGroup("include", includeGroup);
        } else if(group instanceof ExcludeGroup excludeGroup) {
            throw new UnsupportedOperationException("exclude group is temporarily disable for the time being. sorry");
            // This code do not do what is intended ? idk why do it exist
            //changed = writeKnownGroup("exclude", excludeGroup);
        } else {
            changed = writeUnknownGroup(group);
        }
        if(!changed) return false;

        prepareSaveTask();
        if(updatePlanned) prepareUpdateTask();

        return true;
    }

    private static boolean writeKnownGroup(String groupType, AbstractMaterialGroup group) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return writeKnownGroup(lock.get(), groupType, group);
        }
    }

    private static boolean writeKnownGroup(ConfigHolder.ItemGroupConfigHolder holder, String groupType, AbstractMaterialGroup group) {
        FileConfiguration config = holder.getConfig();

        String basePath = group.getName() + ".";
        Set<NamespacedKey> materialSet = group.getNonGroupInheritedMaterials();
        Set<AbstractMaterialGroup> groupSet = group.getGroups();

        boolean empty = true;
        if(!materialSet.isEmpty()) {
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, materialSetToStringList(materialSet));
            empty = false;
        } else {
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, null);
        }
        if(!groupSet.isEmpty()) {
            config.set(basePath + ItemGroupManager.GROUP_LIST_PATH, materialGroupSetToStringList(groupSet));
            empty = false;
        } else {
            config.set(basePath + ItemGroupManager.GROUP_LIST_PATH, null);
        }

        if(empty) {
            config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, null);
            return false;
        }

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, groupType);
        return true;
    }

    private static boolean writeUnknownGroup(AbstractMaterialGroup group) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return writeUnknownGroup(lock.get(), group);
        }
    }

    private static boolean writeUnknownGroup(ConfigHolder.ItemGroupConfigHolder holder, AbstractMaterialGroup group) {
        FileConfiguration config = holder.getConfig();

        String basePath = group.getName() + ".";
        Set<NamespacedKey> materials = group.getMaterials();

        if(materials.isEmpty()) return false;

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, "include");
        config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, materialSetToStringList(materials));

        return true;
    }

    public static List<String> materialSetToStringList(Set<NamespacedKey> materials) {
        return materials.stream().map(NamespacedKey::toString).toList();
    }

    public static List<String> materialGroupSetToStringList(Set<AbstractMaterialGroup> groups) {
        return groups.stream().map(AbstractMaterialGroup::getName).toList();
    }

    /**
     * Remove a material group.
     * Caution ! It will not be removed from depending on conflict or other material group at runtime.
     * For that reason, it is not recommended to use this function.
     *
     * @param group The recipe to remove
     * @return True if the group was present.
     */
    public static boolean removeGroup(AbstractMaterialGroup group) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return removeGroup(lock.get(), group);
        }
    }

    private static boolean removeGroup(ConfigHolder.ItemGroupConfigHolder holder, AbstractMaterialGroup group) {
        // Remove from registry
        AbstractMaterialGroup removed = holder.getItemGroupsManager().groupMap.remove(group.getName());
        if(removed == null) return false;

        // Delete and save to file
        holder.delete(group.getName());
        prepareSaveTask();

        // Remove from gui
        if(group instanceof IncludeGroup includeGroup) {
            GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
            if(configGui != null) configGui.removeGeneric(includeGroup);
        }

        return true;
    }

    /**
     * Prepare a task to reload every conflict.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != null) return;

        @SuppressWarnings("UnnecessaryLocalVariable")
        var task = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            try(var lock = ConfigHolder.ITEM_GROUP.write) {
                lock.get().saveToDisk(true);
                saveChangeTask = null;
            }
        });

        saveChangeTask = task;
    }

    /**
     * Prepare a task to save configuration.
     */
    private static void prepareUpdateTask() {
        if(reloadChangeTask != null) return;

        @SuppressWarnings("UnnecessaryLocalVariable")
        var task = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            try(var lock = ConfigHolder.ITEM_GROUP.write) {
                lock.get().reload();

                GroupConfigGui configGui = GroupConfigGui.getCurrentInstance();
                if(configGui != null) configGui.reloadValues();

                reloadChangeTask = null;
            }
        });

        reloadChangeTask = task;
    }

    /**
     * Get by name a group.
     *
     * @param groupName the group name used to fetch
     * @return the abstract group of this name. null if not found.
     */
    @Nullable
    public static AbstractMaterialGroup getGroup(String groupName) {
        try(var lock = ConfigHolder.ITEM_GROUP.read) {
            return lock.get().getItemGroupsManager().get(groupName);
        }
    }

    /**
     * Get every registered material groups.
     *
     * @return An immutable map of group name as its key and group as mapped value.
     */
    public static Map<String, AbstractMaterialGroup> getRegisteredGroups() {
        try(var lock = ConfigHolder.ITEM_GROUP.read) {
            return Collections.unmodifiableMap(lock.get().getItemGroupsManager().getGroupMap());
        }
    }

}
