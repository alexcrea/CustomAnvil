package xyz.alexcrea.cuanvil.enchant.bulk;

import com.maddoxh.superEnchants.items.EnchantApplicator;
import com.maddoxh.superEnchants.items.EnchantReader;
import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

@NotNullByDefault
public class SuperEnchantBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation {

    private final Plugin plugin;

    public SuperEnchantBulkOperation(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void bulkGet(Map<CAEnchantment, Integer> enchantmentMap, ItemStack item, ItemMeta meta) {
        var enchants = EnchantReader.INSTANCE.readEnchants(item);

        enchants.forEach((ench, level) -> {
            var enchantment = EnchantmentApi.getByKey(NamespacedKey.fromString(ench, plugin));
            if(enchantment == null) {
                CustomAnvil.log("Enchantment " + ench + " not found in custom anvil");
                return;
            }

            enchantmentMap.put(enchantment, level);
        });
    }

    @Override
    public void bulkClear(ItemStack item) {
        EnchantApplicator.INSTANCE.clearAllCustomEnchants(item);
    }

    @Override
    public void bulkClear(ItemStack item, ItemMeta meta) {
        // item meta is not preferred for super enchant
    }

}
