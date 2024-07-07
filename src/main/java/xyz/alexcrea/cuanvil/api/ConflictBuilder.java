package xyz.alexcrea.cuanvil.api;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * A Builder for material conflict.
 */
@SuppressWarnings("unused")
public class ConflictBuilder {

    private final @NotNull Plugin source;
    private @NotNull String name;

    private final @NotNull Set<String> enchantmentNames;
    private final @NotNull Set<NamespacedKey> enchantmentKeys;

    private final @NotNull Set<String> excludedGroupNames;

    private int maxBeforeConflict;

    /**
     * Instantiates a new Conflict builder.
     *
     * @param source the source
     * @param name   the name
     */
    public ConflictBuilder(@NotNull Plugin source, @NotNull String name){
        this.source = source;
        this.name = name;

        this.enchantmentNames = new HashSet<>();
        this.enchantmentKeys = new HashSet<>();

        this.excludedGroupNames = new HashSet<>();
    }

    /**
     * Gets conflict source.
     *
     * @return the source
     */
    public @NotNull Plugin getSource() {
        return source;
    }

    /**
     * Gets conflict name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets stored enchantment names.
     *
     * @return the enchantment names
     */
    public @NotNull Set<String> getEnchantmentNames() {
        return enchantmentNames;
    }

    /**
     * Gets stored enchantment keys.
     *
     * @return the enchantment keys
     */
    public @NotNull Set<NamespacedKey> getEnchantmentKeys() {
        return enchantmentKeys;
    }

    /**
     * Gets stored group names.
     *
     * @return the group names
     */
    public @NotNull Set<String> getExcludedGroupNames() {
        return excludedGroupNames;
    }

    /**
     * Gets max before conflict.
     *
     * @return the max before conflict
     */
    public int getMaxBeforeConflict() {
        return maxBeforeConflict;
    }

    /**
     * Sets name.
     *
     * @param name the name
     * @return the name
     */
    public ConflictBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets max before conflict.
     *
     * @param maxBeforeConflict the max before conflict
     * @return the max before conflict
     */
    public ConflictBuilder setMaxBeforeConflict(int maxBeforeConflict) {
        this.maxBeforeConflict = maxBeforeConflict;
        return this;
    }

    /**
     * Add enchantment by name.
     *
     * @param enchantmentName the enchantment name
     * @return this conflict builder instance
     */
    public ConflictBuilder addEnchantment(@NotNull String enchantmentName){
        enchantmentNames.add(enchantmentName);
        return this;
    }

    /**
     * Add enchantment by key.
     *
     * @param enchantmentKey the enchantment key
     * @return this conflict builder instance
     */
    public ConflictBuilder addEnchantment(@NotNull NamespacedKey enchantmentKey){
        enchantmentKeys.add(enchantmentKey);
        return this;
    }

    /**
     * Add enchantment by instance.
     *
     * @param enchantment the enchantment
     * @return this conflict builder instance
     */
    public ConflictBuilder addEnchantment(@NotNull CAEnchantment enchantment){
        addEnchantment(enchantment.getKey());
        return this;
    }

    /**
     * Remove enchantment by name.
     *
     * @param enchantmentName the enchantment name
     * @return this conflict builder instance
     */
    public ConflictBuilder removeEnchantment(@NotNull String enchantmentName){
        enchantmentNames.remove(enchantmentName);
        return this;
    }

    /**
     * Remove enchantment by key.
     *
     * @param enchantmentKey the enchantment key
     * @return this conflict builder instance
     */
    public ConflictBuilder removeEnchantment(@NotNull NamespacedKey enchantmentKey){
        enchantmentKeys.remove(enchantmentKey);
        return removeEnchantment(enchantmentKey.getKey());
    }

    /**
     * Remove enchantment by instance.
     *
     * @param enchantment the enchantment
     * @return this conflict builder instance
     */
    public ConflictBuilder removeEnchantment(@NotNull CAEnchantment enchantment){
        return removeEnchantment(enchantment.getKey());
    }

    /**
     * Add group by name.
     *
     * @param groupName the group name
     * @return this conflict builder instance
     */
    public ConflictBuilder addExcludedGroup(@NotNull String groupName){
        excludedGroupNames.add(groupName);
        return this;
    }

    /**
     * Add group by instance.
     *
     * @param group the group
     * @return this conflict builder instance
     */
    public ConflictBuilder addExcludedGroup(@NotNull AbstractMaterialGroup group){
        return addExcludedGroup(group.getName());
    }

    /**
     * Remove group by name.
     *
     * @param groupName the group name
     * @return this conflict builder instance
     */
    public ConflictBuilder removeGroup(@NotNull String groupName){
        excludedGroupNames.remove(groupName);
        return this;
    }

    /**
     * Remove group by instance.
     *
     * @param group the group
     * @return this conflict builder instance
     */
    public ConflictBuilder removeGroup(@NotNull AbstractMaterialGroup group){
        return removeGroup(group.getName());
    }

    /**
     * Copy this conflict builder.
     *
     * @return a copy of this conflict builder
     */
    public ConflictBuilder copy() {
        ConflictBuilder clone = new ConflictBuilder(this.source, this.name);

        setMaxBeforeConflict(this.maxBeforeConflict);

        // Set Enchantments
        for (NamespacedKey key : this.enchantmentKeys) {
            clone.addEnchantment(key);
        }
        for (String name : this.enchantmentNames) {
            clone.addEnchantment(name);
        }

        // Set Groups
        for (String name : this.excludedGroupNames) {
            clone.addExcludedGroup(name);
        }

        return clone;
    }

}
