package xyz.alexcrea.cuanvil.util

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.InventoryView

// Hotswap to not relocate
// So I just put small thing calling relocating method here to enable to hotswap more class
// Especially for PrepareAnvilListener
// Will be able to replace that on legacy removal so really temporary
object JustForEasierHotswapUtil {

    fun getPlayerFromView(view: InventoryView): HumanEntity {
        return InventoryViewUtil.getInstance().getPlayer(view)
    }
    
}