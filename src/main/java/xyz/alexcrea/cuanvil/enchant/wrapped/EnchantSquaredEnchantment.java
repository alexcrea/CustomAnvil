package xyz.alexcrea.cuanvil.enchant.wrapped;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;

import java.util.Map;
import java.util.Objects;

public class EnchantSquaredEnchantment extends WrappedEnchantment {

    private final @NotNull CustomEnchant enchant;
    public EnchantSquaredEnchantment(@NotNull CustomEnchant enchant, @NotNull Plugin enchantSquared) {
        super(Objects.requireNonNull(NamespacedKey.fromString(enchant.getType().toLowerCase(), enchantSquared)), null, enchant.getMaxLevel());
        this.enchant = enchant;

    }

    //TODO optimise for bulk operation

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
