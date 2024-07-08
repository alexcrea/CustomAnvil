package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.ExcludeGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom Anvil api for material group registry.
 */
@SuppressWarnings("unused")
public class MaterialGroupApi {

    private static int saveChangeTask = -1;
    private static int reloadChangeTask = -1;

    /**
     * Write and add a group.
     * Will not write the group if it already exists.
     *
     * @param group the group to add
     * @return true if successful.
     */
    public boolean addMaterialGroup(@NotNull AbstractMaterialGroup group){
        ItemGroupManager itemGroupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        if(itemGroupManager.get(group.getName()) != null) return false;

        if(!writeMaterialGroup(group, false)) return false;

        if(group instanceof IncludeGroup includeGroup){
            GroupConfigGui.INSTANCE.updateValueForGeneric(includeGroup, true);
        }

        return true;
    }

    /**
     * Write a material group to the config file and plan an update of groups.
     * <p>
     * You may want to use {@link #addMaterialGroup(AbstractMaterialGroup)} instead as it is more performance in most case as this function will reload every conflict.
     *
     * @param group the group to write
     * @return true if successful.
     */
    public boolean writeMaterialGroup(@NotNull AbstractMaterialGroup group){
        return writeMaterialGroup(group, true);
    }

    /**
     * Write a material group to the config file.
     * <p>
     * You should use {@link #addMaterialGroup(AbstractMaterialGroup)} or {@link #writeMaterialGroup(AbstractMaterialGroup)} instead
     *
     * @param group         the group to write
     * @param updatePlanned if we should plan a global update for material groups
     * @return true if successful.
     */
    public boolean writeMaterialGroup(@NotNull AbstractMaterialGroup group, boolean updatePlanned){
        String name = group.getName();
        if(name.contains(".")) {
            CustomAnvil.instance.getLogger().warning("Group " + name +" contain . in its name but should not. this material group is ignored.");
            return false;
        }

        if(group instanceof IncludeGroup includeGroup){
            writeKnownGroup("include", includeGroup);
        }else if(group instanceof ExcludeGroup excludeGroup){
            writeKnownGroup("exclude", excludeGroup);
        }else{
            writeUnknownGroup(group);
        }

        prepareSaveTask();
        if(updatePlanned) prepareUpdateTask();

        return true;
    }

    private void writeKnownGroup(@NotNull String groupType, @NotNull AbstractMaterialGroup group){
        FileConfiguration config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();

        String basePath = group.getName() + ".";
        Set<Material> materialSet = group.getNonGroupInheritedMaterials();
        Set<AbstractMaterialGroup> groupSet = group.getGroups();

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, groupType);
        if(!materialSet.isEmpty()){
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, materialSet);
        }
        if(!groupSet.isEmpty()){
            config.set(basePath + ItemGroupManager.GROUP_LIST_PATH, groupSet);
        }

    }

    private void writeUnknownGroup(@NotNull AbstractMaterialGroup group) {
        FileConfiguration config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();

        String basePath = group.getName() + ".";
        EnumSet<Material> materials = group.getMaterials();

        config.set(basePath + ItemGroupManager.GROUP_TYPE_PATH, "include");
        if(!materials.isEmpty()){
            config.set(basePath + ItemGroupManager.MATERIAL_LIST_PATH, materials);
        }

    }

    /**
     * Prepare a task to reload every conflict.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != -1) return;

        saveChangeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(CustomAnvil.instance, ()->{
            ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true);
            saveChangeTask = -1;
        }, 0L);
    }

    /**
     * Prepare a task to save configuration.
     */
    private static void prepareUpdateTask() {
        if(reloadChangeTask != -1) return;

        reloadChangeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(CustomAnvil.instance, ()->{
            ConfigHolder.ITEM_GROUP_HOLDER.reload();
            GroupConfigGui.INSTANCE.reloadValues();
            reloadChangeTask = -1;
        }, 0L);

    }

    /**
     * Get by name a group.
     *
     * @param groupName the group name used to fetch
     * @return the abstract group of this name. null if not found.
     */
    @Nullable
    public static AbstractMaterialGroup getGroup(@NotNull String groupName){
        return ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().get(groupName);
    }

    /**
     * Get every registered material groups.
     * @return An immutable map of group name as its key and group as mapped value.
     */
    @NotNull
    public Map<String, AbstractMaterialGroup> getRegisteredGroups(){
        return Collections.unmodifiableMap(ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap());
    }

}
