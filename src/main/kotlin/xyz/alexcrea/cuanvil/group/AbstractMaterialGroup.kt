package xyz.alexcrea.cuanvil.group

import com.google.common.collect.ImmutableSet
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
abstract class AbstractMaterialGroup(private val name: String) {
    protected val includedItems by lazy { createDefaultSet() }

    /**
     * Get the group default set
     */
    protected abstract fun createDefaultSet(): MutableSet<ItemType>

    /**
     * Get if a material is allowed following the group policy
     */
    open fun contain(mat: ItemType): Boolean {
        return mat in getItemTypes()
    }

    /**
     * Get if a group is referenced by this:
     */
    abstract fun isReferencing(other: AbstractMaterialGroup): Boolean

    /**
     * Push an item to this group to follow this group policy
     *
     * @return this instance.
     */
    abstract fun addToPolicy(type: ItemType): AbstractMaterialGroup

    /**
     * Push a list of items to this group to follow this group policy
     *
     * @return this instance.
     */
    fun addAll(vararg types: ItemType): AbstractMaterialGroup {
        for (type in types) {
            addToPolicy(type)
        }
        return this
    }

    /**
     * Push a group to this group to follow this group policy
     *
     * @return this instance.
     */
    abstract fun addToPolicy(other: AbstractMaterialGroup): AbstractMaterialGroup

    /**
     * Push a list of group to this group to follow this group policy
     *
     * @return this instance.
     */
    fun addAll(vararg otherList: AbstractMaterialGroup): AbstractMaterialGroup {
        for (group in otherList) {
            addToPolicy(group)
        }
        return this
    }

    /**
     * Get the group contained item as a set
     */
    abstract fun getItemTypes(): Set<ItemType>

    /**
     * Get the group non-inherited items as a set
     */
    open fun getNonGroupInheritedMaterials(): MutableSet<ItemType> {
        return includedItems
    }

    /**
     * Set the group non-inherited items
     */
    open fun setNonGroupInheritedMaterials(types: Set<ItemType>) {
        this.includedItems.clear()
        this.includedItems.addAll(types)
    }

    /**
     * Get the group name in case something is wrong
     */
    open fun getName(): String {
        return name
    }

    override fun toString(): String {
        return name
    }

    /**
     * Update the contained groups of this group
     */
    abstract fun setGroups(groups: MutableSet<AbstractMaterialGroup>)

    /**
     * Get the contained group of this material group
     */
    abstract fun getGroups(): MutableSet<AbstractMaterialGroup>

    open fun getRepresentativeMaterial(): ItemType {
        // Test inner material
        val itemIterator = includedItems.iterator()
        while (itemIterator.hasNext()) {
            val type = itemIterator.next()
            if (type == ItemType.AIR) continue
            return type
        }
        // Test included group representative material
        val groupIterator = getGroups().iterator()
        while (groupIterator.hasNext()) {
            val groupType = groupIterator.next().getRepresentativeMaterial()
            if (groupType == ItemType.AIR) continue
            return groupType
        }
        return ItemType.PAPER
    }

    abstract fun updateMaterials()

}