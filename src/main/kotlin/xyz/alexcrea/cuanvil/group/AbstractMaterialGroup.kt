package xyz.alexcrea.cuanvil.group

import org.bukkit.Material
import java.util.*

abstract class AbstractMaterialGroup(private val name: String) {
    protected val includedMaterial by lazy { createDefaultSet() }

    /**
     * Get the group default set
     */
    protected abstract fun createDefaultSet(): EnumSet<Material>

    /**
     * Get if a material is allowed following the group policy
     */
    open fun contain(mat: Material): Boolean {
        return mat in getMaterials()
    }

    /**
     * Get if a group is referenced by this:
     */
    abstract fun isReferencing(other: AbstractMaterialGroup): Boolean

    /**
     * Push a material to this group to follow this group policy
     */
    abstract fun addToPolicy(mat: Material)

    /**
     * Push a group to this group to follow this group policy
     */
    abstract fun addToPolicy(other: AbstractMaterialGroup)

    /**
     * Get the group contained material as a set
     */
    abstract fun getMaterials(): EnumSet<Material>

    /**
     * Get the group non-inherited material as a set
     */
    open fun getNonGroupInheritedMaterials(): EnumSet<Material> {
        return includedMaterial
    }

    /**
     * Get the group non-inherited material as a set
     */
    open fun setNonGroupInheritedMaterials(materials: EnumSet<Material>) {
        this.includedMaterial.clear()
        this.includedMaterial.addAll(materials)
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

    open fun getRepresentativeMaterial(): Material {
        // Test inner material
        val matIterator = includedMaterial.iterator()
        while (matIterator.hasNext()) {
            val material = matIterator.next()
            if (material.isAir) continue
            return material
        }
        // Test included group representative material
        val groupIterator = getGroups().iterator()
        while (groupIterator.hasNext()) {
            val groupMat = groupIterator.next().getRepresentativeMaterial()
            if (groupMat.isAir) continue
            return groupMat
        }
        return Material.PAPER
    }

    abstract fun updateMaterials()

}