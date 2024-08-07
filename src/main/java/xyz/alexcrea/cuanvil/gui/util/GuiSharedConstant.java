package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;

import java.util.*;

public class GuiSharedConstant {

    private GuiSharedConstant(){}

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

        GuiGlobalItems.addBackItem(BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE, MainConfigGui.getInstance());

        GuiGlobalItems.addBackgroundItem(BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE);
        BACK_TO_MAIN_MENU_BIG_LIST_DISPLAY_BACKGROUND_PANE.bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);

    }

    public static final ItemStack CANCEL_ITEM;
    public static final ItemStack CONFIRM_ITEM;
    public static final ItemStack CONFIRM_PERMANENT_ITEM;

    static {
        CANCEL_ITEM = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta meta = CANCEL_ITEM.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cCancel");
        meta.setLore(Collections.singletonList("§7Cancel current action and return to previous menu."));
        CANCEL_ITEM.setItemMeta(meta);

        CONFIRM_ITEM = new ItemStack(Material.GREEN_TERRACOTTA);
        meta = CONFIRM_ITEM.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aConfirm");
        meta.setLore(Collections.singletonList("§7Confirm current action."));
        CONFIRM_ITEM.setItemMeta(meta);

        CONFIRM_PERMANENT_ITEM = new ItemStack(Material.GREEN_TERRACOTTA);
        meta = CONFIRM_PERMANENT_ITEM.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aConfirm");
        meta.setLore(Arrays.asList("§7Confirm current action.",
                "§4Cation: This action can't be canceled."));
        CONFIRM_PERMANENT_ITEM.setItemMeta(meta);
    }

    public static void loadConstants(){}

}
