package xyz.alexcrea.cuanvil.group

import org.bukkit.inventory.ItemType
import java.util.*

@Suppress("UnstableApiUsage")
class IncludeGroup(name: String) : AbstractMaterialGroup(name) {
    override fun createDefaultSet(): MutableSet<ItemType> {
        return HashSet()
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

    override fun addToPolicy(type: ItemType): IncludeGroup {
        includedItems.add(type)
        groupItems.add(type)

        return this
    }

    override fun addToPolicy(other: AbstractMaterialGroup): IncludeGroup {
        includedGroup.add(other)
        groupItems.addAll(other.getItemTypes())

        return this
    }

    override fun setGroups(groups: MutableSet<AbstractMaterialGroup>) {
        groupItems.clear()
        groupItems.addAll(includedItems)

        includedGroup.clear()
        groups.forEach { group ->
            if (!group.isReferencing(this)) {
                includedGroup.add(group)
                groupItems.addAll(group.getItemTypes())
            }
        }
    }

    override fun setNonGroupInheritedMaterials(types: Set<ItemType>) {
        super.setNonGroupInheritedMaterials(types)

        updateMaterials()
    }

    override fun getGroups(): MutableSet<AbstractMaterialGroup> {
        return includedGroup
    }

    override fun updateMaterials() {
        groupItems.clear()
        groupItems.addAll(includedItems)

        includedGroup.forEach { group ->
            groupItems.addAll(group.getItemTypes())
        }
    }

    override fun getItemTypes(): Set<ItemType> {
        return Collections.unmodifiableSet(groupItems)
    }


}