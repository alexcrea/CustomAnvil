package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.WorkPenaltyType;

public class PluginSetDefault {

    public static void reAddMissingDefault(){
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.acquiredWrite();

        int nbSet = 0;

        nbSet+= trySetDefault(config, ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST);
        nbSet+= trySetDefault(config, ConfigOptions.MAX_ANVIL_COST, ConfigOptions.DEFAULT_MAX_ANVIL_COST);
        nbSet+= trySetDefault(config, ConfigOptions.REMOVE_ANVIL_COST_LIMIT, ConfigOptions.DEFAULT_REMOVE_ANVIL_COST_LIMIT);
        nbSet+= trySetDefault(config, ConfigOptions.REPLACE_TOO_EXPENSIVE, ConfigOptions.DEFAULT_REPLACE_TOO_EXPENSIVE);
        nbSet+= trySetDefault(config, ConfigOptions.ITEM_REPAIR_COST, ConfigOptions.DEFAULT_ITEM_REPAIR_COST);
        nbSet+= trySetDefault(config, ConfigOptions.UNIT_REPAIR_COST, ConfigOptions.DEFAULT_UNIT_REPAIR_COST);
        nbSet+= trySetDefault(config, ConfigOptions.ITEM_RENAME_COST, ConfigOptions.DEFAULT_ITEM_RENAME_COST);
        nbSet+= trySetDefault(config, ConfigOptions.SACRIFICE_ILLEGAL_COST, ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST);
        nbSet+= trySetDefault(config, ConfigOptions.ALLOW_COLOR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOR_CODE);
        nbSet+= trySetDefault(config, ConfigOptions.ALLOW_HEXADECIMAL_COLOR, ConfigOptions.DEFAULT_ALLOW_HEXADECIMAL_COLOR);
        nbSet+= trySetDefault(config, ConfigOptions.PERMISSION_NEEDED_FOR_COLOR, ConfigOptions.DEFAULT_PERMISSION_NEEDED_FOR_COLOR);
        nbSet+= trySetDefault(config, ConfigOptions.USE_OF_COLOR_COST, ConfigOptions.DEFAULT_USE_OF_COLOR_COST);
        nbSet+= trySetDefault(config, ConfigOptions.WORK_PENALTY_TYPE, WorkPenaltyType.DEFAULT.configName());
        nbSet+= trySetDefault(config, ConfigOptions.DEFAULT_LIMIT_PATH, ConfigOptions.DEFAULT_ENCHANT_LIMIT);

        ConfigHolder.DEFAULT_CONFIG.releaseWrite();

        if(nbSet > 0){
            CustomAnvil.instance.getLogger().info("Adding " + nbSet + " absent default config values.");
            ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
        }

    }

    private static int trySetDefault(@NotNull FileConfiguration config, @NotNull String path, @NotNull String value){
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }

    private static int trySetDefault(@NotNull FileConfiguration config, @NotNull String path, int value){
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }

    private static int trySetDefault(@NotNull FileConfiguration config, @NotNull String path, boolean value){
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }


}
