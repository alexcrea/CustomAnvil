package xyz.alexcrea.cuanvil.dependency.plugins

import com.artillexstudios.axplayerwarps.libs.axapi.gui.AnvilInput
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

class AxPlayerWarpsDependency {

    fun testIfGui(player: HumanEntity): Boolean {
        return player is Player && AnvilInput.get(player) != null
    }

}