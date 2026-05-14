package xyz.alexcrea.cuanvil.enchant.bulk;

import com.maddoxh.superEnchants.items.EnchantApplicator;
import com.maddoxh.superEnchants.items.EnchantReader;
import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

public class SuperEnchantBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation  {

    private Plugin plugin;
    public  SuperEnchantBulkOperation(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void bulkGet(@NotNull Map<CAEnchantment, Integer> enchantmentMap, @NotNull ItemStack item, @NotNull ItemMeta meta) {
        EnchantReader.INSTANCE.readEnchants(item).forEach((ench, level) -> {
            var enchantment = EnchantmentApi.getByKey(NamespacedKey.fromString(ench, plugin));
                if(enchantment == null) {
                    CustomAnvil.log("Enchantment " + ench + " not found in custom anvil");
                    return;
                }

                enchantmentMap.put(enchantment, level);
            }
        );
    }

    @Override
    public void bulkClear(@NotNull ItemStack item) {
        EnchantApplicator.INSTANCE.clearAllCustomEnchants(item);
    }

    @Override
    public void bulkClear(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        // item meta is not preferred for enchantment squared clear
    }

}
