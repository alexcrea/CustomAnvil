package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.Definition;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@NotNullByDefault
public class CAEEPreV5Enchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final Definition definition;

    public CAEEPreV5Enchantment(CustomEnchantment enchantment) {
        super(enchantment.getBukkitEnchantment(), getRarity(enchantment.getBukkitEnchantment()));
        try {
            this.definition = (Definition) getDefinition.invoke(enchantment);
        } catch(IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private final static Method getDefinition;

    static {
        try {
            getDefinition = CustomEnchantment.class.getMethod("getDefinition");
        } catch(NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        if(!definition.hasConflicts()) return false;

        Set<String> conflicts = definition.getConflicts();

        for(CAEnchantment caEnchantment : enchantments.keySet()) {
            if(conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType, ItemStack item) {
        if(Material.ENCHANTED_BOOK.getKey().equals(itemType)) return false;

        return !definition.getSupportedItems().is(item);
    }
}
