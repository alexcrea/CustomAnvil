package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
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

    private static final String SECTION_NAME = "enchant_limits";

    public static final EnchantLimitConfigGui INSTANCE = new EnchantLimitConfigGui();

    static {
        INSTANCE.init();
    }

    /**
     * Constructor of this Global gui for enchantment level limit settings.
     */
    private EnchantLimitConfigGui() {
        super("\u00A78Enchantment Level Limit");

    }

    @Override
    public IntSettingsGui.IntSettingFactory createFactory(CAEnchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key);

        return IntSettingsGui.intFactory(prettyKey + " Level Limit", this,
                SECTION_NAME + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                Collections.singletonList(
                        "\u00A77Maximum applied level of " + prettyKey
                ),
                0, 255,
                enchant.defaultMaxLevel(),
                1, 5, 10, 50, 100);
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, IntSettingsGui.IntSettingFactory inventoryFactory) {
        return inventoryFactory.getItem(
                Material.ENCHANTED_BOOK,
                inventoryFactory.getTitle());
    }

}
