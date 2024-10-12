package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.Definition;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Set;

public class CAEEEnchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull CustomEnchantment eeenchantment;
    @NotNull Definition definition;

    public CAEEEnchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), EnchantmentRarity.getRarity(enchantment.getDefinition().getAnvilCost()));
        this.eeenchantment = enchantment;
        this.definition = enchantment.getDefinition();

    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        if(!definition.hasConflicts()) return false;

        Set<String> conflicts = definition.getConflicts();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if(conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat, @NotNull ItemStack item) {
        if(Material.ENCHANTED_BOOK.equals(itemMat)) return false;

        return !definition.getSupportedItems().is(item);
    }
}
