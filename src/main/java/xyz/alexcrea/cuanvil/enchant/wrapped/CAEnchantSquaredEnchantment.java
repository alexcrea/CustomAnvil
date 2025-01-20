package xyz.alexcrea.cuanvil.enchant.wrapped;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Objects;

public class CAEnchantSquaredEnchantment extends CAEnchantmentBase {

    public final @NotNull CustomEnchant enchant;

    public CAEnchantSquaredEnchantment(@NotNull CustomEnchant enchant) {
        super(Objects.requireNonNull(
                        Objects.requireNonNull(DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility()).getKeyFromEnchant(enchant)),
                EnchantmentRarity.COMMON,
                enchant.getMaxLevel());
        this.enchant = enchant;

    }

    public @NotNull CustomEnchant getEnchant() {
        return enchant;
    }

    @Override
    public boolean isGetOptimised() {
        return true;
    }

    @Override
    public boolean isCleanOptimised() {
        return true;
    }

    @Override
    public boolean isAllowed(@NotNull HumanEntity human) {
        return this.enchant.hasPermission(human);
    }

    @Override
    public int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        return CustomEnchantManager.getInstance().getEnchantStrength(item, this.enchant.getType());
    }

    @Override
    public boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        Map<CustomEnchant, Integer> enchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item);
        return enchants.containsKey(this.enchant);
    }

    @Override
    public void addEnchantmentUnsafe(@NotNull ItemStack item, int level) {
        CustomEnchantManager.getInstance().addEnchant(item, this.enchant.getType(), level);
    }

    @Override
    public void removeFrom(@NotNull ItemStack item) {
        CustomEnchantManager.getInstance().removeEnchant(item, this.enchant.getType());
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CAEnchantSquaredEnchantment other)) {
            return false;
        }

        return this.enchant.equals(other.getEnchant());
    }

}
