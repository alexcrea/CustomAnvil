package xyz.alexcrea.cuanvil.api;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * A Builder for material conflict.
 */
@SuppressWarnings("unused")
public class ConflictBuilder {

    private final @Nullable Plugin source;
    private @NotNull String name;

    private final @NotNull Set<String> enchantmentNames;
    private final @NotNull Set<NamespacedKey> enchantmentKeys;

    private final @NotNull Set<String> excludedGroupNames;

    private int maxBeforeConflict;

    /**
     * Instantiates a new Conflict builder.
     *
     * @param name              The conflict name
     * @param maxBeforeConflict Maximum number of conflicting enchantment before conflict is active
     * @param source            The conflict source
     */
    public ConflictBuilder(@NotNull String name, int maxBeforeConflict, @Nullable Plugin source){
        this.source = source;
        this.name = name;

        this.enchantmentNames = new HashSet<>();
        this.enchantmentKeys = new HashSet<>();

        this.excludedGroupNames = new HashSet<>();

        this.maxBeforeConflict = maxBeforeConflict;
    }

    /**
     * Instantiates a new Conflict builder.
     *
     * @param name   The conflict name
     * @param source The conflict source
     */
    public ConflictBuilder(@NotNull String name, @Nullable Plugin source){
        this(name, 0, source);
    }

    /**
     * Instantiates a new Conflict builder.
     *
     * @param name The conflict name
     */
    public ConflictBuilder(@NotNull String name){
        this(name, null);
    }

    /**
     * Gets conflict source.
     *
     * @return The conflict source.
     */
    @Nullable
    public Plugin getSource() {
        return source;
    }

    /**
     * Gets conflict source name.
     *
     * @return The conflict source name.
     */
    @NotNull
    public String getSourceName() {
        if(source == null) return "an unknown source";

        return source.getName();
    }

    /**
     * Gets conflict name.
     *
     * @return The conflict name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets stored conflicting enchantment names.
     *
     * @return The stored enchantment names.
     */
    @NotNull
    public Set<String> getEnchantmentNames() {
        return enchantmentNames;
    }

    /**
     * Gets stored conflicting enchantment keys.
     *
     * @return The stored enchantment keys.
     */
    @NotNull
    public Set<NamespacedKey> getEnchantmentKeys() {
        return enchantmentKeys;
    }

    /**
     * Gets stored excluded group names.
     *
     * @return The stored group names.
     */
    @NotNull
    public Set<String> getExcludedGroupNames() {
        return excludedGroupNames;
    }

    /**
     * Gets maximum number of conflicting enchantment before conflict is active.
     * <p>
     * This value represent how many enchantment contained on this conflict can be applied to before conflict is considered active.
     * That mean new enchantment will not be able to be added to the item and present enchantment will not have its level upgraded.
     * <p>
     * In vanilla. material restriction have this value set to 0 and enchantment conflict set to 1.
     *
     * @return the max number of conflicting enchantment before conflict. 0 by default.
     */
    public int getMaxBeforeConflict() {
        return maxBeforeConflict;
    }

    /**
     * Sets conflict name.
     *
     * @param name The name
     * @return This conflict builder instance.
     */
    public ConflictBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets maximum number of conflicting enchantment before conflict is active.
     * <p>
     * This value represent how many enchantment contained on this conflict can be applied to before conflict is considered active.
     * That mean new enchantment will not be able to be added to the item and present enchantment will not have its level upgraded.
     * <p>
     * In vanilla. material restriction have this value set to 0 and enchantment conflict set to 1.
     *
     * @param maxBeforeConflict The max before conflict
     * @return This conflict builder instance.
     */
    public ConflictBuilder setMaxBeforeConflict(int maxBeforeConflict) {
        this.maxBeforeConflict = maxBeforeConflict;
        return this;
    }

    /**
     * Add a conflicting enchantment by name.
     *
     * @param enchantmentName The enchantment name
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder addEnchantment(@NotNull String enchantmentName){
        enchantmentNames.add(enchantmentName);
        return this;
    }

    /**
     * Add a conflicting enchantment by key.
     *
     * @param enchantmentKey The enchantment key
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder addEnchantment(@NotNull NamespacedKey enchantmentKey){
        enchantmentKeys.add(enchantmentKey);
        return this;
    }

    /**
     * Add a conflicting enchantment by instance.
     *
     * @param enchantment The enchantment
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder addEnchantment(@NotNull CAEnchantment enchantment){
        addEnchantment(enchantment.getKey());
        return this;
    }

    /**
     * Remove conflicting enchantment by name.
     *
     * @param enchantmentName The enchantment name
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder removeEnchantment(@NotNull String enchantmentName){
        enchantmentNames.remove(enchantmentName);
        return this;
    }

    /**
     * Remove conflicting enchantment by key.
     *
     * @param enchantmentKey The enchantment key
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder removeEnchantment(@NotNull NamespacedKey enchantmentKey){
        enchantmentKeys.remove(enchantmentKey);
        return removeEnchantment(enchantmentKey.getKey());
    }

    /**
     * Remove enchantment by instance.
     *
     * @param enchantment The enchantment
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder removeEnchantment(@NotNull CAEnchantment enchantment){
        return removeEnchantment(enchantment.getKey());
    }

    /**
     * Add an excluded group by name.
     * <p>
     * If left item of an anvil craft is included on one of the excluded group it will ignore this conflict.
     * <p>
     * This allows to create conflict only for some item. Material restriction can be written like that.
     * <p>
     * For example: If we exclude a material group containing every pickaxe and add efficiency enchantment
     * with {@link #setMaxBeforeConflict(int) maxBeforeConflict} set to 0.
     * Then only pickaxe will be able to have efficiency.
     *
     * @param groupName The group name
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder addExcludedGroup(@NotNull String groupName){
        excludedGroupNames.add(groupName);
        return this;
    }

    /**
     * Add an excluded group by instance.
     * <p>
     * If left item of an anvil craft is included on one of the excluded group it will ignore this conflict.
     * <p>
     * This allows to create conflict only for some item. Material restriction can be written like that.
     * <p>
     * For example: If we exclude a material group containing every pickaxe and add efficiency enchantment
     * with {@link #setMaxBeforeConflict(int) maxBeforeConflict} set to 0.
     * Then only pickaxe will be able to have efficiency.
     *
     * @param group The group
     * @return this conflict builder instance.
     */
    @NotNull
    public ConflictBuilder addExcludedGroup(@NotNull AbstractMaterialGroup group){
        return addExcludedGroup(group.getName());
    }

    /**
     * Remove an excluded group by name.
     * <p>
     * If left item of an anvil craft is included on one of the excluded group it will ignore this conflict.
     * <p>
     * This allows to create conflict only for some item. Material restriction can be written like that.
     * <p>
     * For example: If we exclude a material group containing every pickaxe and add efficiency enchantment
     * with {@link #setMaxBeforeConflict(int) maxBeforeConflict} set to 0.
     * Then only pickaxe will be able to have efficiency.
     *
     * @param groupName The group name
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder removeExcludedGroup(@NotNull String groupName){
        excludedGroupNames.remove(groupName);
        return this;
    }

    /**
     * Remove an excluded group by instance.
     * <p>
     * If left item of an anvil craft is included on one of the excluded group it will ignore this conflict.
     * <p>
     * This allows to create conflict only for some item. Material restriction can be written like that.
     * <p>
     * For example: If we exclude a material group containing every pickaxe and add efficiency enchantment
     * with {@link #setMaxBeforeConflict(int) maxBeforeConflict} set to 0.
     * Then only pickaxe will be able to have efficiency.
     *
     * @param group The group
     * @return This conflict builder instance.
     */
    @NotNull
    public ConflictBuilder removeExcludedGroup(@NotNull AbstractMaterialGroup group){
        return removeExcludedGroup(group.getName());
    }

    /**
     * Copy this conflict builder.
     *
     * @return A copy of this conflict builder.
     */
    @NotNull
    public ConflictBuilder copy() {
        ConflictBuilder clone = new ConflictBuilder(this.name, this.source);

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

    /**
     * Register this conflict.
     * Equivalent to {@link ConflictAPI#addConflict(ConflictBuilder)}
     * @return True if successful.
     */
    public boolean registerIfAbsent(){
        return ConflictAPI.addConflict(this);
    }

}
