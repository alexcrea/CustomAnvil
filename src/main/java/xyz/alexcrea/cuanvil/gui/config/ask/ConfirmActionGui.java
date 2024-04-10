package xyz.alexcrea.cuanvil.gui.config.ask;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ConfirmActionGui extends AbstractAskGui {

    public ConfirmActionGui(@NotNull String title, String actionDescription,
                            Gui backOnCancel, Gui backOnConfirm, Supplier<Boolean> onConfirm) {
        super(3, title, backOnCancel);

        // Save item
        this.pane.bindItem('S', new GuiItem(GuiSharedConstant.CONFIRM_ITEM, event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }

            boolean success;
            try {
                success = onConfirm.get();
            } catch (Exception e) {
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not process confirmation supplier.", e);
                success = false;
            }

            if (!success) {
                event.getWhoClicked().sendMessage("\u00A7cAction could not be completed. ");
            }
            backOnConfirm.show(player);

        }, CustomAnvil.instance));

        // Info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();

        infoMeta.setDisplayName("\u00A7eAre you sure ?");
        infoMeta.setLore(Arrays.asList(actionDescription.split("\n")));

        infoItem.setItemMeta(infoMeta);

        pane.bindItem('I', new GuiItem(infoItem, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
    }

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "00B0I0S00",
                GuiSharedConstant.EMPTY_GUI_FULL_LINE
        );
    }

}
