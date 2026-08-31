package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.lang.MsgUI;
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
        super(MsgUI.INSTANCE.getENCHANTMENT_MERGE_LIMIT_TITLE());
        if(INSTANCE == null) INSTANCE = this;

        init();
    }

    /**
     * Constructor of this Global gui for enchantment level limit settings.
     */
    public EnchantMergeLimitConfigGui(Gui parent) {
        super(MsgUI.INSTANCE.getENCHANTMENT_MERGE_LIMIT_TITLE(), parent);
    }


    @Override
    public IntSettingsGui.IntSettingFactory createFactory(CAEnchantment enchant) {
        return createFactory(enchant, this);
    }

    public static IntSettingsGui.IntSettingFactory createFactory(CAEnchantment enchant, ValueUpdatableGui parent) {
        String key = enchant.getKey().toString().toLowerCase(Locale.ROOT);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key.replace(":", "_"));

        return new IntSettingsGui.IntSettingFactory(
                MsgUI.INSTANCE.getENCHANTMENT_MERGE_LIMIT_ELEMENT_TITLE(), parent,
                SECTION_NAME + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                MsgUI.INSTANCE.getENCHANTMENT_MERGE_LIMIT_ELEMENT_DESCRIPTION(), prettyKey,
                -1, 255, -1,
                1, 5, 10, 50, 100) {

            @Override
            public int getConfiguredValue() {
                return ConfigOptions.INSTANCE.maxBeforeMergeDisabled(enchant);
            }
        };
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, IntSettingsGui.IntSettingFactory inventoryFactory) {
        return inventoryFactory.getItem(
                Material.ENCHANTED_BOOK,
                inventoryFactory.getTitle(),
                inventoryFactory.getParam()
        );
    }
}
