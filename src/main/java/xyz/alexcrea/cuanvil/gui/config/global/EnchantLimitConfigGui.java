package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.Locale;

/**
 * Global Config gui for enchantment level limit settings.
 */
public class EnchantLimitConfigGui extends AbstractEnchantConfigGui<IntSettingsGui.IntSettingFactory> {

    private static final String SECTION_NAME = ConfigOptions.ENCHANT_LIMIT_ROOT;

    private static EnchantLimitConfigGui INSTANCE = null;

    @Nullable
    public static EnchantLimitConfigGui getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor of this Global gui for enchantment level limit settings.
     */
    public EnchantLimitConfigGui() {
        super("§8Enchantment Level Limit");
        if(INSTANCE == null) INSTANCE = this;

        init();
    }

    @Override
    public IntSettingsGui.IntSettingFactory createFactory(CAEnchantment enchant) {
        String key = enchant.getKey().toString().toLowerCase(Locale.ROOT);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key.replace(":", "_"));

        var defaultValue = enchant.defaultMaxLevel();

        return new IntSettingsGui.IntSettingFactory(prettyKey + " Limit", this,
                SECTION_NAME + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                Collections.singletonList(
                        "§7Maximum applied level of " + prettyKey
                ),
                -1, 255, -1,
                1, 5, 10, 50, 100){

            @Override
            public int getConfiguredValue() {
                var value = ConfigOptions.INSTANCE.rawEnchantLimit(enchant);
                return Math.min(value, ConfigOptions.ENCHANT_LIMIT);
            }

            @Override
            public String valueDisplayName(IntSettingsGui.ValueDisplayType type, int value) {

                if(value < 0) {
                    return switch (type) {
                        case CURRENT -> "Default (" + defaultValue + ")";
                        case RESET -> String.valueOf(defaultValue);
                        default -> "Default";
                    };

                }
                else return super.valueDisplayName(type, value);
            }
        };
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, IntSettingsGui.IntSettingFactory inventoryFactory) {
        return inventoryFactory.getItem(
                Material.ENCHANTED_BOOK,
                inventoryFactory.getTitle());
    }

}
