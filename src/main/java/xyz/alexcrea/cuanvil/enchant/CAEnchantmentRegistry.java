package xyz.alexcrea.cuanvil.enchant;

import io.delilaheve.CustomAnvil;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.enchant.wrapped.CAVanillaEnchantment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class CAEnchantmentRegistry {

    private static final CAEnchantmentRegistry instance = new CAEnchantmentRegistry();
    public static CAEnchantmentRegistry getInstance() {
        return instance;
    }

    // Register enchantment functions
    private final HashMap<NamespacedKey, CAEnchantment> byKeyMap;
    private final HashMap<String, CAEnchantment> byNameMap;
    private final List<CAEnchantment> unoptimisedValues;

    private CAEnchantmentRegistry() {
        byKeyMap = new HashMap<>();
        byNameMap = new HashMap<>();
        unoptimisedValues = new ArrayList<>();
    }

    /**
     * This should only be called on main of custom anvil.
     * If called more than one time, chance of thing being broken will be high.
     */
    public void registerStartupEnchantments(){
        for (Enchantment enchantment : Enchantment.values()) {
            register(new CAVanillaEnchantment(enchantment));
        }

        if(DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility() != null){
            DependencyManager.INSTANCE.getEnchantmentSquaredCompatibility().registerEnchantments();
        }
        if(DependencyManager.INSTANCE.getEcoEnchantCompatibility() != null){
            DependencyManager.INSTANCE.getEcoEnchantCompatibility().registerEnchantments();
        }

    }

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
                    "Duplicate registered enchantment. This should NOT happen.",
                    new IllegalStateException(enchantment.getKey()+" enchantment was already registered"));
            return false;
        }
        if(byNameMap.containsKey(enchantment.getName())){
            CustomAnvil.instance.getLogger().log(Level.WARNING,
                    "Duplicate registered enchantment name. There will have issue. " +
                            "\nI hope this do not happen to you on a production server. If it do, there is probably a plugin trying to register an enchantment with the same name than another one",
                    new IllegalStateException(enchantment.getKey()+" enchantment name was already registered"));
        }

        byKeyMap.put(enchantment.getKey(), enchantment);
        byNameMap.put(enchantment.getName(), enchantment);

        if(!enchantment.isOptimised()){
            unoptimisedValues.add(enchantment);
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

    public boolean unregister(CAEnchantment enchantment){
        if(enchantment == null) return false;
        byKeyMap.remove(enchantment.getKey());
        byNameMap.remove(enchantment.getName());

        unoptimisedValues.remove(enchantment);
        return true;
    }

    /**
     * Gets the enchantment by the provided key.
     * @param key Key to fetch.
     * @return Registered enchantment. null if absent.
     */
    public @Nullable CAEnchantment getByKey(@NotNull NamespacedKey key){
        return byKeyMap.get(key);
    }

    /**
     * Gets the enchantment by the provided name.
     * @param name Name to fetch.
     * @return Registered enchantment. null if absent.
     */
    public @Nullable CAEnchantment getByName(@NotNull String name){
        return byNameMap.get(name);
    }

    /**
     * Gets an array of all the registered enchantments.
     * @return Array of enchantment.
     */
    @NotNull
    public Collection<CAEnchantment> values() {
        return byKeyMap.values();
    }

    /**
     * Gets a list of all the unoptimised enchantments.
     * @return List of enchantment.
     */
    @NotNull
    public List<CAEnchantment> unoptimisedValues() {
        return unoptimisedValues;
    }

}
