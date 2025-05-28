package xyz.alexcrea.cuanvil.enchant.bulk;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import io.delilaheve.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

public class BukkitEnchantBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation {

    @Override
    public void bulkGet(@NotNull Map<CAEnchantment, Integer> enchantmentMap, @NotNull ItemStack item, @NotNull ItemMeta meta) {
        boolean isBook = ItemUtil.INSTANCE.isEnchantedBook(item);

        if (isBook) {
            ((EnchantmentStorageMeta) meta).getStoredEnchants().forEach((enchantment, level) ->
                    addEnchantment(enchantmentMap, enchantment, level)
            );
        }
        if(!isBook || ConfigOptions.INSTANCE.getAddBookEnchantmentAsStoredEnchantment()){
            item.getEnchantments().forEach((enchantment, level) ->
                    addEnchantment(enchantmentMap, enchantment, level)
            );
        }
    }

    public void addEnchantment(@NotNull Map<CAEnchantment, Integer> enchantmentMap, @NotNull Enchantment enchantment, int level) {
        CAEnchantment enchant = EnchantmentApi.getByKey(enchantment.getKey());
        if (enchant == null) {
            CustomAnvil.instance.getLogger().warning("Enchantment of key " + enchantment.getKey() +
                    " somehow not found in CustomAnvil ?");
            return;
        }

        enchantmentMap.put(enchant, level);
    }

    @Override
    public void bulkClear(@NotNull ItemStack item) {
        if (item.getType() != Material.ENCHANTED_BOOK || ConfigOptions.INSTANCE.getAddBookEnchantmentAsStoredEnchantment()) {

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
