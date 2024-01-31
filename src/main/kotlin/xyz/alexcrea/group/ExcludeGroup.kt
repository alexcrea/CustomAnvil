package xyz.alexcrea.group

import org.bukkit.Material

class ExcludeGroup(name: String): IncludeGroup(name) {

    override fun contain(mat: Material): Boolean {
        Material.POLISHED_DIORITE
        return !super.contain(mat)
    }

}