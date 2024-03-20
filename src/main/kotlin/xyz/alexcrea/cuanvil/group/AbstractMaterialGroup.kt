package xyz.alexcrea.cuanvil.group

import org.bukkit.Material
import java.util.*

abstract class AbstractMaterialGroup(private val name: String) {
    protected val includedMaterial by lazy {createDefaultSet()}
    protected var groupChangeNotified = false

    /**
     * Get the group default set
     */
    protected abstract fun createDefaultSet(): EnumSet<Material>

    /**
     * Get if a material is allowed following the group policy
     */
    fun contain(mat : Material): Boolean {
        return mat in includedMaterial
    }

    /**
     * Get if a group is referenced by this:
     */
    abstract fun isReferencing(other : AbstractMaterialGroup): Boolean

    /**
     * Push a material to this group to follow this group policy
     */
    abstract fun addToPolicy(mat : Material)

    /**
     * Push a group to this group to follow this group policy
     */
    abstract fun addToPolicy(other : AbstractMaterialGroup)

    /**
     * Get the group as a set
     */
    abstract fun getMaterials(): MutableSet<Material>

    /**
     * Get the group name in case something is wrong
     */
    fun getName(): String {
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

}