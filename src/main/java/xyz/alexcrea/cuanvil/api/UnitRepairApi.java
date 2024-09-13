package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import kotlin.Triple;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.gui.config.global.UnitRepairConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom Anvil api for unit repair.
 */
@SuppressWarnings("unused")
public class UnitRepairApi {

    private UnitRepairApi(){}

    private static Object saveChangeTask = null;

    /**
     * Write and add a custom anvil unit repair recipe.
     * Will not write the recipe if it already exists or was deleted.
     * Set the value to minecraft default value (0.25 = 25%)
     *
     * @param unit       The unit material used to repair the bellow item.
     * @param repairable The item to be repaired.
     * @return true if successful.
     */
    public static boolean addUnitRepair(@NotNull Material unit, @NotNull Material repairable){
        return addUnitRepair(unit, repairable, 0.25, false);
    }

    /**
     * Write and add a custom anvil unit repair recipe.
     * Will not write the recipe if it already exists or was deleted.
     *
     * @param unit       The unit material used to repair the bellow item.
     * @param repairable The item to be repaired.
     * @param value      The amount to be repaired by every unit. (1% = 0.01)
     * @return true if successful.
     */
    public static boolean addUnitRepair(@NotNull Material unit, @NotNull Material repairable, double value){
        return addUnitRepair(unit, repairable, value, false);
    }

    /**
     * Write and add a custom anvil unit repair recipe.
     * Will not write the recipe if it already exists.
     *
     * @param unit            The unit material used to repair the bellow item.
     * @param repairable      The item to be repaired.
     * @param value           The amount to be repaired by every unit. (1% = 0.01)
     * @param overrideDeleted If we should write even if the recipe was previously deleted.
     * @return true if successful.
     */
    public static boolean addUnitRepair(@NotNull Material unit, @NotNull Material repairable, double value, boolean overrideDeleted){
        FileConfiguration config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        String path = unit.name().toLowerCase() + "." + repairable.name().toLowerCase();

        if(!overrideDeleted && ConfigHolder.UNIT_REPAIR_HOLDER.isDeleted(path)) return false;
        if(config.contains(path)) return false;

        // Set unit repair
        return setUnitRepair(unit, repairable, value);
    }

    /**
     * Write and add a custom anvil unit repair recipe.
     * Do not check if it previously existed or exist.
     *
     * @param unit       The unit material used to repair the bellow item.
     * @param repairable The item to be repaired.
     * @param value      The amount to be repaired by every unit. (1% = 0.01)
     * @return true if successful.
     */
    public static boolean setUnitRepair(@NotNull Material unit, @NotNull Material repairable, double value){
        FileConfiguration config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        String repairableName = repairable.name().toLowerCase();
        String path = unit.name().toLowerCase() + "." + repairableName;

        // Add to config then prepare save
        config.set(path, value);
        prepareSaveTask();

        // Add to gui
        UnitRepairConfigGui repairConfigGui = UnitRepairConfigGui.getCurrentInstance();
        if(repairConfigGui != null) {
            UnitRepairElementListGui elementGui = repairConfigGui.getInstanceOrCreate(unit);

            elementGui.updateValueForGeneric(repairableName, true);
            repairConfigGui.updateValueForGeneric(unit, true);
        }

        return true;
    }

    /**
     * Remove a custom anvil unit repair recipe.
     *
     * @param unit       The unit material used to repair the bellow item.
     * @param repairable The item used to be repaired.
     * @return true if successful.
     */
    public static boolean removeUnitRepair(@NotNull Material unit, @NotNull Material repairable){
        // Delete every possible variation and save to file
        String unitName = unit.name();
        String repairableName = repairable.name();

        FileConfiguration config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        config.set(unitName.toLowerCase() + repairableName.toUpperCase(), null);
        config.set(unitName.toUpperCase() + repairableName.toLowerCase(), null);
        config.set(unitName.toUpperCase() + repairableName.toUpperCase(), null);

        // Test if it was the last value of this section
        boolean lastValue = false;
        if(config.isConfigurationSection(unitName.toLowerCase())) {
            ConfigurationSection section = config.getConfigurationSection(unitName.toLowerCase());
            if(section.getKeys(false).isEmpty()) {
                lastValue = true;
                config.set(unitName.toLowerCase(), null);
            }

        } else if (config.isConfigurationSection(unitName.toUpperCase())) {
            ConfigurationSection section = config.getConfigurationSection(unitName.toUpperCase());
            if(section.getKeys(false).isEmpty()) {
                lastValue = true;
                config.set(unitName.toUpperCase(), null);
            }

        } else lastValue = true;


        // We only need to "delete" as the lower case to be counted as deleted
        ConfigHolder.UNIT_REPAIR_HOLDER.delete(unitName.toLowerCase() + repairableName.toLowerCase());
        prepareSaveTask();

        // Remove from gui
        UnitRepairConfigGui repairConfigGui = UnitRepairConfigGui.getCurrentInstance();
        if(repairConfigGui != null) {
            UnitRepairElementListGui elementGui = repairConfigGui.getInstanceOrCreate(unit);

            elementGui.removeGeneric(repairableName);
            if(lastValue){
                repairConfigGui.removeGeneric(unit);
            }
        }

        return true;
    }

    /**
     * Prepare a task to save custom unit repair recipe configuration.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != null) return;

        saveChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, ()->{
            ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(true);
            saveChangeTask = null;
        });
    }

    /**
     * Get every unit repair recipes.
     * @return An immutable collection of unit repair recipes.
     * <p>
     * Each element of the provided triple represent a part of the recipe
     * <ul>
     *    <li>First object is the unit material used to repair the bellow item.
     *    <li>Second object is the item to be repaired.
     *    <li>Last object is the amount to be repaired by every unit. (1% = 0.01)
     * </ul>
     */
    @NotNull
    public static List<Triple<Material, Material, Double>> getUnitRepairs(){
        List<Triple<Material, Material, Double>> mutableList = new ArrayList<>();

        FileConfiguration config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        for (String unitKey : config.getKeys(false)) {
            // Test if config section exist
            if(!config.isConfigurationSection(unitKey)) continue;

            // Test if unit is a material
            Material unit = Material.getMaterial(unitKey.toUpperCase());
            if(unit == null) continue;

            // Iterate over reparable items
            ConfigurationSection section = config.getConfigurationSection(unitKey);
            for (String repairableKey : section.getKeys(false)) {
                // Test if value section exist
                if(!section.isDouble(repairableKey)) continue;

                // Test if repairable is valid a material
                Material repairable = Material.getMaterial(repairableKey.toUpperCase());
                if(repairable == null) continue;

                // Add the values
                mutableList.add(new Triple<>(unit, repairable, section.getDouble(repairableKey)));

            }
        }

        return Collections.unmodifiableList(mutableList);
    }

}
