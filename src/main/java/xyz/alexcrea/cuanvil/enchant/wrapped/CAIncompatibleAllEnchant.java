package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.*;

import java.util.Map;

/**
 * Represent an enchantment incompatible with every other enchantments
 */
public class CAIncompatibleAllEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    public CAIncompatibleAllEnchant(@NotNull Enchantment enchantment, @Nullable EnchantmentRarity rarity) {
        super(enchantment, rarity);
    }

    public CAIncompatibleAllEnchant(@NotNull Enchantment enchantment) {
        super(enchantment);
    }


    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        return true;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat, @NotNull ItemStack item) {
        return false;
    }
}
