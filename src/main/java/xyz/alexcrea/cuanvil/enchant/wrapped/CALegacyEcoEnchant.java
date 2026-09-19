package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentTarget;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.Map;

@NotNullByDefault
public class CALegacyEcoEnchant extends CABukkitEnchantment implements AdditionalTestEnchantment {

    private final EcoEnchant ecoEnchant;

    public CALegacyEcoEnchant(EcoEnchant ecoEnchant, Enchantment enchantment) {
        super(enchantment, EnchantmentRarity.COMMON);
        this.ecoEnchant = ecoEnchant;
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        if(enchantments.isEmpty()) return false;

        EnchantmentType type = this.ecoEnchant.getType();
        boolean isSingular = type.isSingular();

        for(CAEnchantment other : enchantments.keySet()) {
            if(other instanceof CABukkitEnchantment otherVanilla
                    && this.ecoEnchant.conflictsWith(otherVanilla.getEnchant())) {
                return true;
            }

            if(isSingular &&
                    other != this &&
                    (other instanceof CALegacyEcoEnchant otherEco) &&
                    type.equals(otherEco.ecoEnchant.getType())) {
                return true;
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

        var mat = MaterialUtil.INSTANCE.getMatFromKey(itemType);
        for(EnchantmentTarget target : this.ecoEnchant.getTargets()) {
            if(target.getMaterials().contains(mat)) {
                return false;
            }
        }

        return true;
    }
}
