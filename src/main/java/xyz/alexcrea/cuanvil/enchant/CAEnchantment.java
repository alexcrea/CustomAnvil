package xyz.alexcrea.cuanvil.enchant;

import io.delilaheve.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.dependency.EnchantmentSquaredDependency;
import xyz.alexcrea.cuanvil.group.ConflictType;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represent an enchantment compatible with Custom Anvil.
 * One issue with custom anvil is: it does not handle well duplicate key name (ignoring namespace) as the plugin was coded with vanilla enchantment in head
 */
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
     * Check if the enchantment have specialised group operation.
     * @return If the enchantment is optimised for group operation.
     */
    boolean isOptimised();

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
     * Test if the provided item can be compatible with this enchantment. only non-Custom Anvil conflict.
     * @param baseEnchantments Validated enchantments for the item.
     * @param itemMat Material of the tested item.
     * @param itemSupply Provide a new instance of used item stack but with baseEnchantments as enchantments.
     * @return Type of conflict this enchantment has with the provided item.
     */
    @NotNull
    ConflictType testOtherConflicts(
            @NotNull Map<CAEnchantment, Integer> baseEnchantments,
            @NotNull Material itemMat,
            @NotNull Supplier<ItemStack> itemSupply);

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for.
     */
    int getLevel(@NotNull ItemStack item);

    /**
     * Get current level of the enchantment.
     * @param item Item to search the level for.
     * @param meta Meta of the provided item. It will not be changed and not be set on the item.
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

        // Clean Enchant Squared enchants
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null){
            enchantmentSquared.clearEnchantments(item);
        }

        // Clean unoptimised enchants
        for (CAEnchantment enchant : CAEnchantmentRegistry.getInstance().unoptimisedValues()) {
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

        // Vanilla optimised get
        if (ItemUtil.INSTANCE.isEnchantedBook(item)) {
            ((EnchantmentStorageMeta)meta).getStoredEnchants().forEach(
                    (enchantment, level) -> enchantments.put(registry.getByKey(enchantment.getKey()), level)
            );
        } else {
            item.getEnchantments().forEach(
                    (enchantment, level) -> enchantments.put(registry.getByKey(enchantment.getKey()), level)
            );
        }

        // Enchants Squared get
        EnchantmentSquaredDependency enchantmentSquared = DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility();
        if(enchantmentSquared != null){
            enchantmentSquared.getEnchantmentsSquared(item, enchantments);
        }

        // Unoptimised enchantment get
        findEnchantsFromSelectedList(item, meta, enchantments, registry.unoptimisedValues());

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
     * @return Array of enchantment.
     */
    static @Nullable CAEnchantment getByKey(@NotNull NamespacedKey key){
        return CAEnchantmentRegistry.getInstance().getByKey(key);
    }

    /**
     * Gets a list of all the unoptimised enchantments.
     * @return List of enchantment.
     */
    static @Nullable CAEnchantment getByName(@NotNull String name){
        return CAEnchantmentRegistry.getInstance().getByName(name);
    }

}
