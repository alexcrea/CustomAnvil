package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GuiSharedConstant {

    public static final List<Enchantment> SORTED_ENCHANTMENT_LIST;

    static {
        SORTED_ENCHANTMENT_LIST = Arrays.asList(Enchantment.values());
        SORTED_ENCHANTMENT_LIST.sort(Comparator.comparing(ench -> ench.getKey().getKey()));
    }

    public static final Material SECONDARY_BACKGROUND_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;
    public static final GuiItem SECONDARY_BACKGROUND_ITEM = GuiGlobalItems.backgroundItem(GuiSharedConstant.SECONDARY_BACKGROUND_MATERIAL);

    public static final String EMPTY_GUI_FULL_LINE = "000000000";

    // Temporary values, until I get something better.
    public static final boolean TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE = true;
    public static final boolean TEMPORARY_DO_BACKUP_EVERY_SAVE = true;

    public static final PatternPane BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE;

    static {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B11111111"
        );
        BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE = new PatternPane(0, 0, 9, 6, Pane.Priority.LOW, pattern);

        GuiGlobalItems.addBackItem(BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE, MainConfigGui.INSTANCE);

        GuiGlobalItems.addBackgroundItem(BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE);
        BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE.bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);

    }

}
