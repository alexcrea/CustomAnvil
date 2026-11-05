package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;

@NotNullByDefault
public class GuiSharedConstant {

    private GuiSharedConstant() {
    }

    public static final Material SECONDARY_BACKGROUND_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;
    public static final GuiItem SECONDARY_BACKGROUND_ITEM = GuiGlobalItems.backgroundItem(GuiSharedConstant.SECONDARY_BACKGROUND_MATERIAL);

    public static final String UPPER_FILLER_FULL_PLANE = "111111111";
    public static final String EMPTY_GUI_FULL_LINE = "000000000";
    public static final String EMPTY_FILLER_FULL_LINE = "100000001";

    // Temporary values, until I get something better.
    public static final boolean TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE = true;
    public static final boolean TEMPORARY_DO_BACKUP_EVERY_SAVE = true;

    public static void loadConstants() {
    }

}
