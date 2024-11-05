package xyz.alexcrea.cuanvil.mock;

import be.seeseemelk.mockbukkit.inventory.meta.ItemMetaMock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantedItemMetaMock extends ItemMetaMock implements EnchantmentStorageMeta {


    public EnchantedItemMetaMock() {

    }

    public EnchantedItemMetaMock(@NotNull ItemMeta meta) {
        super(meta);
    }


    @Override
    public boolean hasStoredEnchants() {
        return super.hasEnchants();
    }

    @Override
    public boolean hasStoredEnchant(@NotNull Enchantment ench) {
        return super.hasEnchant(ench);
    }

    @Override
    public int getStoredEnchantLevel(@NotNull Enchantment ench) {
        return super.getEnchantLevel(ench);
    }

    @Override
    public @NotNull Map<Enchantment, Integer> getStoredEnchants() {
        return super.getEnchants();
    }

    @Override
    public boolean addStoredEnchant(@NotNull Enchantment ench, int level, boolean ignoreLevelRestriction) {
        return super.addEnchant(ench, level, ignoreLevelRestriction);
    }

    @Override
    public boolean removeStoredEnchant(@NotNull Enchantment ench) throws IllegalArgumentException {
        return super.removeEnchant(ench);
    }

    @Override
    public boolean hasConflictingStoredEnchant(@NotNull Enchantment ench) {
        return super.hasConflictingEnchant(ench);
    }


    @Override
    public EnchantedItemMetaMock clone() {
        // Not ideal but we do with what we have
        return new EnchantedItemMetaMock(this);
    }
}
