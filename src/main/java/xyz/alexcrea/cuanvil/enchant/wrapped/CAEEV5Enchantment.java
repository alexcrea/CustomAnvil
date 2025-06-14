package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Set;

public class CAEEV5Enchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull CustomEnchantment eeenchantment;
    @NotNull EnchantDefinition definition;

    public CAEEV5Enchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), EnchantmentRarity.getRarity(enchantment.getDefinition().getAnvilCost()));
        this.eeenchantment = enchantment;
        this.definition = enchantment.getDefinition();

    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        if (!definition.hasConflicts()) return false;

        Set<String> conflicts = definition.getExclusiveSet();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if (conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat, @NotNull ItemStack item) {
        return false;
    }

}
