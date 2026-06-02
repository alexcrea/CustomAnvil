package xyz.alexcrea.cuanvil.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.world.inventory.AnvilMenu
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.craftbukkit.inventory.view.CraftAnvilView
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

@Suppress("UnstableApiUsage")
class AnvilRenameDialogImpl(
    val fromFormated: BiFunction<HumanEntity, Component?, String?>,
    val keepUserPreviousDialog: Supplier<Boolean>,
    val maxLength: Supplier<Int>,
    val plugin: Plugin,
): AnvilRenameDialog {

    companion object {
        private const val RENAME_TEXT_KEY = "rename"

        private const val MAX_WIDTH = 512

        private val PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText()

        // Need to be able to translate it later !
        private val USER_FACING_RENAME_TITLE = Component.text("Rename Your Item")
        private val USER_FACING_WARNING = Component.text("Note that the repair text will appear blank after Confirm\n" +
                "But the name will be correctly applied")
        private val USER_FACING_CONFIRM = Component.text("Confirm").color(TextColor.fromHexString("#40FF40"))
        private val USER_FACING_CANCEL = Component.text("Cancel").color(TextColor.fromHexString("#FF4040"))
    }

    private val lastNames = HashMap<UUID, String>()
    private val lastLeftItem = HashMap<UUID, String>()
    private val runTaskMap = HashMap<UUID, ScheduledTask>()

    private val containerField = CraftInventoryView::class.java.getDeclaredField("container")

    init {
        containerField.setAccessible(true)
    }

    override fun canSendDialog(): Boolean {
        return true
    }

    fun makeDialog(initial: String?, callback: Consumer<String?>): Dialog {
        val maxLength = this.maxLength.get()
        val initialFinal = initial?.take(maxLength)

        val baseBuilder = DialogBase.builder(USER_FACING_RENAME_TITLE)
            .canCloseWithEscape(true)
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .inputs(listOf(
                DialogInput.text(RENAME_TEXT_KEY, Component.text("Rename text"))
                    .maxLength(maxLength)
                    .initial(initialFinal ?: "")
                    .labelVisible(false)
                    .width(MAX_WIDTH)
                    .build(),
                ),
            )
        baseBuilder.body(listOf(
                DialogBody.plainMessage(USER_FACING_WARNING, MAX_WIDTH)
            ))

        return Dialog.create { builder -> builder.empty()
            .base(baseBuilder.build())
            .type(DialogType.confirmation(
                ActionButton.builder(USER_FACING_CONFIRM)
                    .action(DialogAction.customClick({ response, _ ->
                        val text = response.getText(RENAME_TEXT_KEY)!!
                        callback.accept(text)
                    }, ClickCallback.Options.builder().build()))
                    .build(),
                ActionButton.builder(USER_FACING_CANCEL)
                    .build(),
            ))
        }
    }

    private fun setResult(player: HumanEntity, view: CraftAnvilView, result: String?) {
        val defaultName = PLAIN_TEXT_SERIALIZER.serializeOrNull(view.getItem(0)?.effectiveName())
        if(defaultName == result) {
            setName(player, view, "")
            if(defaultName != null) lastNames[player.uniqueId] = defaultName
        }
        else setName(player, view, result)
    }

    private fun setName(player: HumanEntity, view: CraftAnvilView, name: String?) {
        val menu = (containerField.get(view) as AnvilMenu)
        menu.itemName = name

        if(name == null)
            lastNames.remove(player.uniqueId)
        else
            lastNames[player.uniqueId] = name
        CraftEventFactory.callPrepareResultEvent(menu, 2);
    }

    private fun nameFromItem(player: HumanEntity, item: ItemStack?): String? {
        // Already has text
        if(item?.hasItemMeta() != true || !item.itemMeta.hasCustomName())
            return PLAIN_TEXT_SERIALIZER.serializeOrNull(item?.effectiveName())

        if(keepUserPreviousDialog.get() && item.hasItemMeta()) {
            val lastName = item.itemMeta.persistentDataContainer.get(
                AnvilRenameDialog.PCD_KEEP_RENAME_TEXT_KEY,
                PersistentDataType.STRING)

            if(lastName != null) return lastName
        }

        return fromFormated.apply(player, item.effectiveName())
    }

    private fun tryShowDialogScheduled(player: HumanEntity, event: PrepareAnvilEvent) {
        val view = event.view
        if(view !is CraftAnvilView) return

        val renameText = view.renameText
        val leftItem = view.getItem(0)
        val leftItemStr = leftItem?.toString()
        val lastName = lastNames.getOrDefault(player.uniqueId, null)

        if(lastLeftItem.getOrDefault(player.uniqueId, null) != leftItemStr) {
            if(leftItemStr == null)
                lastLeftItem.remove(player.uniqueId)
            else lastLeftItem[player.uniqueId] = leftItemStr

            setName(player, view, nameFromItem(player, leftItem))
            return
        }

        if(lastName == renameText)
            return

        if(renameText?.isBlank() == true) {
            setName(player, view, lastNames[player.uniqueId])
            return
        }

        val dialog = makeDialog(lastName)
        { result -> setResult(player, view, result) }
        player.showDialog(dialog)
    }

    // We need to wait for a short time as changing item will change the name BEFORE the item change
    // no guaranty both of them came in the same tick too so let's wait 2 tick....
    override fun tryShowDialog(player: HumanEntity, event: PrepareAnvilEvent) {
        runTaskMap.remove(player.uniqueId)?.cancel()

        val task = player.scheduler.runDelayed(
            plugin,
            { _ ->
                run { tryShowDialogScheduled(player, event) }
            },
            {},
            2
        )
        if(task == null) return

        runTaskMap[player.uniqueId] = task
    }

    override fun closeInventory(player: HumanEntity) {
        lastNames.remove(player.uniqueId)
        lastLeftItem.remove(player.uniqueId)
        runTaskMap.remove(player.uniqueId)?.cancel()
    }

    override fun currentText(player: HumanEntity): String? {
        return lastNames[player.uniqueId]
    }

}