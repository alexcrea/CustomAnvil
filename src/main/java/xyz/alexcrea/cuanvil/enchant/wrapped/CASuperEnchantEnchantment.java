package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.maddoxh.superEnchants.enchants.CustomEnchant;
import com.maddoxh.superEnchants.enchants.EnchantManager;
import com.maddoxh.superEnchants.items.EnchantApplicator;
import com.maddoxh.superEnchants.items.EnchantReader;
import com.maddoxh.superEnchants.util.ConflictChecker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NotNullByDefault
public class CASuperEnchantEnchantment extends CAEnchantmentBase implements AdditionalTestEnchantment {

    private final CustomEnchant enchant;
    private final EnchantManager enchantManager;

    public CASuperEnchantEnchantment(CustomEnchant enchant, Plugin plugin, EnchantManager enchantManager) {
        super(Objects.requireNonNull(NamespacedKey.fromString(enchant.getId(), plugin)),
                EnchantmentRarity.COMMON,
                enchant.getMaxLevel()
        );

        this.enchant = enchant;
        this.enchantManager = enchantManager;
    }

    @Override
    public int getLevel(ItemStack item, ItemMeta meta) {
        return EnchantReader.INSTANCE.getEnchantLevel(item, enchant.getId());
    }

    @Override
    public boolean isEnchantmentPresent(ItemStack item, ItemMeta meta) {
        return EnchantReader.INSTANCE.hasEnchant(item, enchant.getId());
    }

    @Override
    public void addEnchantmentUnsafe(ItemStack item, int level) {
        EnchantApplicator.INSTANCE.applyEnchant(item, enchant.getId(), level);
    }

    @Override
    public void removeFrom(ItemStack item) {
        EnchantApplicator.INSTANCE.removeEnchant(item, enchant.getId());
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemType) {
        var idMap = new HashMap<String, Integer>();

        enchantments.forEach((enchant, level) -> {
            if(!(enchant instanceof CASuperEnchantEnchantment superEnch)) return;
            idMap.put(superEnch.enchant.getId(), level);
        });

        return ConflictChecker.INSTANCE.hasConflict(
                idMap,
                enchant.getId(),
                enchantManager
        ) != null;
    }

    @Override
    public boolean isItemConflict(
            Map<CAEnchantment, Integer> enchantments,
            NamespacedKey itemType,
            ItemStack item,
            ItemStack original
    ) {
        if(Material.ENCHANTED_BOOK.equals(item.getType())) return false;

        return !enchant.canApplyTo(item.getType());
    }

    @Override
    public boolean isCleanOptimised() {
        return true;
    }

    @Override
    public boolean isGetOptimised() {
        return true;
    }
}
