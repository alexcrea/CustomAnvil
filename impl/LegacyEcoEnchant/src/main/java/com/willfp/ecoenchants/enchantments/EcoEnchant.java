package com.willfp.ecoenchants.enchantments;

import com.willfp.ecoenchants.enchantments.meta.EnchantmentTarget;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Mock class for legacy package of eco enchants
 */
public class EcoEnchant {

    public boolean conflictsWith(@NotNull Enchantment enchant) {
        return false;
    }

    public Set<EnchantmentTarget> getTargets() {
        return null;
    }

    public EnchantmentType getType() {
        return null;
    }
}
