package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.willfp.ecoenchants.enchant.EcoEnchant;
import com.willfp.ecoenchants.target.EnchantmentTarget;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.group.ConflictType;

import java.util.Map;
import java.util.function.Supplier;

public class CAEcoEnchant extends CAVanillaEnchantment {

    private final @NotNull EcoEnchant ecoEnchant;

    public CAEcoEnchant(@NotNull EcoEnchant enchant) {
        super(enchant.getEnchantment(), EnchantmentRarity.COMMON);
        this.ecoEnchant = enchant;
    }

    @Override
    public @NotNull ConflictType testOtherConflicts(
            @NotNull Map<CAEnchantment, Integer> baseEnchantments,
            @NotNull Material itemMat,
            @NotNull Supplier<ItemStack> itemSupply) {

        if(!baseEnchantments.isEmpty()){
            if(this.ecoEnchant.getConflictsWithEverything()){
                return ConflictType.ENCHANTMENT_CONFLICT;
            }

            for (CAEnchantment other : baseEnchantments.keySet()) {
                if(other instanceof CAVanillaEnchantment otherVanilla  //TODO ISSUE FOUND: if vanilla is applied first conflcit is ignored. maybe export only enchantment conflict but keep target from ecoEnchant.
                        && this.ecoEnchant.conflictsWith(otherVanilla.getEnchant())){
                    return ConflictType.ENCHANTMENT_CONFLICT;
                }
            }

        }

        // Allow enchanted book
        if(Material.ENCHANTED_BOOK.equals(itemMat)){
            return ConflictType.NO_CONFLICT;
        }

        ItemStack item = itemSupply.get();
        for (EnchantmentTarget target : this.ecoEnchant.getTargets()) {
            if(target.matches(item)){
                return ConflictType.NO_CONFLICT;
            }
        }
        return ConflictType.ITEM_CONFLICT;
    }
}
