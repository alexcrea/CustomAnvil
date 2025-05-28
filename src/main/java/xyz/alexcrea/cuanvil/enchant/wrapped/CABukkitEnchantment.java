package xyz.alexcrea.cuanvil.enchant.wrapped;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import io.delilaheve.util.ItemUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentProperties;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

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
        } catch (IllegalArgumentException ignored) {
            return findRarity(enchantment);
        }
    }

    @NotNull
    protected Enchantment getEnchant() {
        return this.bukkit;
    }

    private static Method getAnvilCostMethod;

    static {
        Class<Enchantment> clazz = Enchantment.class;
        try {
            getAnvilCostMethod = clazz.getDeclaredMethod("getAnvilCost");
            getAnvilCostMethod.setAccessible(true);

            CustomAnvil.Companion.log("Detected getAnvilCost method");
        } catch (NoSuchMethodException e) {
            getAnvilCostMethod = null;
        }

    }

    private static final Map<EnchantmentTarget, String> targetToGroup = new HashMap<>();
    static {
        targetToGroup.put(EnchantmentTarget.ARMOR, "armors");
        targetToGroup.put(EnchantmentTarget.ARMOR_HEAD, "helmets");
        targetToGroup.put(EnchantmentTarget.ARMOR_TORSO, "chestplate");
        targetToGroup.put(EnchantmentTarget.ARMOR_LEGS, "leggings");
        targetToGroup.put(EnchantmentTarget.ARMOR_FEET, "boots");
        targetToGroup.put(EnchantmentTarget.BOW, "bow");
        targetToGroup.put(EnchantmentTarget.BREAKABLE, "can_unbreak");
        targetToGroup.put(EnchantmentTarget.CROSSBOW, "crossbow");
        targetToGroup.put(EnchantmentTarget.FISHING_ROD, "fishing_rod");
        targetToGroup.put(EnchantmentTarget.TOOL, "tools");
        targetToGroup.put(EnchantmentTarget.TRIDENT, "trident");
        targetToGroup.put(EnchantmentTarget.VANISHABLE, "can_vanish");
        targetToGroup.put(EnchantmentTarget.WEAPON, "swords");
        targetToGroup.put(EnchantmentTarget.WEARABLE, "wearable");
    }

    private static EnchantmentRarity findRarity(Enchantment enchantment) {
        if (getAnvilCostMethod == null) return EnchantmentRarity.COMMON;

        try {
            int itemCost = (int) getAnvilCostMethod.invoke(enchantment);

            return EnchantmentRarity.getRarity(itemCost);
        } catch (IllegalAccessException | InvocationTargetException e) {
            CustomAnvil.instance.getLogger().log(Level.SEVERE, "could not find cost for enchantment " + enchantment.getKey(), e);

            return EnchantmentRarity.COMMON;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CABukkitEnchantment other)) {
            return false;
        }

        return Objects.equals(this.bukkit, other.getEnchant());
    }

}
