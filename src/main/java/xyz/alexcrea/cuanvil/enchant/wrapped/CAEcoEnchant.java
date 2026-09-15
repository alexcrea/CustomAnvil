package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.willfp.ecoenchants.enchant.EcoEnchant;
import com.willfp.ecoenchants.enchant.EcoEnchants;
import com.willfp.ecoenchants.type.EnchantmentType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NotNullByDefault
public class CAEcoEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final String enchantID;

    public CAEcoEnchant(EcoEnchant enchant) {
        super(enchant.getEnchantment(), EnchantmentRarity.COMMON);
        this.enchantID = enchant.getID();
    }

    public EcoEnchant fromKey() {
        return EcoEnchants.INSTANCE.getByID(enchantID);
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        if(enchantments.isEmpty()) return false;

        // Check if there is only self
        var result = enchantments.keySet().stream().findFirst();
        if(result.isPresent() && this.equals(result.get()))
            return false;

        var ecoEnchant = fromKey();
        if(ecoEnchant.getConflictsWithEverything()) {
            return true;
        }

        HashMap<EnchantmentType, Integer> typeAmountMap = new HashMap<>();

        for(CAEnchantment other : enchantments.keySet()) {
            if(other instanceof CABukkitEnchantment otherVanilla
                    && ecoEnchant.conflictsWith(otherVanilla.getEnchant())) {
                return true;
            }

            if(other instanceof CAEcoEnchant ecoOther) {
                EnchantmentType type = ecoOther.fromKey().getType();
                typeAmountMap.putIfAbsent(type, 0);

                int amount = typeAmountMap.get(type) + 1;
                if(amount > type.getLimit()) {
                    return true;
                }

                typeAmountMap.put(type, amount);
            }

        }

        return false;
    }

    @Override
    public boolean isItemConflict(
            Map<CAEnchantment, Integer> enchantments,
            NamespacedKey itemType,
            ItemStack item
    ) {
        if(Material.ENCHANTED_BOOK.getKey().equals(itemType)) {
            return false;
        }

        var canEnchant = fromKey().canEnchantItem(item, Collections.emptyList());
        return !canEnchant;
    }
}
