package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Set;

@NotNullByDefault
public class CALegacyEEEnchantment extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final EnchantmentData eeenchantment;

    public CALegacyEEEnchantment(EnchantmentData enchantment) {
        super(enchantment.getEnchantment(), new EnchantmentRarity(enchantment.getAnvilCost()));
        this.eeenchantment = enchantment;
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        if(!eeenchantment.hasConflicts()) return false;

        Set<String> conflicts = eeenchantment.getConflicts();

        for(CAEnchantment caEnchantment : enchantments.keySet()) {
            if(conflicts.contains(caEnchantment.getName())) return true;
        }

        return false;
    }

    @Override
    public boolean isItemConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType, ItemStack item) {
        if(Material.ENCHANTED_BOOK.getKey().equals(itemType)) return false;

        return !eeenchantment.getSupportedItems().is(item);
    }
}
