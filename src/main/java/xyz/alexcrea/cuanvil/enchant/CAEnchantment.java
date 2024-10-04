package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkCleanEnchantOperation;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkGetEnchantOperation;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent an enchantment compatible with Custom Anvil.
 * One issue with custom anvil is: it does not handle well duplicate key name (ignoring namespace)
 * as the plugin was initially coded with vanilla enchantment in head
 */
@SuppressWarnings("unused")
public interface CAEnchantment {


    /**
     * Get the default rarity of this enchant.
     * @return The default rarity of this enchant.
     */
    @NotNull
    EnchantmentRarity defaultRarity();

    /**
     * Get the enchantment key.
     * @return The enchantment key.
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Get the enchantment name.
     * @return The enchantment name.
     */
    @NotNull
    String getName();

    /**
     * Get the default maximum level of this enchantment.
     * @return The default maximum level of this enchantment.
     */
    int defaultMaxLevel();

    /**
     * Check if the enchantment have specialised get bulk operation.
     * @return If the enchantment is optimised for get bulk operation.
     */
    boolean isGetOptimised();

    /**
     * Check if the enchantment have specialised clean bulk operation.
     * @return If the enchantment is optimised for clean bulk operation.
     */
    boolean isCleanOptimised();

    /**
     * Check if the player is allowed to use this enchantment.
     * @param player The player to test.
     * @return If the player is allowed to use this enchantment.
     */
    boolean isAllowed(@NotNull HumanEntity player);

    /**
     * Add a conflict to this enchantment conflict list.
     * @param conflict The conflict to add.
     */
    void addConflict(@NotNull EnchantConflictGroup conflict);

    /**
     * Remove a conflict from the conflict list of this enchantment.
     * @param conflict The conflict to remove from this enchantment.
     */
    void removeConflict(@NotNull EnchantConflictGroup conflict);

    /**
     * Clear Custom Anvil conflicts for this enchantment.
     */
    void clearConflict();

    /**
     * Get a collection of Custom Anvil conflict containing this enchantment.
     * @return A collection of Custom Anvil conflict containing this enchantment.
     */
    @NotNull Collection<EnchantConflictGroup> getConflicts();

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for. Should not get changed.
     * @return Current leve of this enchantment on item. or 0 if absent.
     */
    default int getLevel(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return 0;

        return getLevel(item, meta);
    }

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for. Should not get changed.
     * @param meta Meta of the provided item. Should not get changed.
     * @return Current leve of this enchantment on item. or 0 if absent.
     */
    int getLevel(@NotNull ItemStack item, @NotNull ItemMeta meta);

    /**
     * Check if this enchantment is present on the provided level.
     * @param item The item to set the enchantment level.
     * @return If the enchantment have been found.
     */
    boolean isEnchantmentPresent(@NotNull ItemStack item);

    /**
     * Check if this enchantment is present on the provided level.
     * @param item The item to set the enchantment level.
     * @param meta Meta of the provided item. It will not be changed and not be set on the item.
     * @return If the enchantment have been found.
     */
    boolean isEnchantmentPresent(@NotNull ItemStack item, @NotNull ItemMeta meta);

    /**
     * Force add an enchantment at the provided level.
     * @param item The item to set the enchantment level.
     * @param level The level to set the enchantment to.
     */
    void addEnchantmentUnsafe(@NotNull ItemStack item, int level);

    /**
     * Remove this enchantment from the provided ItemStack.
     * @param item The item to remove the enchantment.
     */
    void removeFrom(@NotNull ItemStack item);

    // Static functions
    /**
     * Clear every enchantment from this item.
     * @param item Item to be cleared from enchantments.
     */
    static void clearEnchants(@NotNull ItemStack item){
        // Optimised enchantment clean using item stack
        for (BulkCleanEnchantOperation cleanOperator : CAEnchantmentRegistry.getInstance().getOptimisedCleanOperators()) {
            cleanOperator.bulkClear(item);
        }

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;

        // Optimised enchantment clean using item meta
        for (BulkCleanEnchantOperation cleanOperator : CAEnchantmentRegistry.getInstance().getOptimisedCleanOperators()) {
            cleanOperator.bulkClear(item, meta);
        }

        item.setItemMeta(meta);

        // Clean unoptimised enchants
        for (CAEnchantment enchant : CAEnchantmentRegistry.getInstance().unoptimisedCleanValues()) {
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
    static Map<CAEnchantment, Integer> getEnchants(@NotNull ItemStack item){
        Map<CAEnchantment, Integer> enchantments = new HashMap<>();
        CAEnchantmentRegistry registry = CAEnchantmentRegistry.getInstance();

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return enchantments;

        // Optimised enchantment get
        for (BulkGetEnchantOperation getOperator : CAEnchantmentRegistry.getInstance().getOptimisedGetOperators()) {
            getOperator.bulkGet(enchantments, item, meta);
        }

        // Unoptimised enchantment get
        findEnchantsFromSelectedList(item, meta, enchantments, registry.unoptimisedGetValues());

        return enchantments;
    }


    /**
     * Find enchantments of an item. only test the enchantment from the list.
     * @param item Item to get enchantment from.
     * @param meta Meta of the provided item.
     * @param enchantments Map of enchantment to complete.
     * @param enchantmentToTest Enchantment to test
     */
    static void findEnchantsFromSelectedList(
            @NotNull ItemStack item,
            @NotNull ItemMeta meta,
            @NotNull Map<CAEnchantment, Integer> enchantments,
            @NotNull Collection<CAEnchantment> enchantmentToTest){

        for (CAEnchantment enchantment : enchantmentToTest) {
            if(enchantment.isEnchantmentPresent(item, meta)){
                enchantments.put(enchantment, enchantment.getLevel(item, meta));
            }
        }

    }

    /**
     * Gets an array of all the registered enchantments.
     *
     * @param key The enchantment key
     * @return Array of enchantment.
     */
    static @Nullable CAEnchantment getByKey(@NotNull NamespacedKey key){
        return CAEnchantmentRegistry.getInstance().getByKey(key);
    }

    /**
     * Gets the enchantment by the provided name.
     * @param name Name to fetch.
     * @return Registered enchantment. null if absent.
     *
     * @deprecated use {@link #getListByName(String)}
     */
    @Deprecated(since = "1.6.3")
    static @Nullable CAEnchantment getByName(@NotNull String name){
        return CAEnchantmentRegistry.getInstance().getByName(name);
    }

    /**
     * Gets list of enchantment using the provided name.
     * @param name Name to fetch.
     * @return List of registered enchantment.
     */
    static List<CAEnchantment> getListByName(@NotNull String name){
        return CAEnchantmentRegistry.getInstance().getListByName(name);
    }

}
