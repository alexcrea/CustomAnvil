package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Set;

public class CALegacyEEEnchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull EnchantmentData eeenchantment;

    public CALegacyEEEnchantment(@NotNull EnchantmentData enchantment) {
        super(enchantment.getEnchantment(), EnchantmentRarity.getRarity(enchantment.getAnvilCost()));
        this.eeenchantment = enchantment;

    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType) {
        if (!eeenchantment.hasConflicts()) return false;

        Set<String> conflicts = eeenchantment.getConflicts();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if (conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType, @NotNull ItemStack item) {
        if (Material.ENCHANTED_BOOK.getKey().equals(itemType)) return false;

        return !eeenchantment.getSupportedItems().is(item);
    }
}
