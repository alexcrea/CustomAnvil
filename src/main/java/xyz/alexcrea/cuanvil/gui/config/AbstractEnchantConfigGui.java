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
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractEnchantConfigGui<T extends AbstractSettingGui.SettingGuiFactory> extends ValueUpdatableGui {

    private final static Material SECONDARY_BACKGROUND_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;

    protected AbstractEnchantConfigGui(String title){
        super(6, title, CustomAnvil.instance);
    }

    PatternPane backItems;
    OutlinePane filledEnchant;
    protected void init(){
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

    private List<T> bookItemFactoryList;
    protected void prepareValues(){
        bookItemFactoryList = new ArrayList<>();

        List<Enchantment> enchantments = Arrays.asList(Enchantment.values());
        enchantments.sort(Comparator.comparing(ench -> ench.getKey().getKey()));

        for (Enchantment enchant : enchantments) {
            T factory = getFactoryFromEnchant(enchant);

            bookItemFactoryList.add(factory);
        }
    }

    @Override
    public void updateGuiValues() {
        // probably not the most efficient but hey ! it do the work
        // TODO optimise one day.. maybe

        this.filledEnchant.clear();

        for (T inventoryFactory : this.bookItemFactoryList) {
            GuiItem item = getItemFromFactory(inventoryFactory);

            this.filledEnchant.addItem(item);
        }

        update();
    }


    public abstract T getFactoryFromEnchant(Enchantment enchant);

    public abstract GuiItem getItemFromFactory(T inventoryFactory);

}
