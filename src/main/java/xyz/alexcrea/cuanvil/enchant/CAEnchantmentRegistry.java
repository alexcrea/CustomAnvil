package xyz.alexcrea.cuanvil.enchant;

import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.bulk.BukkitEnchantBulkOperation;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkCleanEnchantOperation;
import xyz.alexcrea.cuanvil.enchant.bulk.BulkGetEnchantOperation;
import xyz.alexcrea.cuanvil.enchant.wrapped.CABukkitEnchantment;

import java.util.*;
import java.util.logging.Level;

public class CAEnchantmentRegistry {

    private static final CAEnchantmentRegistry instance = new CAEnchantmentRegistry();
    public static CAEnchantmentRegistry getInstance() {
        return instance;
    }

    // Register enchantment functions
    private final HashMap<NamespacedKey, CAEnchantment> byKeyMap;
    private final HashMap<String, List<CAEnchantment>> byNameMap;

    private final SortedSet<CAEnchantment> nameSortedEnchantments;

    private final List<CAEnchantment> unoptimisedGetValues;
    private final List<CAEnchantment> unoptimisedCleanValues;

    private final List<BulkGetEnchantOperation> optimisedGetOperators;
    private final List<BulkCleanEnchantOperation> optimisedCleanOperators;

    private CAEnchantmentRegistry() {
        byKeyMap = new HashMap<>();
        byNameMap = new HashMap<>();

        nameSortedEnchantments = new TreeSet<>(Comparator.comparing(CAEnchantment::getName));

        unoptimisedGetValues = new ArrayList<>();
        unoptimisedCleanValues = new ArrayList<>();

        optimisedGetOperators = new ArrayList<>();
        optimisedCleanOperators = new ArrayList<>();
    }

    /**
     * This should only be called on main of custom anvil.
     * If called more than one time, chance of thing being broken will be high.
     */
    public void registerBukkit(){
        // Register enchantment
        for (Enchantment enchantment : Enchantment.values()) {
            register(new CABukkitEnchantment(enchantment));
        }

        // Add bukkit enchantment bulk operation
        BukkitEnchantBulkOperation bukkitOperation = new BukkitEnchantBulkOperation();
        optimisedGetOperators.add(bukkitOperation);
        optimisedCleanOperators.add(bukkitOperation);

    }

    private static boolean hasWarnedRegistering = false;

    /**
     * Can be used to register new enchantment.
     * <p>
     * No guarantee that the enchantment will be present on the config gui if registered late.
     * (By late I mean after custom anvil startup.)
     * @param enchantment The enchantment to be registered.
     * @return If the operation was successful.
     */
    public boolean register(@NotNull CAEnchantment enchantment){
        if(byKeyMap.containsKey(enchantment.getKey())){
            CustomAnvil.instance.getLogger().log(Level.WARNING,
                    "Duplicate registered enchantment. This should NOT happen any time.\n" +
                    "If you are a custom anvil developer. You maybe custom anvil detected your enchantment as a bukkit enchantment. " +
                            "maybe remove enchantment with the same key before registering yours",
                    new IllegalStateException(enchantment.getKey()+" enchantment was already registered"));
            return false;
        }

        if((!hasWarnedRegistering) && byNameMap.containsKey(enchantment.getName())){
            hasWarnedRegistering = true;

            CustomAnvil.instance.getLogger().log(Level.WARNING,
                    "Duplicate registered enchantment name. Please check that configuration is using namespace.");
        }

        byKeyMap.put(enchantment.getKey(), enchantment);

        byNameMap.putIfAbsent(enchantment.getName(), new ArrayList<>());
        byNameMap.get(enchantment.getName()).add(enchantment);

        nameSortedEnchantments.add(enchantment);

        if(!enchantment.isGetOptimised()){
            unoptimisedGetValues.add(enchantment);
        }
        if(!enchantment.isCleanOptimised()){
            unoptimisedCleanValues.add(enchantment);
        }

        return true;
    }

    /**
     * Can be used to unregister new enchantment.
     * Please be cautious with this function.
     * It should probably rarely be used.
     * <p>
     * No guarantee that the enchantment will absent if the config guis if unregistered late.
     * (By late I mean after custom anvil startup.)
     * @param enchantment The enchantment to be unregistered.
     * @return If the operation was successful.
     */

    public boolean unregister(@Nullable CAEnchantment enchantment){
        if(enchantment == null) return false;
        byKeyMap.remove(enchantment.getKey());
        byNameMap.get(enchantment.getName()).remove(enchantment);

        nameSortedEnchantments.remove(enchantment);

        unoptimisedGetValues.remove(enchantment);
        unoptimisedCleanValues.remove(enchantment);
        return true;
    }

    /**
     * Gets the enchantment by the provided key.
     * @param key Key to fetch.
     * @return Registered enchantment. null if absent.
     */
    @Nullable
    public CAEnchantment getByKey(@NotNull NamespacedKey key){
        return byKeyMap.get(key);
    }

    /**
     * Gets the enchantment by the provided name.
     * @param name Name to fetch.
     * @return Registered enchantment. null if absent.
     *
     * @deprecated use {@link #getListByName(String)}
     */
    @Deprecated(since = "1.6.3")
    @Nullable
    public CAEnchantment getByName(@NotNull String name){
        List<CAEnchantment> enchantments = getListByName(name);
        if(enchantments.isEmpty()) return null;

        return enchantments.get(0);
    }

    /**
     * Gets list of enchantment using the provided name.
     * @param name Name to fetch.
     * @return List of registered enchantment.
     */
    @NotNull
    public List<CAEnchantment> getListByName(@NotNull String name){
        return byNameMap.getOrDefault(name, Collections.emptyList());
    }

    /**
     * Gets an array of all the registered enchantments.
     * @return Array of enchantments.
     */
    @NotNull
    public Collection<CAEnchantment> values() {
        return byKeyMap.values();
    }

    /**
     * Gets a map of all the registered enchantments.
     * @return Immutable map of enchantments.
     */
    public Map<NamespacedKey, CAEnchantment> registeredEnchantments() {
        return Collections.unmodifiableMap(byKeyMap);
    }

    /**
     * Gets a list of all the unoptimised get operation enchantments.
     * @return List of unoptimised enchantments.
     */
    @NotNull
    public List<CAEnchantment> unoptimisedGetValues() {
        return unoptimisedGetValues;
    }

    /**
     * Gets a list of all the unoptimised clean operation enchantments.
     * @return List of unoptimised enchantments.
     */
    @NotNull
    public List<CAEnchantment> unoptimisedCleanValues() {
        return unoptimisedCleanValues;
    }

    /**
     * Get "clean optimised operation" for get enchantments.
     * @return Mutable "clean enchantments optimised operation" list.
     */
    public List<BulkCleanEnchantOperation> getOptimisedCleanOperators() {
        return optimisedCleanOperators;
    }

    /**
     * Get "get optimised operation" for get enchantments.
     * @return Mutable "get enchantments optimised operation" list.
     */
    public List<BulkGetEnchantOperation> getOptimisedGetOperators() {
        return optimisedGetOperators;
    }

    /**
     * Get custom anvil enchantment sorted by name.
     * @return An immutable sorted set of every registered enchantment sorted by name.
     */
    public SortedSet<CAEnchantment> getNameSortedEnchantments() {
        return Collections.unmodifiableSortedSet(nameSortedEnchantments);
    }
}
