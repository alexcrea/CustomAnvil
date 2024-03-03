package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.StringUtil;

import java.util.Locale;

public class EnchantLimitConfigGui extends AbstractEnchantConfigGui<IntSettingsGui.IntSettingFactory> {

    private final static String SECTION_NAME = "enchant_limits";

    public final static EnchantLimitConfigGui INSTANCE = new EnchantLimitConfigGui();

    static {
        INSTANCE.init();
    }

    private EnchantLimitConfigGui() {
        super("\u00A78Enchantment Level Limit");

    }

    @Override
    public IntSettingsGui.IntSettingFactory getFactoryFromEnchant(Enchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        String prettyKey = StringUtil.snakeToUpperSpacedCase(key);

        return IntSettingsGui.intFactory(prettyKey+" Level Limit", this,
                SECTION_NAME+'.'+key, ConfigHolder.DEFAULT_CONFIG, 0, 255,
                enchant.getMaxLevel(),
                1, 5, 10, 50, 100);
    }

    @Override
    public GuiItem getItemFromFactory(IntSettingsGui.IntSettingFactory inventoryFactory) {
        return GuiGlobalItems.intSettingGuiItem(inventoryFactory,
                Material.ENCHANTED_BOOK,
                inventoryFactory.getTitle());
    }

}
