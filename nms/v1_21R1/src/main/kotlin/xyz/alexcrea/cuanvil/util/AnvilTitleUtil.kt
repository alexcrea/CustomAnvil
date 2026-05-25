package xyz.alexcrea.cuanvil.util

import org.bukkit.inventory.InventoryView

// TODO yet another cleanup to do on legacy removal branch
object AnvilTitleUtil {

    fun rename(view: InventoryView, name: String) {
        view.title = name
    }

}