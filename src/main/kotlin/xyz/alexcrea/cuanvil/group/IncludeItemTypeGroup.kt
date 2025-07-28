package xyz.alexcrea.cuanvil.group

import org.bukkit.inventory.ItemType
import java.util.*

class IncludeItemTypeGroup(name: String) : AbstractItemTypeGroup(name) {
    override fun createDefaultSet(): MutableSet<ItemType> {
        return HashSet()
    }

    private var includedGroup: MutableSet<AbstractItemTypeGroup> = HashSet()
    private val groupItems by lazy { createDefaultSet() }

    override fun isReferencing(other: AbstractItemTypeGroup): Boolean {
        for (subGroup in includedGroup.iterator()) {
            if ((subGroup == other) || (subGroup.isReferencing(other))) {
                return true
            }
        }
        return false
    }

    override fun addToPolicy(type: ItemType): IncludeItemTypeGroup {
        includedItems.add(type)
        groupItems.add(type)

        return this
    }

    override fun addToPolicy(other: AbstractItemTypeGroup): IncludeItemTypeGroup {
        includedGroup.add(other)
        groupItems.addAll(other.getItemTypes())

        return this
    }

    override fun setGroups(groups: MutableSet<AbstractItemTypeGroup>) {
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

    override fun setNonGroupInheritedItemTypes(types: Set<ItemType>) {
        super.setNonGroupInheritedItemTypes(types)

        update()
    }

    override fun getGroups(): MutableSet<AbstractItemTypeGroup> {
        return includedGroup
    }

    override fun update() {
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