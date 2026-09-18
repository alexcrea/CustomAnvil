package xyz.alexcrea.cuanvil.enchant.bulk;

import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.dependency.plugins.EnchantmentSquaredDependency;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Collections;
import java.util.Map;

@NotNullByDefault
public class EnchantSquaredBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation {

    @Override
    public void bulkGet(Map<CAEnchantment, Integer> enchantmentMap, ItemStack item, ItemMeta meta) {
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null) {
            enchantmentSquared.getEnchantmentsSquared(item, enchantmentMap);
        }
    }


    @Override
    public void bulkClear(ItemStack item) {
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null) {
            CustomEnchantManager.getInstance().setItemEnchants(item, Collections.emptyMap());
        }
    }

    @Override
    public void bulkClear(ItemStack item, ItemMeta meta) {
        // item meta is not preferred for enchantment squared clear
    }
}
