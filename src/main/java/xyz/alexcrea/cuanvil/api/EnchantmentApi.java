package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkCleanEnchantOperation;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkGetEnchantOperation;
import xyz.alexcrea.cuanvil.enchant.wrapped.CABukkitEnchantment;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantCostConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantLimitConfigGui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Custom Anvil api for enchantment registry.
 */
@SuppressWarnings("unused")
public class EnchantmentApi {

    private static Object saveChangeTask = null;

    private EnchantmentApi() {}

    /**
     * Register an enchantment.
     *
     * @param enchantment The enchantment to register
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull CAEnchantment enchantment) {
        if (!CAEnchantmentRegistry.getInstance().register(enchantment)) return false;

        // Add enchantment to gui.
        if (EnchantCostConfigGui.getInstance() != null) {
            EnchantCostConfigGui.getInstance().updateValueForGeneric(enchantment, true);
        }
        if (EnchantLimitConfigGui.getInstance() != null) {
            EnchantLimitConfigGui.getInstance().updateValueForGeneric(enchantment, true);
        }

        // Write default if do not exist
        writeDefaultConfig(enchantment, false);

        return true;
    }

    /**
     * Register an enchantment by minecraft registered enchantment instance.
     *
     * @param enchantment   The enchantment to register
     * @param defaultRarity The default rarity of the provided enchantment
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull Enchantment enchantment, @Nullable EnchantmentRarity defaultRarity) {
        if (defaultRarity == null)
            return registerEnchantment(new CABukkitEnchantment(enchantment));

        return registerEnchantment(new CABukkitEnchantment(enchantment, defaultRarity));
    }

    /**
     * Register an enchantment by minecraft registered enchantment instance.
     * <p>
     * Please note that this function assume the provided enchantment is registered into minecraft registry.
     *
     * @param enchantment The enchantment to register
     * @return True if successful.
     */
    public static boolean registerEnchantment(@NotNull Enchantment enchantment) {
        return registerEnchantment(new CABukkitEnchantment(enchantment));
    }

    /**
     * Unregister an enchantment.
     *
     * @param enchantment The enchantment to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@Nullable CAEnchantment enchantment) {
        // Remove from gui
        if (EnchantCostConfigGui.getInstance() != null) {
            EnchantCostConfigGui.getInstance().removeGeneric(enchantment);
        }
        if (EnchantLimitConfigGui.getInstance() != null) {
            EnchantLimitConfigGui.getInstance().removeGeneric(enchantment);
        }

        return CAEnchantmentRegistry.getInstance().unregister(enchantment);
    }

    /**
     * Unregister an enchantment by its key.
     *
     * @param key The enchantment key to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@NotNull NamespacedKey key) {
        CAEnchantment enchantment = CAEnchantment.getByKey(key);
        return unregisterEnchantment(enchantment);
    }

    /**
     * Unregister an enchantment by his bukkit enchantment.
     *
     * @param enchantment The enchantment to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@NotNull Enchantment enchantment) {
        return unregisterEnchantment(enchantment.getKey());
    }

    /**
     * Get by key an enchantment.
     *
     * @param key The key used to fetch
     * @return The custom anvil enchantment of this key. null if not found.
     */
    @Nullable
    public static CAEnchantment getByKey(@Nullable NamespacedKey key) {
        return CAEnchantment.getByKey(key);
    }

    /**
     * Get list of enchantment using the provided name.
     *
     * @param name The name used to fetch
     * @return List of custom anvil enchantments of this name. May be empty if not found.
     */
    public static List<CAEnchantment> getByName(@NotNull String name) {
        return CAEnchantment.getByName(name);
    }

    /**
     * Get every registered custom anvil enchantments.
     *
     * @return An immutable map of enchantment key as map key and custom anvil enchantment as value.
     */
    @NotNull
    public static Map<NamespacedKey, CAEnchantment> getRegisteredEnchantments() {
        return Collections.unmodifiableMap(CAEnchantmentRegistry.getInstance().registeredEnchantments());
    }

    /**
     * Write the default level and rarity configuration of the enchantment.
     *
     * @param enchantment The enchantment to write default configuration
     * @param override    If it should override old configuration
     * @return Return false if override is false and a configuration exist. true otherwise.
     */
    public static boolean writeDefaultConfig(CAEnchantment enchantment, boolean override) {
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        if (tryWriteDefaultConfig(config, enchantment, override)) {
            prepareSaveTask();
        }
        return true;
    }

    private static boolean tryWriteDefaultConfig(FileConfiguration defaultConfig, CAEnchantment enchantment, boolean override) {
        boolean hasChange = false;

        String levelPath = ConfigOptions.ENCHANT_LIMIT_ROOT + "." + enchantment.getKey();
        if (override || !defaultConfig.isSet(levelPath)) {
            defaultConfig.set(levelPath, enchantment.defaultMaxLevel());
            hasChange = true;
        }

        String basePath = ConfigOptions.ENCHANT_VALUES_ROOT + "." + enchantment.getKey();
        EnchantmentRarity rarity = enchantment.defaultRarity();

        String itemPath = basePath + ".item";
        String bookPath = basePath + ".book";
        if (override || !defaultConfig.isSet(itemPath)) {
            defaultConfig.set(itemPath, rarity.getItemValue());
            hasChange = true;
        }
        if (override || !defaultConfig.isSet(bookPath)) {
            defaultConfig.set(bookPath, rarity.getBookValue());
            hasChange = true;
        }

        return hasChange;
    }

    /**
     * Prepare a task to save custom recipe configuration.
     */
    private static void prepareSaveTask() {
        if (saveChangeTask != null) return;

        saveChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, () -> {
            ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
            saveChangeTask = null;
        });
    }

    /**
     * Add a bulk get operator. (not needed for proper "bukkit" enchantments)
     * <p>
     * Do not forget to mark your enchantments as {@link CAEnchantment#isGetOptimised() Get Optimized}
     *
     * @param operation An optimised get enchantments operation
     */
    public static void addBulkGet(@NotNull BulkGetEnchantOperation operation) {
        CAEnchantmentRegistry.getInstance().getOptimisedGetOperators().add(operation);
    }

    /**
     * Add a bulk clean operator.
     *
     * @param operation An optimised clean enchantments operation (not needed for proper "bukkit" enchantments)
     *                  <p>
     *                  Do not forget to mark your enchantments as {@link CAEnchantment#isCleanOptimised() Clean Optimized}
     */
    public static void addBulkClean(@NotNull BulkCleanEnchantOperation operation) {
        CAEnchantmentRegistry.getInstance().getOptimisedCleanOperators().add(operation);
    }

    /**
     * Get all the enchantments of an item
     *
     * @param item The item to get the enchantment from
     * @return A map of key of enchantment, value the level of all the enchantments of the item
     * @since 1.17.6
     */
    public static Map<CAEnchantment, Integer> getEnchantments(@NotNull ItemStack item) {
        return CAEnchantment.getEnchants(item);
    }

    /**
     * Set all the enchantments to an item. Clearing previous enchantments
     *
     * @param item     The item to get the enchantment from
     * @param enchants A map of key of enchantment, value the level of all the enchantments
     * @since 1.17.6
     */
    public static void setEnchantments(@NotNull ItemStack item, @NotNull Map<CAEnchantment, Integer> enchants) {
        clearEnchantments(item);
        addEnchantments(item, enchants);
    }

    /**
     * Add enchantment with there respective level.
     * If the enchantment is already present it will be overridden to the new level
     *
     * @param item     The item to get the enchantment from
     * @param enchants A map of key of enchantment, value the level of the new enchantments
     * @since 1.17.6
     */
    public static void addEnchantments(@NotNull ItemStack item, @NotNull Map<CAEnchantment, Integer> enchants) {
        enchants.forEach((enchantment, level) ->
                enchantment.addEnchantmentUnsafe(item, level)
        );
    }

    /**
     * Clear all the enchantments
     *
     * @param item The item to clear the enchantments from
     * @since 1.17.6
     */
    public static void clearEnchantments(@NotNull ItemStack item) {
        CAEnchantment.clearEnchants(item);
    }

}
