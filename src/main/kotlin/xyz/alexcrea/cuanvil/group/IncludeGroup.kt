package xyz.alexcrea.cuanvil.group

import org.bukkit.Material
import java.util.EnumSet

class IncludeGroup(name: String): AbstractMaterialGroup(name) {
    override fun createDefaultSet(): EnumSet<Material> {
        return EnumSet.noneOf(Material::class.java)
    }

    private val includedGroup = HashSet<AbstractMaterialGroup>()

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
    }

    override fun addToPolicy(other: AbstractMaterialGroup) {
        includedGroup.add(other)
        includedMaterial.addAll(other.getSet())
    }

}