package xyz.alexcrea.group

import org.bukkit.Material

class ExcludeGroup(name: String): IncludeGroup(name) {

    override fun contain(mat: Material): Boolean {
        return !super.contain(mat)
    }

}