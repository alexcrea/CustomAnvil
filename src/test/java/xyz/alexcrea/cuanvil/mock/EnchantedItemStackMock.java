package xyz.alexcrea.cuanvil.mock;

import be.seeseemelk.mockbukkit.inventory.ItemStackMock;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantedItemStackMock extends ItemStackMock {

    EnchantedItemStackMock(){}

    public EnchantedItemStackMock(@NotNull Material type, int amount) {
        super(type, amount);
    }

    public EnchantedItemStackMock(@NotNull Material type) {
        this(type, 1);
    }

    public EnchantedItemStackMock(@NotNull ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    @Override
    public int removeEnchantment(@NotNull Enchantment ench) {
        int level = this.getEnchantmentLevel(ench);
        this.getItemMeta().removeEnchant(ench);

        return level;
    }

    @Override
    public void removeEnchantments() {
        this.getItemMeta().removeEnchantments();
    }

    // badly imitate paper (and I hope spigot) behavior and avoid concurrent modification exception
    @Override
    public @NotNull Map<Enchantment, Integer> getEnchantments() {
        return ImmutableMap.copyOf(super.getEnchantments());
    }

    @Override
    public @NotNull ItemStack clone() {
        ItemStackMock clone = new EnchantedItemStackMock(this.getType());

        clone.setAmount(this.getAmount());
        clone.setDurability(this.getDurability());
        clone.setItemMeta(this.hasItemMeta() ? this.getItemMeta().clone() : null);
        return clone;
    }
}
