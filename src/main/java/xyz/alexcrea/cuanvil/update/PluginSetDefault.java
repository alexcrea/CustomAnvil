package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.util.config.LoreEditType;

import static io.delilaheve.util.ConfigOptions.*;
import static xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.*;

public class PluginSetDefault {

    public static void reAddMissingDefault(){
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        int nbSet = 0;

        nbSet+= trySetDefault(config, CAP_ANVIL_COST, DEFAULT_CAP_ANVIL_COST);
        nbSet+= trySetDefault(config, MAX_ANVIL_COST, DEFAULT_MAX_ANVIL_COST);
        nbSet+= trySetDefault(config, REMOVE_ANVIL_COST_LIMIT, DEFAULT_REMOVE_ANVIL_COST_LIMIT);
        nbSet+= trySetDefault(config, REPLACE_TOO_EXPENSIVE, DEFAULT_REPLACE_TOO_EXPENSIVE);
        nbSet+= trySetDefault(config, ITEM_REPAIR_COST, DEFAULT_ITEM_REPAIR_COST);
        nbSet+= trySetDefault(config, UNIT_REPAIR_COST, DEFAULT_UNIT_REPAIR_COST);
        nbSet+= trySetDefault(config, ITEM_RENAME_COST, DEFAULT_ITEM_RENAME_COST);
        nbSet+= trySetDefault(config, SACRIFICE_ILLEGAL_COST, DEFAULT_SACRIFICE_ILLEGAL_COST);
        nbSet+= trySetDefault(config, ALLOW_COLOR_CODE, DEFAULT_ALLOW_COLOR_CODE);
        nbSet+= trySetDefault(config, ALLOW_HEXADECIMAL_COLOR, DEFAULT_ALLOW_HEXADECIMAL_COLOR);
        nbSet+= trySetDefault(config, PERMISSION_NEEDED_FOR_COLOR, DEFAULT_PERMISSION_NEEDED_FOR_COLOR);
        nbSet+= trySetDefault(config, USE_OF_COLOR_COST, DEFAULT_USE_OF_COLOR_COST);
        nbSet+= trySetDefault(config, DEFAULT_LIMIT_PATH, DEFAULT_ENCHANT_LIMIT);

        // Lore Edit defaults
        for (@NotNull LoreEditType value : LoreEditType.values()) {
            String path = value.getRootPath() + ".";

            nbSet+= trySetDefault(config, path + IS_ENABLED, DEFAULT_IS_ENABLED);
            nbSet+= trySetDefault(config, path + FIXED_COST, DEFAULT_FIXED_COST);
            nbSet+= trySetDefault(config, path + PER_LINE_COST, DEFAULT_PER_LINE_COST);
            if(value.isAppend()){
                nbSet+= trySetDefault(config, path + DO_CONSUME, DEFAULT_DO_CONSUME);
            }
        }

        nbSet+= trySetDefault(config, BOOK_PERMISSION_NEEDED, DEFAULT_BOOK_PERMISSION_NEEDED);
        nbSet+= trySetDefault(config, PAPER_PERMISSION_NEEDED, DEFAULT_PAPER_PERMISSION_NEEDED);

        nbSet+= trySetDefault(config, COLOR_BOOK_COLOR_CODE, DEFAULT_COLOR_BOOK_COLOR_CODE);
        nbSet+= trySetDefault(config, COLOR_BOOK_HEX, DEFAULT_COLOR_BOOK_HEX);
        nbSet+= trySetDefault(config, COLOR_BOOK_COST, DEFAULT_COLOR_BOOK_COST);

        nbSet+= trySetDefault(config, COLOR_PAPER_COLOR_CODE, DEFAULT_COLOR_PAPER_COLOR_CODE);
        nbSet+= trySetDefault(config, COLOR_PAPER_HEX, DEFAULT_COLOR_PAPER_HEX);
        nbSet+= trySetDefault(config, COLOR_PAPER_COST, DEFAULT_COLOR_PAPER_COST);

        nbSet+= trySetDefault(config, PAPER_EDIT_ORDER, DEFAULT_PAPER_EDIT_ORDER);

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
