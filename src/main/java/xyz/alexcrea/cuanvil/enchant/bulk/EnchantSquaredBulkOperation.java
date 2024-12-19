package xyz.alexcrea.cuanvil.enchant.bulk;

import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.dependency.EnchantmentSquaredDependency;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Collections;
import java.util.Map;

public class EnchantSquaredBulkOperation implements BulkGetEnchantOperation, BulkCleanEnchantOperation {

    @Override
    public void bulkGet(@NotNull Map<CAEnchantment, Integer> enchantmentMap, @NotNull ItemStack item, @NotNull ItemMeta meta) {
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null){
            enchantmentSquared.getEnchantmentsSquared(item, enchantmentMap);
        }
    }


    @Override
    public void bulkClear(@NotNull ItemStack item) {
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null){
            CustomEnchantManager.getInstance().setItemEnchants(item, Collections.emptyMap());
        }
    }

    @Override
    public void bulkClear(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        // item meta is not preferred for enchantment squared clear
    }
}
