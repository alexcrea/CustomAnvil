package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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


    // Temporary values, until I get something better.
    public static final boolean TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE = true;
    public static final boolean TEMPORARY_DO_BACKUP_EVERY_SAVE = true;
}
