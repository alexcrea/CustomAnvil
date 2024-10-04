package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Locale;

public class EnchantMergeLimitConfigGui extends AbstractEnchantConfigGui<IntSettingsGui.IntSettingFactory> {

    private static final String SECTION_NAME = "disable-merge-over";

    private static EnchantMergeLimitConfigGui INSTANCE = null;

    @Nullable
    public static EnchantMergeLimitConfigGui getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor of this Global gui for enchantment level limit settings.
     */
    public EnchantMergeLimitConfigGui() {
        super("§8Enchantment Maximum Merge Level");
        if(INSTANCE == null) INSTANCE = this;

        init();
    }

    @Override
    public IntSettingsGui.IntSettingFactory createFactory(CAEnchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key);

        return new IntSettingsGui.IntSettingFactory(prettyKey + " Merge Limit", this,
                SECTION_NAME + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7Maximum merge level for for " + prettyKey,
                        "",
                        "§7For example, if set to §e2§7, §alvl1 §7+ §alvl1 §7of will give a §alvl2",
                        "§7But §alvl2 §7+ §alvl2 §7will not give a §clv3§7.",
                        "§7Will still not merge above max enchantment level",
                        "§e-1 §7(default) will set the merge limit to enchantment's maximum level"
                ),
                -1, 255, -1,
                1, 5, 10, 50, 100);
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, IntSettingsGui.IntSettingFactory inventoryFactory) {
        return inventoryFactory.getItem(
                Material.ENCHANTED_BOOK,
                inventoryFactory.getTitle());
    }
}
