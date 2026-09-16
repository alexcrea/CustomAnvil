package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import kotlin.Pair;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.util.MetricType;
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil;
import xyz.alexcrea.cuanvil.util.config.LoreEditType;

import java.util.List;

import static io.delilaheve.util.ConfigOptions.*;
import static xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.*;

@NotNullByDefault
public class PluginSetDefault {

    private static final List<Pair<String, Object>> defaults = List.of(
            new Pair<>(METRIC_TYPE, MetricType.AUTO.getValue()),
            new Pair<>(METRIC_COLLECT_ERROR, true),

            new Pair<>(CAP_ANVIL_COST, DEFAULT_CAP_ANVIL_COST),
            new Pair<>(MAX_ANVIL_COST, DEFAULT_MAX_ANVIL_COST),
            new Pair<>(REMOVE_ANVIL_COST_LIMIT, DEFAULT_REMOVE_ANVIL_COST_LIMIT),
            new Pair<>(REPLACE_TOO_EXPENSIVE, DEFAULT_REPLACE_TOO_EXPENSIVE),
            new Pair<>(ITEM_REPAIR_COST, DEFAULT_ITEM_REPAIR_COST),
            new Pair<>(UNIT_REPAIR_COST, DEFAULT_UNIT_REPAIR_COST),
            new Pair<>(ITEM_RENAME_COST, DEFAULT_ITEM_RENAME_COST),
            new Pair<>(SACRIFICE_ILLEGAL_COST, DEFAULT_SACRIFICE_ILLEGAL_COST),
            new Pair<>(ConfigOptions.ALLOW_COLOUR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOUR_CODE),
            new Pair<>(ALLOW_HEXADECIMAL_COLOUR, DEFAULT_ALLOW_HEXADECIMAL_COLOUR),
            new Pair<>(PERMISSION_NEEDED_FOR_COLOUR, DEFAULT_PERMISSION_NEEDED_FOR_COLOUR),
            new Pair<>(USE_OF_COLOUR_COST, DEFAULT_USE_OF_COLOUR_COST),
            new Pair<>(PER_COLOUR_CODE_PERMISSION, DEFAULT_PER_COLOUR_CODE_PERMISSION),

            new Pair<>(BOOK_PERMISSION_NEEDED, DEFAULT_BOOK_PERMISSION_NEEDED),
            new Pair<>(PAPER_PERMISSION_NEEDED, DEFAULT_PAPER_PERMISSION_NEEDED),

            new Pair<>(PAPER_EDIT_ORDER, DEFAULT_PAPER_EDIT_ORDER),

            new Pair<>(DIALOG_RENAME_ENABLED, DEFAULT_DIALOG_RENAME_ENABLED),
            new Pair<>(DIALOG_MAX_SIZE, DEFAULT_DIALOG_MAX_SIZE),
            new Pair<>(DIALOG_RENAME_USE_PERMISSION, DEFAULT_DIALOG_RENAME_USE_PERMISSION),
            new Pair<>(DIALOG_KEEP_USER_TEXT, DEFAULT_DIALOG_KEEP_USER_TEXT),

            new Pair<>(INCLUDE_LEFT_ENCHANTMENT_FOR_COST, DEFAULT_INCLUDE_LEFT_ENCHANTMENT_FOR_COST),

            new Pair<>(DEBUG_LOGGING, DEFAULT_DEBUG_LOG),
            new Pair<>(VERBOSE_DEBUG_LOGGING, DEFAULT_VERBOSE_DEBUG_LOG),
            new Pair<>(SHOW_CONSOLE_DEBUG_LOGGING, DEFAULT_SHOW_CONSOLE_DEBUG_LOGGING)
    );

    public static boolean reAddMissingDefault(UpdateHandler.UpdatedConfigList toSave) {
        FileConfiguration config = toSave.use(ConfigHolder.DEFAULT).getConfig();

        int nbSet = 0;

        for(Pair<String, Object> aDefault : defaults) {
            nbSet += trySetDefault(config, aDefault.getFirst(), aDefault.getSecond().toString());
        }

        // Lore Edit defaults
        for(LoreEditType value : LoreEditType.getEntries()) {
            String path = value.getRootPath() + ".";

            nbSet += trySetDefault(config, path + IS_ENABLED, DEFAULT_IS_ENABLED);
            nbSet += trySetDefault(config, path + FIXED_COST, DEFAULT_FIXED_COST);

            nbSet += trySetDefault(config, path + DO_CONSUME, DEFAULT_DO_CONSUME);
            if(value.isMultiLine()) {
                nbSet += trySetDefault(config, path + PER_LINE_COST, DEFAULT_PER_LINE_COST);
            }
            if(value.isAppend()) {
                nbSet += trySetDefault(config, path + LoreEditConfigUtil.ALLOW_COLOUR_CODE, LoreEditConfigUtil.DEFAULT_ALLOW_COLOUR_CODE);
                nbSet += trySetDefault(config, path + ALLOW_HEX_COLOUR, DEFAULT_ALLOW_HEX_COLOUR);
                nbSet += trySetDefault(config, path + USE_COLOUR_COST, DEFAULT_USE_COLOUR_COST);
            } else {
                nbSet += trySetDefault(config, path + REMOVE_COLOUR_COST, DEFAULT_REMOVE_COLOUR_COST);
            }
        }

        if(nbSet > 0) {
            CustomAnvil.instance.getLogger().info("Adding " + nbSet + " absent default config values.");
            return true;
        }

        return false;
    }

    private static int trySetDefault(FileConfiguration config, String path, String value) {
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }

    private static int trySetDefault(FileConfiguration config, String path, int value) {
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }

    private static int trySetDefault(FileConfiguration config, String path, boolean value) {
        if(config.isSet(path)) return 0;

        config.set(path, value);
        return 1;
    }

}
