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
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgError;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.awt.*;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ConfirmActionGui extends AbstractAskGui {

    public ConfirmActionGui(
            @NotNull Message title, @NotNull String titleParam,
            @Nullable Message actionDescription, @NotNull String actionParam,
            Gui backOnCancel, Gui backOnConfirm, Supplier<Boolean> onConfirm,
            boolean permanent
    ) {
        super(3, title, titleParam, backOnCancel);

        // Save item
        this.pane.bindItem('S', new GuiItem(
                (permanent ? GuiSharedConstant.CONFIRM_PERMANENT_ITEM : GuiSharedConstant.CONFIRM_ITEM),
                event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
                return;
            }

            boolean success;
            try {
                success = onConfirm.get();
            } catch (Exception e) {
                CustomAnvil.Companion.logError(MsgError.INSTANCE.getCONFIRM_ACTION_GENERIC().unformatted(), e, true, Level.WARNING);
                success = false;
            }

            if (!success) {
                MsgUI.INSTANCE.getCONFIRM_ACTION_FAILED().send(player);
            }
            backOnConfirm.show(player);

        }, CustomAnvil.instance));

        // Info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        assert infoMeta != null;

        ComponentUtil.INSTANCE.setMessageName(infoMeta, MsgUI.INSTANCE.getCONFIRM_ACTION_ARE_YOU_SURE());
        if(actionDescription != null){
            ComponentUtil.INSTANCE.applyLore(actionDescription.formatted(actionParam), infoMeta);
        }

        infoItem.setItemMeta(infoMeta);

        pane.bindItem('I', new GuiItem(infoItem, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
    }
    public ConfirmActionGui(
            @NotNull Message title, @NotNull String titleParam,
            @Nullable Message actionDescription, @NotNull String actionParam,
            Gui backOnCancel, Gui backOnConfirm, Supplier<Boolean> onConfirm
    ){
        this(title, titleParam, actionDescription, actionParam, backOnCancel, backOnConfirm, onConfirm, true);
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
