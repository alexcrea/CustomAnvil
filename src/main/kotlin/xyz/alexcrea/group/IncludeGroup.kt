package xyz.alexcrea.group

import org.bukkit.Material
import java.util.EnumSet

open class IncludeGroup(private val name: String): MaterialGroup{
    private val includedMaterial = EnumSet.noneOf(Material::class.java)
    private val includedGroup = HashSet<MaterialGroup>()

    override fun contain(mat: Material): Boolean {
        if(mat in includedMaterial){
            return true
        }

        for (materialGroup in includedGroup.iterator()) {
            if(materialGroup.contain(mat)){
                return true
            }
        }
        return false
    }

    override fun isReferencing(other: MaterialGroup): Boolean {
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

    override fun addToPolicy(other: MaterialGroup) {
        includedGroup.add(other)
    }

    override fun getName(): String {
        return name
    }

}