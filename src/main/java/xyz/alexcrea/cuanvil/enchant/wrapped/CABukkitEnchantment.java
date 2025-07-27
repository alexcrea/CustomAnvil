package xyz.alexcrea.cuanvil.enchant.wrapped;

import io.delilaheve.util.ConfigOptions;
import io.delilaheve.util.ItemUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentProperties;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.Locale;
import java.util.Objects;

/**
 * Custom Anvil enchantment implementation for vanilla registered enchantment.
 */
public class CABukkitEnchantment extends CAEnchantmentBase {

    public final @NotNull Enchantment bukkit;

    public CABukkitEnchantment(@NotNull Enchantment bukkit, @Nullable EnchantmentRarity rarity) {
        super(bukkit.getKey(),
                rarity,
                bukkit.getMaxLevel());
        this.bukkit = bukkit;
    }

    public CABukkitEnchantment(@NotNull Enchantment bukkit) {
        this(bukkit, getRarity(bukkit));
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
    public int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            return ((EnchantmentStorageMeta) meta).getStoredEnchantLevel(this.bukkit);
        } else {
            return meta.getEnchantLevel(this.bukkit);
        }
    }

    @Override
    public boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta) meta);

            return bookMeta.getStoredEnchants().containsKey(this.bukkit) ||
                    (ConfigOptions.INSTANCE.getAddBookEnchantmentAsStoredEnchantment() && item.containsEnchantment(this.bukkit));
        } else {
            return item.containsEnchantment(this.bukkit);
        }
    }

    @Override
    public void addEnchantmentUnsafe(@NotNull ItemStack item, int level) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta) item.getItemMeta());

            assert bookMeta != null;
            bookMeta.addStoredEnchant(this.bukkit, level, true);
            item.setItemMeta(bookMeta);
        } else {
            item.addUnsafeEnchantment(this.bukkit, level);
        }

    }

    @Override
    public void removeFrom(@NotNull ItemStack item) {
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = ((EnchantmentStorageMeta) item.getItemMeta());

            assert bookMeta != null;
            bookMeta.removeStoredEnchant(this.bukkit);
            bookMeta.removeEnchant(this.bukkit);
            item.setItemMeta(bookMeta);
        } else {
            item.removeEnchantment(this.bukkit);
        }

    }

    @NotNull
    public static EnchantmentRarity getRarity(Enchantment enchantment) {
        try {
            return EnchantmentProperties.valueOf(enchantment.getKey().getKey().toUpperCase(Locale.ENGLISH)).getRarity();
        } catch (Exception ignored) {
            return findRarity(enchantment);
        }
    }

    @NotNull
    protected Enchantment getEnchant() {
        return this.bukkit;
    }

    private static EnchantmentRarity findRarity(Enchantment enchantment) {
        return EnchantmentRarity.getRarity(enchantment.getAnvilCost());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CABukkitEnchantment other)) {
            return false;
        }

        return Objects.equals(this.bukkit, other.getEnchant());
    }

}
