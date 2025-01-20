package xyz.alexcrea.cuanvil.enchant.bulk;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

/**
 * Bulk operation for get enchantments operations.
 */
public interface BulkGetEnchantOperation {

    /**
     * Bulk get part of the stored enchantment of this item.
     * @param enchantmentMap Mutable map of collected enchantment. should b
     * @param item The item to get enchantment from. Should not get edited.
     * @param meta The item meta to get enchantment from. Should not get edited.
     */
    void bulkGet(@NotNull Map<CAEnchantment, Integer> enchantmentMap, @NotNull ItemStack item, @NotNull ItemMeta meta);

}
