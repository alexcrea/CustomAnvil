package xyz.alexcrea.cuanvil.enchant.bulk;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Bulk operation for clean enchantments operations.
 */
public interface BulkCleanEnchantOperation {

    /**
     * Bulk clear part of the enchantments from this item.
     * The item can be edited freely. If you need the meta it is preferred to use {@link #bulkClear(ItemStack, ItemMeta)} if possible
     * @param item The item to clear enchantment from.
     */
    void bulkClear(@NotNull ItemStack item);

    /**
     * Bulk clear part of the enchantments from this item meta.
     * Item should not be edited as meta will be applied later.
     * If you need to edit the item and do not need the meta use {@link #bulkClear(ItemStack)}
     * @param item The item source of the item meta. should not be edited.
     * @param meta The item meta to clear enchantment from.
     */
    void bulkClear(@NotNull ItemStack item, @NotNull ItemMeta meta);

}
