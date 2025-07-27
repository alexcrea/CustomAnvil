package xyz.alexcrea.cuanvil.group

import org.bukkit.Registry
import org.bukkit.inventory.ItemType
import java.util.*

@Deprecated("Need rework to reduce memory cost as not enum set")
class ExcludeItemTypeGroup(name: String) : AbstractItemTypeGroup(name) {

    override fun createDefaultSet(): MutableSet<ItemType> {
        val types: MutableSet<ItemType> = HashSet()

        types.addAll(Registry.ITEM)
        return types
    }

    private var includedGroup: MutableSet<AbstractItemTypeGroup> = HashSet()
    private val groupItems by lazy { createDefaultSet() }

    override fun isReferencing(other: AbstractItemTypeGroup): Boolean {
        for (materialGroup in includedGroup.iterator()) {
            if ((materialGroup == other) || (materialGroup.isReferencing(other))) {
                return true
            }
        }
        return false
    }

    override fun addToPolicy(type: ItemType): ExcludeItemTypeGroup {
        includedItems.remove(type)
        groupItems.remove(type)

        return this
    }

    override fun addToPolicy(other: AbstractItemTypeGroup): ExcludeItemTypeGroup {
        includedGroup.add(other)
        groupItems.removeAll(other.getItemTypes())

        return this
    }

    override fun setGroups(groups: MutableSet<AbstractItemTypeGroup>) {
        groupItems.clear()
        groupItems.addAll(includedItems)

        includedGroup.clear()
        groups.forEach { group ->
            if (!group.isReferencing(this)) {
                includedGroup.add(group)
                groupItems.removeAll(group.getItemTypes())
            }
        }
    }

    override fun getGroups(): MutableSet<AbstractItemTypeGroup> {
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