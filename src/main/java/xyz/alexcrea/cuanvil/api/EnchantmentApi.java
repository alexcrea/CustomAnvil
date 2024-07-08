package xyz.alexcrea.cuanvil.api;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.wrapped.CAVanillaEnchantment;

import java.util.Collections;
import java.util.Map;

/**
 * Custom Anvil api for enchantment registry.
 */
@SuppressWarnings("unused")
public class EnchantmentApi {

    private EnchantmentApi() {}

    /**
     * Register an enchantment.
     *
     * @param enchantment The enchantment to register
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull CAEnchantment enchantment){
        return CAEnchantmentRegistry.getInstance().register(enchantment);
    }

    /**
     * Register an enchantment by minecraft registered enchantment instance.
     *
     * @param enchantment   The enchantment to register
     * @param defaultRarity The default rarity of the provided enchantment
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull Enchantment enchantment, @Nullable EnchantmentRarity defaultRarity){
        if(defaultRarity == null)
            return registerEnchantment(new CAVanillaEnchantment(enchantment));

        return registerEnchantment(new CAVanillaEnchantment(enchantment, defaultRarity));
    }

    /**
     * Register an enchantment by minecraft registered enchantment instance.
     * <p>
     * Please note that this function assume the provided enchantment is registered into minecraft registry.
     *
     * @param enchantment The enchantment to register
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull Enchantment enchantment){
        return registerEnchantment(new CAVanillaEnchantment(enchantment));
    }

    /**
     * Unregister an enchantment by its key.
     *
     * @param key The enchantment key to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@NotNull NamespacedKey key){
        CAEnchantment enchantment = CAEnchantmentRegistry.getInstance().getByKey(key);
        return CAEnchantmentRegistry.getInstance().unregister(enchantment);
    }

    /**
     * Unregister an enchantment.
     *
     * @param enchantment The enchantment to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@NotNull CAEnchantment enchantment){
        return CAEnchantmentRegistry.getInstance().unregister(enchantment);
    }

    /**
     * Unregister an enchantment by his bukkit enchantment.
     *
     * @param enchantment The enchantment to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@NotNull Enchantment enchantment){
        return unregisterEnchantment(enchantment.getKey());
    }

    /**
     * Get by key an enchantment.
     *
     * @param key The key used to fetch
     * @return The custom anvil enchantment of this key. null if not found.
     */
    @Nullable
    public static CAEnchantment getByKey(@NotNull NamespacedKey key){
        return CAEnchantmentRegistry.getInstance().getByKey(key);
    }

    /**
     * Get by name an enchantment.
     *
     * @param name The name used to fetch
     * @return The custom anvil enchantment of this name. null if not found.
     */
    @Nullable
    public static CAEnchantment getByName(@NotNull String name){
        return CAEnchantmentRegistry.getInstance().getByName(name);
    }

    /**
     * Get every registered custom anvil enchantments.
     * @return An immutable map of enchantment key as map key and custom anvil enchantment as value.
     */
    @NotNull
    public Map<NamespacedKey, CAEnchantment> getRegisteredEnchantments(){
        return Collections.unmodifiableMap(CAEnchantmentRegistry.getInstance().registeredEnchantments());
    }

}
