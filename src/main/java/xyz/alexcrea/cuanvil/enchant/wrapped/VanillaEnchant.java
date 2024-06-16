package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;

public class VanillaEnchant extends WrappedEnchantment {

    private final @NotNull Enchantment enchantment;
    public VanillaEnchant(@NotNull Enchantment enchantment){
        super(enchantment.getKey(),
                enchantment.getName(),
                EnchantmentRarity.COMMON);//TODO determine rarity
        this.enchantment = enchantment;
    }

    @Override
    public int defaultMaxLevel() {
        return this.enchantment.getMaxLevel();
    }

    @Override
    public int enchantmentLevel(ItemStack item, ItemMeta meta) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
