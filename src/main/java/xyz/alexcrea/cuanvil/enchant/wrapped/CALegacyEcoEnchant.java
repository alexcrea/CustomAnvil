package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentTarget;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;

public class CALegacyEcoEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final @NotNull EcoEnchant ecoEnchant;

    public CALegacyEcoEnchant(@NotNull EcoEnchant ecoEnchant, @NotNull Enchantment enchantment) {
        super(enchantment, EnchantmentRarity.COMMON);
        this.ecoEnchant = ecoEnchant;
    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        if (enchantments.isEmpty()) return false;

        EnchantmentType type = this.ecoEnchant.getType();
        boolean isSingular = type.isSingular();

        for (CAEnchantment other : enchantments.keySet()) {
            if (other instanceof CABukkitEnchantment otherVanilla
                    && this.ecoEnchant.conflictsWith(otherVanilla.getEnchant())) {
                return true;
            }

            if (isSingular &&
                    other != this &&
                    (other instanceof CALegacyEcoEnchant otherEco) &&
                    type.equals(otherEco.ecoEnchant.getType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments,
                                  @NotNull Material itemMat,
                                  @NotNull ItemStack item) {
        if (Material.ENCHANTED_BOOK.equals(itemMat)) {
            return false;
        }

        for (EnchantmentTarget target : this.ecoEnchant.getTargets()) {
            if (target.getMaterials().contains(itemMat)) {
                return false;
            }
        }

        return true;
    }
}
