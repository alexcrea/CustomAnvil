package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;

/**
 * Represent an enchantment incompatible with every other enchantments
 */
@SuppressWarnings("unused")
@NotNullByDefault
public class CAIncompatibleAllEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    public CAIncompatibleAllEnchant(Enchantment enchantment, @Nullable EnchantmentRarity rarity) {
        super(enchantment, rarity);
    }

    public CAIncompatibleAllEnchant(Enchantment enchantment) {
        super(enchantment);
    }


    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        return !enchantments.isEmpty() && !(enchantments.size() == 1 && enchantments.containsKey(this));
    }

    @Override
    public boolean isItemConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType, ItemStack item) {
        return false;
    }
}
