package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.willfp.ecoenchants.enchant.EcoEnchant;
import com.willfp.ecoenchants.target.EnchantmentTarget;
import com.willfp.ecoenchants.type.EnchantmentType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class CAEcoEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final @NotNull EcoEnchant ecoEnchant;

    public CAEcoEnchant(@NotNull EcoEnchant enchant) {
        super(enchant.getEnchantment(), EnchantmentRarity.COMMON);
        this.ecoEnchant = enchant;
    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull ItemType itemType) {
        if (enchantments.isEmpty()) return false;

        if (this.ecoEnchant.getConflictsWithEverything()) {
            return true;
        }

        HashMap<EnchantmentType, Integer> typeAmountMap = new HashMap<>();

        for (CAEnchantment other : enchantments.keySet()) {
            if (other instanceof CABukkitEnchantment otherVanilla
                    && this.ecoEnchant.conflictsWith(otherVanilla.getEnchant())) {
                return true;
            }

            if (other instanceof CAEcoEnchant ecoOther) {
                EnchantmentType type = ecoOther.ecoEnchant.getType();
                typeAmountMap.putIfAbsent(type, 0);

                int amount = typeAmountMap.get(type) + 1;
                if (amount > type.getLimit()) {
                    return true;
                }

                typeAmountMap.put(type, amount);
            }

        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments,
                                  @NotNull ItemType type,
                                  @NotNull ItemStack item) {
        if (ItemType.ENCHANTED_BOOK.equals(type)) {
            return false;
        }

        for (EnchantmentTarget target : this.ecoEnchant.getTargets()) {
            if (target.matches(item)) {
                return false;
            }
        }

        return true;
    }
}
