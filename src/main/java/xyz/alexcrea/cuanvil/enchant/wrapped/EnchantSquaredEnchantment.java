package xyz.alexcrea.cuanvil.enchant.wrapped;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;

import java.util.Map;
import java.util.Objects;

public class EnchantSquaredEnchantment extends WrappedEnchantment {

    public final @NotNull CustomEnchant enchant;
    public EnchantSquaredEnchantment(@NotNull CustomEnchant enchant) {
        super(Objects.requireNonNull(
                Objects.requireNonNull(DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility()).getKeyFromEnchant(enchant)),
                EnchantmentRarity.COMMON,
                enchant.getMaxLevel());
        this.enchant = enchant;

    }

    @Override
    protected boolean isOptimised() {
        return true;
    }

    @Override
    public boolean isAllowed(@NotNull HumanEntity human) {
        if(human instanceof Player){
            return this.enchant.hasPermission((Player) human);
        }
        // Not really ideal for maintainability but will probably never be executed. (At least I hope)
        boolean required = CustomEnchantManager.getInstance().isRequirePermissions();
        if (!required) return true;

        return human.hasPermission("es.enchant.*") || !human.hasPermission(this.enchant.getRequiredPermission());
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

}
