package xyz.alexcrea.cuanvil.api;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.enchant.wrapped.CAVanillaEnchantment;

/**
 * Custom Anvil api for enchantment registry.
 */
@SuppressWarnings("unused")
public class EnchantmentApi {

    private EnchantmentApi() {}

    /**
     * Register an enchantment.
     *
     * @param enchantment the enchantment to register
     * @return true if successful
     */
    public static boolean registerEnchantment(@NotNull CAEnchantment enchantment){
        return CAEnchantmentRegistry.getInstance().register(enchantment);
    }

    /**
     * Register an enchantment by minecraft registered enchantment.
     *
     * @param enchantment the enchantment to register
     * @return true if successful
     */
    public static boolean registerEnchantment(@NotNull Enchantment enchantment){
        return registerEnchantment(new CAVanillaEnchantment(enchantment));
    }

    /**
     * Unregister an enchantment by its key.
     *
     * @param key the enchantment key to unregister
     * @return true if successful
     */
    public static boolean unregisterEnchantment(@NotNull NamespacedKey key){
        CAEnchantment enchantment = CAEnchantmentRegistry.getInstance().getByKey(key);
        return CAEnchantmentRegistry.getInstance().unregister(enchantment);
    }

    /**
     * Unregister an enchantment.
     *
     * @param enchantment the enchantment to unregister
     * @return true if successful
     */
    public static boolean unregisterEnchantment(@NotNull CAEnchantment enchantment){
        return CAEnchantmentRegistry.getInstance().unregister(enchantment);
    }

    /**
     * Unregister an enchantment by his bukkit enchantment.
     *
     * @param enchantment the enchantment to unregister
     * @return true if successful
     */
    public static boolean unregisterEnchantment(@NotNull Enchantment enchantment){
        return unregisterEnchantment(enchantment.getKey());
    }

    /**
     * Get by key the enchantment.
     *
     * @param key the key used to fetch
     * @return the custom anvil enchantment
     */
    @Nullable
    public static CAEnchantment getByKey(@NotNull NamespacedKey key){
        return CAEnchantmentRegistry.getInstance().getByKey(key);
    }

    /**
     * Get by name the enchantment.
     *
     * @param name the name used to fetch
     * @return the custom anvil enchantment
     */
    @Nullable
    public static CAEnchantment getByName(@NotNull String name){
        return CAEnchantmentRegistry.getInstance().getByName(name);
    }

}
