package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.Definition;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class CAEEPreV5Enchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull CustomEnchantment eeenchantment;
    @NotNull Definition definition;

    public CAEEPreV5Enchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), getRarity(enchantment.getBukkitEnchantment()));
        this.eeenchantment = enchantment;
        try {
            this.definition = (Definition) getDefinition.invoke(enchantment);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private final static Method getDefinition;
    static {
        try {
            getDefinition = CustomEnchantment.class.getMethod("getDefinition");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType) {
        if (!definition.hasConflicts()) return false;

        Set<String> conflicts = definition.getConflicts();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if (conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType, @NotNull ItemStack item) {
        if (Material.ENCHANTED_BOOK.getKey().equals(itemType)) return false;

        return !definition.getSupportedItems().is(item);
    }
}
