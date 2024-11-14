package xyz.alexcrea.cuanvil.mock;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;

public class EnchantedItemStackMock extends ItemStackMock {

    public EnchantedItemStackMock(@NotNull Material type, int amount) {
        super(type, amount);
    }

    public EnchantedItemStackMock(@NotNull Material type) {
        this(type, 1);
    }

    public EnchantedItemStackMock(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public int removeEnchantment(@NotNull Enchantment ench) {
        if(!this.hasItemMeta()) return 0;

        int level = this.getEnchantmentLevel(ench);
        this.getItemMeta().removeEnchant(ench);

        return level;
    }

    @Override
    public void removeEnchantments() {
        if(!this.hasItemMeta()) return;

        this.getItemMeta().removeEnchantments();
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) return false;

        return getItemMeta().equals(((ItemStack)obj).getItemMeta());
    }

    @Override
    public @NotNull ItemStack clone() {
        EnchantedItemStackMock clone = new EnchantedItemStackMock(this.getType());

        clone.setAmount(this.getAmount());
        clone.setItemMeta(this.getItemMeta());
        clone.setDurability(this.getDurability());
        return clone;
    }

}
