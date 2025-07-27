package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class CAEEV5Enchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull CustomEnchantment eeenchantment;
    @NotNull EnchantDefinition definition;

    public CAEEV5Enchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), EnchantmentRarity.getRarity(enchantment.getDefinition().getAnvilCost()));
        this.eeenchantment = enchantment;
        this.definition = enchantment.getDefinition();

    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull ItemType type) {
        if (!definition.hasConflicts()) return false;

        Set<String> conflicts = definition.getExclusiveSet();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if (conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull ItemType type, @NotNull ItemStack item) {
        if (ItemType.ENCHANTED_BOOK.equals(type)) return false;

        String key = type.getKey().getKey();
        ItemSet primary = eeenchantment.getPrimaryItems();
        if (primary.getMaterials().contains(key)) return false;

        ItemSet supported = eeenchantment.getSupportedItems();
        if (supported.getMaterials().contains(key)) return false;

        return true;
    }

}
