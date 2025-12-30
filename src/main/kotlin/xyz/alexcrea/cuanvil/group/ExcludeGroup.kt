package xyz.alexcrea.cuanvil.group

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.inventory.ItemType
import java.util.*

@Deprecated("Need rework to reduce memory cost as not enum set")
@Suppress("UnstableApiUsage")
class ExcludeGroup(name: String) : AbstractMaterialGroup(name) {

    override fun createDefaultSet(): MutableSet<ItemType> {
        val types: MutableSet<ItemType> = HashSet()

        types.addAll(RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM))
        return types
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

    override fun addToPolicy(type: ItemType): ExcludeGroup {
        includedItems.remove(type)
        groupItems.remove(type)

        return this
    }

    override fun addToPolicy(other: AbstractMaterialGroup): ExcludeGroup {
        includedGroup.add(other)
        groupItems.removeAll(other.getItemTypes())

        return this
    }

    override fun setGroups(groups: MutableSet<AbstractMaterialGroup>) {
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