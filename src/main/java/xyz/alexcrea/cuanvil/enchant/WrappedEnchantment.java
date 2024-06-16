package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.wrapped.VanillaEnchant;

import java.util.HashMap;

public abstract class WrappedEnchantment {

    @NotNull
    private final NamespacedKey key;
    @NotNull
    private final String name;
    @NotNull
    private final EnchantmentRarity defaultRarity;

    /**
     * Constructor of Wrapped Enchantment.
     * @param key the enchantment's key.
     * @param name the enchantment's name.
     * @param defaultRarity default rarity the enchantment should be.
     */
    public WrappedEnchantment(
            @NotNull NamespacedKey key,
            @NotNull String name,
            @Nullable EnchantmentRarity defaultRarity){
        this.key = key;
        this.name = name;

        if(defaultRarity == null) this.defaultRarity = EnchantmentRarity.COMMON;
        else this.defaultRarity = defaultRarity;
    }

    /**
     * Get the default rarity of this enchant.
     * @return the default rarity of this enchant.
     */
    public final EnchantmentRarity defaultRarity(){
        return defaultRarity;
    }

    /**
     * Get the enchantment key.
     * @return the enchantment key.
     */
    @NotNull
    public final NamespacedKey getKey(){
        return key;
    }

    /**
     * Get the enchantment name.
     * @return the enchantment name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the default maximum level of this enchantment.
     * @return the default maximum level of this enchantment.
     */
    public abstract int defaultMaxLevel();


    public abstract int enchantmentLevel(ItemStack item, ItemMeta meta);




    // Static functions
    private static HashMap<NamespacedKey, WrappedEnchantment> BY_KEY;
    //private static HashMap<NamespacedKey, WrappedEnchantment> BY_NAME; //TODO decide if I should implement it.

    /**
     * This should only be called on main of custom anvil.
     * If called more than one time, chance of thing being broken will be high.
     */
    public static void registerEnchantments(){
        BY_KEY = new HashMap<>();
        //BY_NAME = new HashMap<>();

        for (Enchantment enchantment : Enchantment.values()) {
            register(new VanillaEnchant(enchantment));
        }

    }

    /**
     * Can be used to register new enchantment.
     * <p>
     * No guarantee that the enchantment will be present on the config gui if registered late.
     * (by late I mean after custom anvil startup.)
     * @param enchantment the enchantment to register.
     */
    public static void register(@NotNull WrappedEnchantment enchantment){
        BY_KEY.put(enchantment.getKey(), enchantment);
        //BY_NAME.put(enchantment.getName(), enchantment);
    }

    /**
     * Can be used to unregister new enchantment.
     * Please be cautious with this function.
     * It should probably rarely be used.
     * <p>
     * No guarantee that the enchantment will absent if the config guis if unregistered late.
     * (by late I mean after custom anvil startup.)
     * @param enchantment the enchantment to unregister.
     */
    public static void unregister(@NotNull WrappedEnchantment enchantment){
        BY_KEY.remove(enchantment.getKey());
        //BY_NAME.remove(enchantment.getName());
    }

    /**
     * Gets the enchantment by the provided key.
     * @param key key to fetch.
     * @return registered enchantment. null if absent.
     */
    public static @Nullable WrappedEnchantment getByKey(@NotNull NamespacedKey key){
        return BY_KEY.get(key);
    }

    /**
     * Gets an array of all the registered enchantments.
     * @return array of enchantment.
     */
    @NotNull
    public static WrappedEnchantment[] values() {
        return BY_KEY.values().toArray(new WrappedEnchantment[BY_KEY.size()]);
    }

}
