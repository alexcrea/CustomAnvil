package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@NotNullByDefault
public class PUpdate_1_6_2 extends PluginUpdate {

    private static final String[] toUpdate = new String[] {"restriction_density", "restriction_breach", "restriction_wind_burst"};

    public PUpdate_1_6_2() {
        super(new Version(1, 6, 2));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        var conflict = toSave.use(ConfigHolder.CONFLICT);
        FileConfiguration config = conflict.getConfig();

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
            // May not be the most efficient for later revision, maybe move to PluginUpdates
            conflict.reload();
        }

        // Then we add the unit repair
        config = toSave.use(ConfigHolder.UNIT_REPAIR).getConfig();
        String unitRepairPath = "breeze_rod.mace";
        if(!config.isConfigurationSection(unitRepairPath)){
            config.set(unitRepairPath, 0.25);
        }
    }

}
