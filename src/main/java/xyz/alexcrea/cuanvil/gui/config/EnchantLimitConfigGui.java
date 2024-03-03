package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnchantLimitConfigGui extends ValueUpdatableGui {

    private final static String SECTION_NAME = "enchant_limits";
    private final static Material SECONDARY_BACKGROUND_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;

    public final static EnchantLimitConfigGui INSTANCE = new EnchantLimitConfigGui();

    static {
        INSTANCE.init();
    }

    private EnchantLimitConfigGui(){
        super(6, "\u00A78Enchantment Level Limit", CustomAnvil.instance);

    }

    PatternPane backItems;
    OutlinePane filledEnchant;
    private void init(){
        // Back item panel
        Pattern pattern = new Pattern(
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "B11111111"
        );
        backItems = new PatternPane(0, 0, 9, 6, Pane.Priority.LOW, pattern);
        addPane(backItems);

        GuiGlobalItems.addBackItem(backItems, MainConfigGui.INSTANCE);

        GuiGlobalItems.addBackgroundItem(backItems);
        backItems.bindItem('1', GuiGlobalItems.backgroundItem(SECONDARY_BACKGROUND_MATERIAL));

        // enchant item panel
        filledEnchant = new OutlinePane(0, 0, 9, 5);
        filledEnchant.align(OutlinePane.Alignment.BEGIN);
        filledEnchant.setOrientation(Orientable.Orientation.HORIZONTAL);
        addPane(filledEnchant);

        prepareValues();
        updateGuiValues();
    }

    private List<IntSettingsGui.IntSettingFactory> bookItemFactoryList;
    protected void prepareValues(){
        bookItemFactoryList = new ArrayList<>();

        for (Enchantment enchant : Enchantment.values()) {
            String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
            String prettyKey = StringUtil.snakeToUpperSpacedCase(key);

            IntSettingsGui.IntSettingFactory factory = IntSettingsGui.factory(prettyKey, this,
                    SECTION_NAME+'.'+key, ConfigHolder.DEFAULT_CONFIG, 0, 255,
                    enchant.getMaxLevel(),
                    1, 5, 10, 50, 100);

            bookItemFactoryList.add(factory);
        }
    }

    @Override
    public void updateGuiValues() {
        // probably not the most efficient but hey ! it do the work
        // TODO optimise one day.. maybe

        this.filledEnchant.clear();

        for (IntSettingsGui.IntSettingFactory inventoryFactory : this.bookItemFactoryList) {

            GuiItem item = GuiGlobalItems.intSettingGuiItem(inventoryFactory,
                    Material.ENCHANTED_BOOK,
                    inventoryFactory.getTitle());
            this.filledEnchant.addItem(item);
        }

        update();
    }

}
