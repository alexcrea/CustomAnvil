package xyz.alexcrea.cuanvil.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommonItemUtil {

    public static ItemStack sharpness(int level){
        return AnvilFuseTestUtil.prepareItem(
                ItemType.DIAMOND_SWORD,
                List.of("sharpness"),
                level
        );
    }

    public static ItemStack bane_of_arthropods(int level){
        return AnvilFuseTestUtil.prepareItem(
                ItemType.DIAMOND_SWORD,
                List.of("bane_of_arthropods"),
                level
        );
    }

}
