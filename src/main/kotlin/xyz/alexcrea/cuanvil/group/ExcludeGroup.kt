package xyz.alexcrea.cuanvil.group

import org.bukkit.NamespacedKey
import java.util.*

class ExcludeGroup(name: String) : AbstractMaterialGroup(name) {

    override fun createDefaultSet(): MutableSet<NamespacedKey> {
        return NegativeSet(HashSet())
    }

    private var includedGroup: MutableSet<AbstractMaterialGroup> = HashSet()
    private val groupItems by lazy { createDefaultSet() }

    override fun isReferencing(other: AbstractMaterialGroup): Boolean {
        for (materialGroup in includedGroup.iterator()) {
            if ((materialGroup == other) || (materialGroup.isReferencing(other))) {
                return true
            }
        }
        return false
    }

    override fun addToPolicy(type: NamespacedKey): ExcludeGroup {
        includedMaterial.remove(type)
        groupItems.remove(type)

        return this
    }

    override fun addToPolicy(other: AbstractMaterialGroup): ExcludeGroup {
        includedGroup.add(other)
        groupItems.removeAll(other.getMaterials())

        return this
    }

    override fun setGroups(groups: MutableSet<AbstractMaterialGroup>) {
        groupItems.clear()
        groupItems.addAll(includedMaterial)

        includedGroup.clear()
        groups.forEach { group ->
            if (!group.isReferencing(this)) {
                includedGroup.add(group)
                groupItems.removeAll(group.getMaterials())
            }
        }
    }

    override fun getGroups(): MutableSet<AbstractMaterialGroup> {
        return includedGroup
    }

    override fun updateMaterials() {
        groupItems.clear()
        groupItems.addAll(includedMaterial)

        includedGroup.forEach { group ->
            groupItems.addAll(group.getMaterials())
        }
    }

    override fun getMaterials(): MutableSet<NamespacedKey> {
        return groupItems
    }


}