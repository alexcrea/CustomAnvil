package xyz.alexcrea.cuanvil.mock;

import be.seeseemelk.mockbukkit.inventory.ItemStackMock;
import be.seeseemelk.mockbukkit.inventory.meta.ItemMetaMock;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

public class EnchantedItemStackMock extends ItemStackMock {

    public EnchantedItemStackMock(@NotNull Material type, int amount) {
        super(type, amount);
        updateItemMeta();
    }

    public EnchantedItemStackMock(@NotNull Material type) {
        this(type, 1);
        updateItemMeta();
    }

    public EnchantedItemStackMock(@NotNull ItemStack stack) throws IllegalArgumentException {
        super(stack);
        updateItemMeta();
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
        EnchantedItemStackMock clone = new EnchantedItemStackMock(this.getType());

        clone.setAmount(this.getAmount());
        clone.setDurability(this.getDurability());
        clone.setItemMeta(this.hasItemMeta() ? this.getItemMeta().clone() : null);
        clone.updateItemMeta();
        return clone;
    }

    @Override
    public void setType(@NotNull Material type) {
        super.setType(type);
        updateItemMeta();
    }

    @Override
    public boolean setItemMeta(@Nullable ItemMeta itemMeta) {
        boolean success = super.setItemMeta(itemMeta);
        updateItemMeta();
        return success;
    }

    @Override
    public void setDurability(short durability) {
        if(getType().getMaxDurability() == 0) return;
        super.setDurability(durability);
    }

    private void updateItemMeta() {
        super.setItemMeta(updateItemMeta(getType(), getItemMeta()));
    }

    private static @Nullable ItemMeta updateItemMeta(Material material, ItemMeta oldMeta) {
        if(oldMeta == null) return null;
        if(material != Material.ENCHANTED_BOOK) return oldMeta;
        if(oldMeta instanceof ItemMetaMock) return new EnchantedItemMetaMock(oldMeta);

        return oldMeta;
    }

}
