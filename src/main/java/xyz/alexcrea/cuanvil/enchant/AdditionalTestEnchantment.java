package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface AdditionalTestEnchantment {

    /**
     * Test if the provided enchantments can be compatible with this enchantment. only non-Custom Anvil conflict.
     * @param enchantments Immutable map of validated enchantments for the item.
     * @param itemMat Material of the tested item.
     * @return If there is a conflict with the enchantments.
     */
    boolean isEnchantConflict(
            @NotNull Map<CAEnchantment, Integer> enchantments,
            @NotNull Material itemMat);


    /**
     * Test if the provided item can be compatible with this enchantment. only non-Custom Anvil conflict.
     * @param enchantments Immutable map of validated enchantments for the item.
     * @param itemMat Material of the tested item.
     * @param item Provide a new instance of the used item stack with the partial enchantment applied.
     * @return If there is a conflict with the enchantment and the item.
     */
    boolean isItemConflict(
            @NotNull Map<CAEnchantment, Integer> enchantments,
            @NotNull Material itemMat,
            @NotNull ItemStack item);

}
