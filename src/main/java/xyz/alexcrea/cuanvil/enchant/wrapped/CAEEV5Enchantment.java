package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class CAEEV5Enchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    @NotNull CustomEnchantment eeenchantment;
    @NotNull Object definition;

    public CAEEV5Enchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), EnchantmentRarity.getRarity(getAnvilCost(enchantment)));
        this.eeenchantment = enchantment;
        this.definition = getDefinition(enchantment);

    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        if (!hasConflicts()) return false;

        Set<String> conflicts = getExclusiveSet();

        for (CAEnchantment caEnchantment : enchantments.keySet()) {
            if (conflicts.contains(caEnchantment.getName())) return true;
            if (conflicts.contains(caEnchantment.getKey().toString())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat, @NotNull ItemStack item) {
        if (Material.ENCHANTED_BOOK.equals(itemMat)) return false;

        String key = itemMat.getKey().getKey();
        ItemSet primary = eeenchantment.getPrimaryItems();
        if (primary.getMaterials().contains(key)) return false;

        ItemSet supported = eeenchantment.getSupportedItems();
        if (supported.getMaterials().contains(key)) return false;

        return true;
    }


    private static final Method getDefinitonMethod;

    private static final Method getAnvilCostMethod;
    private static final Method hasConflictsMethod;
    private static final Method getExclusiveSetMethod;
    static {
        var enchClazz = CustomEnchantment.class;
        try {
            getDefinitonMethod = enchClazz.getDeclaredMethod("getDefinition");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Class<?> definitionClazz;
        try {
            definitionClazz = Class.forName("su.nightexpress.excellentenchants.api.EnchantDefinition");
        } catch (ClassNotFoundException e) {
            try {
                definitionClazz = Class.forName("su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition");
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        // Now definition methods
        try {
            getAnvilCostMethod = definitionClazz.getDeclaredMethod("getAnvilCost");
            hasConflictsMethod = definitionClazz.getDeclaredMethod("hasConflicts");
            getExclusiveSetMethod = definitionClazz.getDeclaredMethod("getExclusiveSet");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    private static Object getDefinition(CustomEnchantment enchantment) {
        try {
            return getDefinitonMethod.invoke(enchantment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getAnvilCost(CustomEnchantment enchantment) {
        try {
            return (int) getAnvilCostMethod.invoke(getDefinition(enchantment));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasConflicts() {
        try {
            return (boolean) hasConflictsMethod.invoke(definition);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    private Set<String> getExclusiveSet() {
        try {
            return (Set<String>) getExclusiveSetMethod.invoke(definition);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


}
