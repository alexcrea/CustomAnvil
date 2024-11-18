package xyz.alexcrea.cuanvil.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommonItemUtil {

    public static ItemStack sharpness(int level){
        return AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("sharpness"),
                level
        );
    }

    public static ItemStack bane_of_arthropods(int level){
        return AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("bane_of_arthropods"),
                level
        );
    }


}
