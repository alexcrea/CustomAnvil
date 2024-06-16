package xyz.alexcrea.cuanvil.enchant.wrapped;

import io.delilaheve.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;

public class VanillaEnchant extends WrappedEnchantment {

    private final @NotNull Enchantment enchantment;
    public VanillaEnchant(@NotNull Enchantment enchantment){
        super(enchantment.getKey(),
                enchantment.getName(),
                EnchantmentRarity.COMMON);//TODO determine rarity
        this.enchantment = enchantment;
    }

    @Override
    public int defaultMaxLevel() {
        return this.enchantment.getMaxLevel();
    }

    @Override
    public int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (isEnchantedBook(item)) {
            return ((EnchantmentStorageMeta)meta).getStoredEnchantLevel(this.enchantment);
        } else {
            return meta.getEnchantLevel(this.enchantment);
        }
    }

    @Override
    public boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta)meta);

            return bookMeta.getStoredEnchants().containsKey(this.enchantment);
        }else{
            return item.containsEnchantment(this.enchantment);
        }
    }

    @Override
    public void addEnchantmentUnsafe(@NotNull ItemStack item, @NotNull ItemMeta meta, int level) {
        if (isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta)meta);

            bookMeta.addStoredEnchant(this.enchantment, level, true);
        } else {
            item.addUnsafeEnchantment(this.enchantment, level);
        }

    }

    @Override
    public void removeFrom(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta)meta);

            bookMeta.removeStoredEnchant(this.enchantment);
        }
        item.removeEnchantment(this.enchantment);

    }

    public static boolean isEnchantedBook(@NotNull ItemStack item){
        return Material.ENCHANTED_BOOK.equals(item.getType());
    }

}
