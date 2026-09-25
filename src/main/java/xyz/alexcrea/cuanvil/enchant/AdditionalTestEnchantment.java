package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Map;

@NotNullByDefault
@SuppressWarnings("unused")
public interface AdditionalTestEnchantment {

    /**
     * Test if the provided enchantments can be compatible with this enchantment. only non-Custom Anvil conflict.
     * @param enchantments Immutable map of validated enchantments for the item.
     * @param itemType Material namespaced key of the tested item.
     * @return If there is a conflict with the enchantments.
     */
    boolean isEnchantConflict(
            Map<CAEnchantment, Integer> enchantments,
            NamespacedKey itemType
    );

    /**
     * Test if the provided item can be compatible with this enchantment. only non-Custom Anvil conflict.
     * @param enchantments Immutable map of validated enchantments for the item.
     * @param itemType Material namespaced key of the tested item.
     * @param item Provide a new instance of the used item stack with the partial enchantment applied.
     * @param original The original item. DO NOT modify it.
     * @return If there is a conflict with the enchantment and the item.
     */
    boolean isItemConflict(
            Map<CAEnchantment, Integer> enchantments,
            NamespacedKey itemType,
            ItemStack item,
            ItemStack original
    );

}
