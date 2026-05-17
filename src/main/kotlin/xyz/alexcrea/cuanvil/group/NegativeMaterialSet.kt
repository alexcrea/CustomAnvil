package xyz.alexcrea.cuanvil.group

import org.bukkit.NamespacedKey
import xyz.alexcrea.cuanvil.util.MaterialUtil
import xyz.alexcrea.cuanvil.util.NegativeSet

class NegativeMaterialSet: NegativeSet<NamespacedKey>() {

    override fun iterator(): MutableIterator<NamespacedKey> {
        val materials = MaterialUtil.getMaterials()
        materials.removeIf { negate.contains(it) }

        return materials.iterator()
    }

    override fun isEmpty(): Boolean {
        return negate.size >= MaterialUtil.getMaterialCount()
    }

    override val size get() = MaterialUtil.getMaterialCount() - negate.size

}