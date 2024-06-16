package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.wrapped.VanillaEnchant;

import java.util.HashMap;
import java.util.Map;

public abstract class WrappedEnchantment {

    @NotNull
    private final NamespacedKey key;
    @NotNull
    private final String name;
    @NotNull
    private final EnchantmentRarity defaultRarity;

    /**
     * Constructor of Wrapped Enchantment.
     * @param key The enchantment's key.
     * @param name The enchantment's name.
     * @param defaultRarity Default rarity the enchantment should be.
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
     * @return The default rarity of this enchant.
     */
    public final EnchantmentRarity defaultRarity(){
        return defaultRarity;
    }

    /**
     * Get the enchantment key.
     * @return The enchantment key.
     */
    @NotNull
    public final NamespacedKey getKey(){
        return key;
    }

    /**
     * Get the enchantment name.
     * @return The enchantment name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the default maximum level of this enchantment.
     * @return The default maximum level of this enchantment.
     */
    public abstract int defaultMaxLevel();

    // TODO maybe methods that do not require itemmeta ?

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for.
     * @param meta Meta of the provided item. It will not be changed and not be set on the item.
     * @return Current leve of this enchantment on item. or 0 if absent.
     */
    public abstract int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta);

    /**
     * Check if this enchantment is present on the provided level.
     * @param item The item to set the enchantment level.
     * @param meta Meta of the provided item. It will not be changed and not be set on the item.
     * @return If the enchantment have been found.
     */
    public abstract boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta);

    /**
     * Force add an enchantment at the provided level.
     * @param item The item to set the enchantment level.
     * @param meta Meta of the provided item. It can be changed, but will not be set on the item.
     * @param level The level to set the enchantment to.
     */
    public abstract void addEnchantmentUnsafe(@NotNull ItemStack item, @NotNull ItemMeta meta, int level);

    /**
     * Remove this enchantment from the provided ItemStack.
     * @param item The item to remove the enchantment.
     * @param meta Meta of the provided item. It can be changed, but will not be set on the item.
     */
    public abstract void removeFrom(@NotNull ItemStack item, @NotNull ItemMeta meta);

    // Static functions

    /**
     * Clear every enchantment from this item.
     * @param item Item to be cleared from enchantments.
     */
    public static void clearEnchants(@NotNull ItemStack item){ //TODO faster method to clear vanilla enchantment
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;

        for (WrappedEnchantment enchant : getEnchants(item).keySet()) {
            enchant.removeFrom(item, meta);
        }
        item.setItemMeta(meta);
    }

    /**
     * Get enchantments of an item.
     * @param item item to get enchantment from.
     * @return a map of the set enchantments and there's respective levels.
     */
    public static Map<WrappedEnchantment, Integer> getEnchants(@NotNull ItemStack item){ //TODO faster method to find vanilla enchantment
        Map<WrappedEnchantment, Integer> enchantments = new HashMap<>();

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return enchantments;

        for (WrappedEnchantment enchantment : WrappedEnchantment.values()) {
            if(enchantment.isEnchantmentPresent(item, meta)){
                enchantments.put(enchantment, enchantment.getLevel(item, meta));
            }
        }

        return enchantments;
    }

    // Register enchantment functions
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
     * (By late I mean after custom anvil startup.)
     * @param enchantment The enchantment to be registered.
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
     * (By late I mean after custom anvil startup.)
     * @param enchantment The enchantment to be unregistered.
     */
    public static void unregister(@NotNull WrappedEnchantment enchantment){
        BY_KEY.remove(enchantment.getKey());
        //BY_NAME.remove(enchantment.getName());
    }

    /**
     * Gets the enchantment by the provided key.
     * @param key Key to fetch.
     * @return Registered enchantment. null if absent.
     */
    public static @Nullable WrappedEnchantment getByKey(@NotNull NamespacedKey key){
        return BY_KEY.get(key);
    }

    /**
     * Gets an array of all the registered enchantments.
     * @return Array of enchantment.
     */
    @NotNull
    public static WrappedEnchantment[] values() {
        return BY_KEY.values().toArray(new WrappedEnchantment[0]);
    }

}
