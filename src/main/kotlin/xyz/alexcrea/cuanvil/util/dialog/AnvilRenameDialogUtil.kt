package xyz.alexcrea.cuanvil.util.dialog

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import net.kyori.adventure.text.Component
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.PrepareAnvilEvent
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.dialog.AnvilRenameDialog
import xyz.alexcrea.cuanvil.dialog.AnvilRenameDialogImpl
import xyz.alexcrea.cuanvil.update.UpdateUtils
import xyz.alexcrea.cuanvil.util.AnvilColorUtil

object AnvilRenameDialogUtil {

    val anvilRenameDialog: AnvilRenameDialog;

    init {
        val version = UpdateUtils.currentMinecraftVersion()
        anvilRenameDialog = (if(!PlatformUtil.isPaper ||
            (version.major <= 1 && version.minor <= 21 && version.patch <= 6)) {
            NoImplAnvilRenameDialog()
        } else {
            AnvilRenameDialogImpl({ player, component -> AnvilColorUtil.revertColorSmallest(
                component, AnvilColorUtil.renamePermission(player)
            ) },
                { ConfigOptions.renameDialogMaxSize },
                CustomAnvil.instance,
                )
        })
    }

    class NoImplAnvilRenameDialog: AnvilRenameDialog {

        override fun canSendDialog(): Boolean {
            return false
        }

        override fun tryShowDialog(
            player: HumanEntity,
            event: PrepareAnvilEvent
        ) {

        }

        override fun closeInventory(player: HumanEntity) {

        }

    }
}