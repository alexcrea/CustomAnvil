package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class PUpdate_1_6_2 {

    private static final String[] toUpdate = new String[] {"restriction_density", "restriction_breach", "restriction_wind_burst"};

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();

        boolean conflictUpdated = false;
        for (String restriction : toUpdate) {
            if(!config.isConfigurationSection(restriction)) continue;
            String path = restriction + ".notAffectedGroups";

            boolean contained = false;
            for (String value : config.getStringList(path)) {
                if(value.equalsIgnoreCase("enchanted_book")) {
                    contained = true;
                    break;
                }
            }

            if(!contained){
                addAbsentToList(config, path, "enchanted_book");
                conflictUpdated = true;
            }
        }

        if(conflictUpdated){
            toSave.add(ConfigHolder.CONFLICT_HOLDER);

            // May not be the most efficient for later revision, maybe move to PluginUpdates
            ConfigHolder.CONFLICT_HOLDER.reload();
        }

        // Then we add the unit repair
        config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        String unitRepairPath = "breeze_rod.mace";
        if(!config.isConfigurationSection(unitRepairPath)){
            config.set(unitRepairPath, 0.25);

            toSave.add(ConfigHolder.UNIT_REPAIR_HOLDER);
        }

    }

}
