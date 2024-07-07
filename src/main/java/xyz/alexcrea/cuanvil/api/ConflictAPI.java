package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.*;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConflictGui;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom Anvil api for conflict registry.
 */
@SuppressWarnings("unused")
public class ConflictAPI {

    private static int saveChangeTask = -1;
    private static int reloadChangeTask = -1;

    /**
     * Write and add a conflict.
     *
     * @param builder the conflict builder to base on
     * @return true if successful
     */
    public boolean addConflict(@NotNull ConflictBuilder builder){
        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();
        if(config.contains(builder.getName())) return false;

        if(!writeConflict(builder, false)) return false;

        AbstractMaterialGroup materials = extractGroup(builder);
        EnchantConflictGroup conflict = new EnchantConflictGroup(builder.getName(), materials, builder.getMaxBeforeConflict());
        appendEnchantments(builder, conflict);

        EnchantConflictGui.INSTANCE.updateValueForGeneric(conflict, true);

        return true;
    }

    /**
     * Append builders stored enchantments into conflict.
     *
     * @param builder  the builder source
     * @param conflict the conflict target
     */
    protected void appendEnchantments(@NotNull ConflictBuilder builder, @NotNull EnchantConflictGroup conflict){
        for (String enchantmentName : builder.getEnchantmentNames()){
            if(appendEnchantment(conflict, EnchantmentApi.getByName(enchantmentName))){
                CustomAnvil.instance.getLogger().warning("Could not find enchantment " + enchantmentName + " for conflict " + builder.getName());
                logConflictOrigin(builder);
            }
        }
        for (NamespacedKey enchantmentKey : builder.getEnchantmentKeys()){
            if(!appendEnchantment(conflict, EnchantmentApi.getByKey(enchantmentKey))){
                CustomAnvil.instance.getLogger().warning("Could not find enchantment " + enchantmentKey + " for conflict " + builder.getName());
                logConflictOrigin(builder);
            }
        }
    }

    /**
     * Append an enchantment.
     *
     * @param conflict    the conflict target
     * @param enchantment the enchantment
     * @return true if successful
     */
    protected boolean appendEnchantment(@NotNull EnchantConflictGroup conflict, @Nullable CAEnchantment enchantment){
        if(enchantment == null)
            return false;
        conflict.addEnchantment(enchantment);
        return true;
    }

    /**
     * Extract group abstract material group.
     *
     * @param builder the builder source
     * @return the abstract material group from the builder
     */
    protected AbstractMaterialGroup extractGroup(@NotNull ConflictBuilder builder){
        ItemGroupManager itemGroupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        IncludeGroup group = new IncludeGroup(EnchantConflictManager.DEFAULT_GROUP_NAME);

        for (String groupName : builder.getExcludedGroupNames()) {
            AbstractMaterialGroup materialGroup = itemGroupManager.get(groupName);

            if(materialGroup == null){
                CustomAnvil.instance.getLogger().warning("Material group " + groupName + " do not exist but is ask by conflict " + builder.getName());
                logConflictOrigin(builder);
                continue;
            }

            group.addToPolicy(materialGroup);
        }

        return group;
    }

    /**
     * Write a conflict to the config file and plan an update of conflicts.
     * <p>
     * You may want to use {@link #addConflict(ConflictBuilder)} instead as it is more performance in most case as this function will reload every conflict.
     *
     * @param builder the builder
     * @return true if successful
     */
    public boolean writeConflict(@NotNull ConflictBuilder builder){
        return writeConflict(builder, true);
    }

    /**
     * Write a conflict to the config file.
     * <p>
     * You should use {@link #addConflict(ConflictBuilder)} or {@link #writeConflict(ConflictBuilder)} instead
     *
     * @param builder       the builder
     * @param updatePlanned if we should plan an update
     * @return true if successful
     */
    public boolean writeConflict(@NotNull ConflictBuilder builder, boolean updatePlanned){
        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();

        String name = builder.getName();
        if(name.contains(".")) {
            CustomAnvil.instance.getLogger().warning("Conflict " + name +" contain . in its name but should not. this conflict is ignored.");
            logConflictOrigin(builder);
            return false;
        }

        String basePath = name+".";

        Set<String> enchantments = extractEnchantments(builder);
        Set<String> excludedGroups = builder.getExcludedGroupNames();
        if(!enchantments.isEmpty()) config.set(basePath + "enchantments", enchantments);
        if(!excludedGroups.isEmpty()) config.set(basePath + "notAffectedGroups", excludedGroups);
        if(builder.getMaxBeforeConflict() > 0) config.set(basePath + "maxEnchantmentBeforeConflict", builder.getMaxBeforeConflict());


        prepareSaveTask();
        if(updatePlanned) prepareUpdateTask();

        return true;
    }

    /**
     * Extract every enchantment names from a builder.
     * @param builder the builder storing the enchantments
     * @return builder's stored enchantment
     */
    @NotNull
    private Set<String> extractEnchantments(@NotNull ConflictBuilder builder){
        Set<String> result = new HashSet<>(builder.getEnchantmentNames());
        for (NamespacedKey enchantmentKey : builder.getEnchantmentKeys()) {
            result.add(enchantmentKey.getKey());
        }

        return result;
    }

    /**
     * Prepare a task to reload every conflict.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != -1) return;

        saveChangeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(CustomAnvil.instance, ()->{
            ConfigHolder.CONFLICT_HOLDER.saveToDisk(true);
            saveChangeTask = -1;
        }, 0L);
    }

    /**
     * Prepare a task to save configuration.
     */
    private static void prepareUpdateTask() {
        if(reloadChangeTask != -1) return;

        reloadChangeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(CustomAnvil.instance, ()->{
            ConfigHolder.CONFLICT_HOLDER.reload();
            EnchantConflictGui.INSTANCE.reloadValues();
            reloadChangeTask = -1;
        }, 0L);

    }

    private void logConflictOrigin(@NotNull ConflictBuilder builder){
        CustomAnvil.instance.getLogger().warning("Conflict " + builder.getName() +" came from " + builder.getSourceName() + ".");
    }

}
