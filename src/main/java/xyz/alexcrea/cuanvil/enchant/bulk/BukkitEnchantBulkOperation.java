package xyz.alexcrea.cuanvil.enchant.bulk;

import io.delilaheve.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

public class BukkitEnchantBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation {

    @Override
    public void bulkGet(@NotNull Map<CAEnchantment, Integer> enchantmentList, @NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            ((EnchantmentStorageMeta)meta).getStoredEnchants().forEach((enchantment, level) ->
                    enchantmentList.put(EnchantmentApi.getByKey(enchantment.getKey()), level)
            );
        } else {
            item.getEnchantments().forEach((enchantment, level) ->
                    enchantmentList.put(EnchantmentApi.getByKey(enchantment.getKey()), level)
            );
        }
    }

    @Override
    public void bulkClear(@NotNull ItemStack item) {
        if (item.getType() != Material.ENCHANTED_BOOK) {

            item.getEnchantments().forEach((enchantment, level) ->
                    item.removeEnchantment(enchantment)
            );
        }
    }

    @Override
    public void bulkClear(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
            bookMeta.getStoredEnchants().forEach((enchantment, leve) ->
                    bookMeta.removeStoredEnchant(enchantment)
            );
        }

    }
}
