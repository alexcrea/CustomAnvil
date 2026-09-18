package xyz.alexcrea.cuanvil.enchant.wrapped;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.dependency.plugins.EnchantmentSquaredDependency;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Map;
import java.util.Objects;

@NotNullByDefault
public class CAEnchantSquaredEnchantment extends CAEnchantmentBase {

    public final CustomEnchant enchant;

    public CAEnchantSquaredEnchantment(EnchantmentSquaredDependency compat, CustomEnchant enchant) {
        super(Objects.requireNonNull(compat.getKeyFromEnchant(enchant)),
                EnchantmentRarity.COMMON,
                enchant.getMaxLevel()
        );
        this.enchant = enchant;
    }

    public CustomEnchant getEnchant() {
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
    public boolean isAllowed(HumanEntity human) {
        return this.enchant.hasPermission(human);
    }

    @Override
    public int getLevel(ItemStack item, ItemMeta meta) {
        return CustomEnchantManager.getInstance().getEnchantStrength(item, this.enchant.getType());
    }

    @Override
    public boolean isEnchantmentPresent(ItemStack item, ItemMeta meta) {
        Map<CustomEnchant, Integer> enchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item);
        return enchants.containsKey(this.enchant);
    }

    @Override
    public void addEnchantmentUnsafe(ItemStack item, int level) {
        CustomEnchantManager.getInstance().addEnchant(item, this.enchant.getType(), level);
    }

    @Override
    public void removeFrom(ItemStack item) {
        CustomEnchantManager.getInstance().removeEnchant(item, this.enchant.getType());
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CAEnchantSquaredEnchantment other)) {
            return false;
        }

        return this.enchant.equals(other.getEnchant());
    }

}
