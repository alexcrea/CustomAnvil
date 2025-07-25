package xyz.alexcrea.cuanvil.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommonItemUtil {

    public static ItemStack sharpness(Integer level){
        if(level == null) return null;

        return AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("sharpness"),
                level
        );
    }

    public static ItemStack bane_of_arthropods(Integer level){
        if(level == null) return null;

        return AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("bane_of_arthropods"),
                level
        );
    }

}
