package xyz.alexcrea.cuanvil.api;

import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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
    public static boolean registerEnchantment(@NotNull CAEnchantment enchantment){
        if(!CAEnchantmentRegistry.getInstance().register(enchantment)) return false;

        // Add enchantment to gui.
        if(EnchantCostConfigGui.getInstance() != null){
            EnchantCostConfigGui.getInstance().updateValueForGeneric(enchantment, true);
        }
        if(EnchantLimitConfigGui.getInstance() != null){
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
    public static boolean registerEnchantment(@NotNull Enchantment enchantment, @Nullable EnchantmentRarity defaultRarity){
        if(defaultRarity == null)
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
    public static boolean registerEnchantment(@NotNull Enchantment enchantment){
        return registerEnchantment(new CABukkitEnchantment(enchantment));
    }

    /**
     * Unregister an enchantment.
     *
     * @param enchantment The enchantment to unregister
     * @return True if successful.
     */
    public static boolean unregisterEnchantment(@Nullable CAEnchantment enchantment){
        // Remove from gui
        if(EnchantCostConfigGui.getInstance() != null){
            EnchantCostConfigGui.getInstance().removeGeneric(enchantment);
        }
        if(EnchantLimitConfigGui.getInstance() != null){
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
    public static boolean unregisterEnchantment(@NotNull NamespacedKey key){
        CAEnchantment enchantment = CAEnchantment.getByKey(key);
        return unregisterEnchantment(enchantment);
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
        return CAEnchantment.getByKey(key);
    }

    /**
     * Get by name an enchantment.
     *
     * @param name The name used to fetch
     * @return The custom anvil enchantment of this name. null if not found.
     * @deprecated use {@link #getListByName(String)}
     */
    @Deprecated(since = "1.6.3")
    @Nullable
    public static CAEnchantment getByName(@NotNull String name){
        return CAEnchantment.getByName(name);
    }

    /**
     * Get list of enchantment using the provided name.
     *
     * @param name The name used to fetch
     * @return List of custom anvil enchantments of this name. May be empty if not found.
     */
    public static List<CAEnchantment> getListByName(@NotNull String name){
        return CAEnchantment.getListByName(name);
    }

    /**
     * Get every registered custom anvil enchantments.
     * @return An immutable map of enchantment key as map key and custom anvil enchantment as value.
     */
    @NotNull
    public static Map<NamespacedKey, CAEnchantment> getRegisteredEnchantments(){
        return Collections.unmodifiableMap(CAEnchantmentRegistry.getInstance().registeredEnchantments());
    }

    /**
     * Write the default level and rarity configuration of the enchantment.
     * @param enchantment The enchantment to write default configuration
     * @param override If it should override old configuration
     * @return Return false if override is false and a configuration exist. true otherwise.
     */
    public static boolean writeDefaultConfig(CAEnchantment enchantment, boolean override){
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.acquiredWrite();
        if(!override && config.contains(enchantment.getName())) return false;

        writeDefaultConfig(config, enchantment);

        prepareSaveTask();
        ConfigHolder.DEFAULT_CONFIG.releaseWrite();
        return true;
    }


    private static void writeDefaultConfig(FileConfiguration defaultConfig, CAEnchantment enchantment) {
        defaultConfig.set("enchant_limits." + enchantment.getKey(), enchantment.defaultMaxLevel());

        String basePath = "enchant_values." + enchantment.getKey();
        EnchantmentRarity rarity = enchantment.defaultRarity();

        defaultConfig.set(basePath + ".item", rarity.getItemValue());
        defaultConfig.set(basePath + ".book", rarity.getBookValue());
    }

    /**
     * Prepare a task to save custom recipe configuration.
     */
    private static void prepareSaveTask() {
        if(saveChangeTask != null) return;

        saveChangeTask = DependencyManager.scheduler.scheduleGlobally(CustomAnvil.instance, ()->{
            ConfigHolder.DEFAULT_CONFIG.saveToDisk(true);
            saveChangeTask = null;
        });
    }

    /**
     * Add a bulk get operator.
     * @param operation An optimised get enchantments operation
     */
    public static void addBulkGet(@NotNull BulkGetEnchantOperation operation){
        CAEnchantmentRegistry.getInstance().getOptimisedGetOperators().add(operation);
    }

    /**
     * Add a bulk clean operator.
     * @param operation An optimised clean enchantments operation
     */
    public static void addBulkClean(@NotNull BulkCleanEnchantOperation operation){
        CAEnchantmentRegistry.getInstance().getOptimisedCleanOperators().add(operation);
    }

}
