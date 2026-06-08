package xyz.alexcrea.cuanvil.enchant.wrapped;

import com.maddoxh.superEnchants.enchants.CustomEnchant;
import com.maddoxh.superEnchants.enchants.EnchantManager;
import com.maddoxh.superEnchants.items.EnchantApplicator;
import com.maddoxh.superEnchants.items.EnchantReader;
import com.maddoxh.superEnchants.util.ConflictChecker;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.HashMap;
import java.util.Map;

public class CASuperEnchantEnchantment extends CAEnchantmentBase implements AdditionalTestEnchantment {

    private @NotNull CustomEnchant enchant;
    private @NotNull EnchantManager enchantManager;

    public CASuperEnchantEnchantment(@NotNull CustomEnchant enchant, @NotNull Plugin plugin, @NotNull EnchantManager enchantManager) {
        super(NamespacedKey.fromString(enchant.getId(), plugin), EnchantmentRarity.COMMON, enchant.getMaxLevel());

        this.enchant = enchant;
        this.enchantManager = enchantManager;
    }

    @Override
    public int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        return EnchantReader.INSTANCE.getEnchantLevel(item, enchant.getId());
    }

    @Override
    public boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        return EnchantReader.INSTANCE.hasEnchant(item, enchant.getId());
    }

    @Override
    public void addEnchantmentUnsafe(@NotNull ItemStack item, int level) {
        EnchantApplicator.INSTANCE.applyEnchant(item, enchant.getId(), level);
    }

    @Override
    public void removeFrom(@NotNull ItemStack item) {
        EnchantApplicator.INSTANCE.removeEnchant(item, enchant.getId());
    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType) {
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
    public boolean isItemConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull NamespacedKey itemType, @NotNull ItemStack item) {
        return !enchant.canApplyTo(item.getType());
    }
}
