package xyz.alexcrea.cuanvil.enchant;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ItemUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.enchant.wrapped.VanillaEnchantment;

import java.util.*;
import java.util.logging.Level;

/**
 * Represent any enchantment.
 * One issue with the plugin is: it does not handle well duplicate key name (ignoring namespace) as the plugin was coded with vanilla enchantment in head
 */
public abstract class WrappedEnchantment {

    @NotNull
    private final NamespacedKey key;
    @NotNull
    private final String name;
    @NotNull
    private final EnchantmentRarity defaultRarity;
    private final int defaultMaxLevel;

    /**
     * Constructor of Wrapped Enchantment.
     * @param key The enchantment's key.
     * @param defaultRarity Default rarity the enchantment should be.
     * @param defaultMaxLevel Default max level the enchantment can be applied with.
     */
    public WrappedEnchantment(
            @NotNull NamespacedKey key,
            @Nullable EnchantmentRarity defaultRarity,
            int defaultMaxLevel){
        this.key = key;
        this.name = key.getKey();
        this.defaultMaxLevel = defaultMaxLevel;

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
    public final String getName(){
        return name;
    }

    /**
     * Get the default maximum level of this enchantment.
     * @return The default maximum level of this enchantment.
     */
    public final int defaultMaxLevel(){return defaultMaxLevel;}

    /**
     * If the enchantment have specialised group operation.
     * @return If the enchantment is optimised for group operation.
     */
    protected boolean isOptimised(){
        return false;
    }

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for.
     */
    public int getLevel(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return 0;
        return getLevel(item, meta);
    }

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
     * @return If the enchantment have been found.
     */
    public boolean isEnchantmentPresent(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return isEnchantmentPresent(item, meta);
    }

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
     * @param level The level to set the enchantment to.
     */
    public abstract void addEnchantmentUnsafe(@NotNull ItemStack item, int level);

    /**
     * Remove this enchantment from the provided ItemStack.
     * @param item The item to remove the enchantment.
     */
    public abstract void removeFrom(@NotNull ItemStack item);

    // Static functions

    /**
     * Clear every enchantment from this item.
     * @param item Item to be cleared from enchantments.
     */
    public static void clearEnchants(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;

        // Clean Vanilla enchants
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
            bookMeta.getStoredEnchants().forEach(
                    (enchantment, leve) -> bookMeta.removeStoredEnchant(enchantment)
            );
        } else {
            item.getEnchantments().forEach(
                    (enchantment, leve) -> item.removeEnchantment(enchantment)
            );
        }

        // Clean unoptimised enchants
        for (WrappedEnchantment enchant : unoptimisedValues()) {
            if(enchant.isEnchantmentPresent(item)){
                enchant.removeFrom(item);
            }
        }

    }

    /**
     * Get enchantments of an item.
     * @param item Item to get enchantment from.
     * @return A map of the set enchantments and there's respective levels.
     */
    public static Map<WrappedEnchantment, Integer> getEnchants(@NotNull ItemStack item){
        Map<WrappedEnchantment, Integer> enchantments = new HashMap<>();

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return enchantments;

        // Vanilla optimised get
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            ((EnchantmentStorageMeta)meta).getStoredEnchants().forEach(
                    (enchantment, level) -> enchantments.put(getByKey(enchantment.getKey()), level)
            );
        } else {
            item.getEnchantments().forEach(
                    (enchantment, level) -> enchantments.put(getByKey(enchantment.getKey()), level)
            );
        }

        // Unoptimised enchantment get
        findEnchantsFromSelectedList(item, meta, enchantments, unoptimisedValues());

        return enchantments;
    }


    /**
     * Find enchantments of an item. only test the enchantment from the list.
     * @param item Item to get enchantment from.
     * @param meta Meta of the provided item.
     * @param enchantments Map of enchantment to complete.
     * @param enchantmentToTest Enchantment to test
     */
    private static void findEnchantsFromSelectedList(
            @NotNull ItemStack item,
            @NotNull ItemMeta meta,
            @NotNull Map<WrappedEnchantment, Integer> enchantments,
            @NotNull Collection<WrappedEnchantment> enchantmentToTest){

        for (WrappedEnchantment enchantment : enchantmentToTest) {
            if(enchantment.isEnchantmentPresent(item, meta)){
                enchantments.put(enchantment, enchantment.getLevel(item, meta));
            }
        }

    }


    // Register enchantment functions
    private static final HashMap<NamespacedKey, WrappedEnchantment> BY_KEY = new HashMap<>();
    private static final HashMap<String, WrappedEnchantment> BY_NAME = new HashMap<>();
    private static final List<WrappedEnchantment> UNOPTIMISED_ENCHANTMENT = new ArrayList<>();

    /**
     * This should only be called on main of custom anvil.
     * If called more than one time, chance of thing being broken will be high.
     */
    public static void registerEnchantments(){
        for (Enchantment enchantment : Enchantment.values()) {
            register(new VanillaEnchantment(enchantment));
        }

        if(DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility() != null){
            DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility().registerEnchantements();
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
        if(BY_KEY.containsKey(enchantment.getKey())){
            CustomAnvil.instance.getLogger().log(Level.WARNING,
                    "Duplicate registered enchantment. This should NOT happen.",
                    new IllegalStateException(enchantment.getKey()+" enchantment was already registered"));
            return;
        }
        if(BY_NAME.containsKey(enchantment.getName())){
            CustomAnvil.instance.getLogger().log(Level.WARNING,
                    "Duplicate registered enchantment name. There will have issue. " +
                    "\nI hope this do not happen to you on a production server. If it do, there is probably a plugin trying to register an enchantment with the same name than another one",
                    new IllegalStateException(enchantment.getKey()+" enchantment name was already registered"));
        }

        BY_KEY.put(enchantment.getKey(), enchantment);
        BY_NAME.put(enchantment.getName(), enchantment);

        if(!enchantment.isOptimised()){
            UNOPTIMISED_ENCHANTMENT.add(enchantment);
        }
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
        BY_NAME.remove(enchantment.getName());
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
     * Gets the enchantment by the provided name.
     * @param name Name to fetch.
     * @return Registered enchantment. null if absent.
     */
    public static @Nullable WrappedEnchantment getByName(@NotNull String name){
        return BY_NAME.get(name);
    }

    /**
     * Gets an array of all the registered enchantments.
     * @return Array of enchantment.
     */
    @NotNull
    public static WrappedEnchantment[] values() {
        return BY_KEY.values().toArray(new WrappedEnchantment[0]);
    }

    /**
     * Gets a list of all the unoptimised enchantments.
     * @return List of enchantment.
     */
    @NotNull
    private static List<WrappedEnchantment> unoptimisedValues() {
        return UNOPTIMISED_ENCHANTMENT;
    }

}
