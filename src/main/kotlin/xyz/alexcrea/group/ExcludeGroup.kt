package xyz.alexcrea.group

import org.bukkit.Material
import java.util.*
import kotlin.collections.HashSet

class ExcludeGroup(name: String): AbstractMaterialGroup(name) {
    override fun createDefaultSet(): EnumSet<Material> {
        return EnumSet.allOf(Material::class.java)
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
        includedMaterial.remove(mat)
    }

    override fun addToPolicy(other: AbstractMaterialGroup) {
        includedGroup.add(other)
        includedMaterial.removeAll(other.getSet())
    }


}