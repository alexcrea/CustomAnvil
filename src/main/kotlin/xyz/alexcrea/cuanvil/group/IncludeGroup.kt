package xyz.alexcrea.cuanvil.group

import org.bukkit.Material
import java.util.*

class IncludeGroup(name: String): AbstractMaterialGroup(name) {
    override fun createDefaultSet(): EnumSet<Material> {
        return EnumSet.noneOf(Material::class.java)
    }

    private var includedGroup: MutableSet<AbstractMaterialGroup> = HashSet()
    private val groupItems by lazy {createDefaultSet()}

    override fun isReferencing(other: AbstractMaterialGroup): Boolean {
        for (materialGroup in includedGroup.iterator()) {
            if((materialGroup == other) || (materialGroup.isReferencing(other))){
                return true
            }
        }
        return false
    }

    override fun addToPolicy(mat: Material) {
        includedMaterial.add(mat)
        groupItems.add(mat)
    }

    override fun addToPolicy(other: AbstractMaterialGroup) {
        includedGroup.add(other)
        groupItems.addAll(other.getMaterials())
    }

    override fun setGroups(groups: MutableSet<AbstractMaterialGroup>) {
        groupItems.clear()
        groupItems.addAll(includedMaterial)

        includedGroup.clear()
        groups.forEach { group ->
            if(!group.isReferencing(this)){
                includedGroup.add(group)
                groupItems.addAll(group.getMaterials())
            }
        }
    }

    override fun setNonGroupInheritedMaterials(materials: EnumSet<Material>) {
        super.setNonGroupInheritedMaterials(materials)
        // Update group items
        groupItems.clear()
        groupItems.addAll(includedMaterial)

        includedGroup.forEach { group ->
            groupItems.addAll(group.getMaterials())
        }
    }

    override fun getGroups(): MutableSet<AbstractMaterialGroup> {
        return includedGroup
    }

    override fun getMaterials(): EnumSet<Material> {
        return groupItems
    }


}