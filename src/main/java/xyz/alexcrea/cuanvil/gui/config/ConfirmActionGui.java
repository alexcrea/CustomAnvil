package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ConfirmActionGui extends ChestGui {

    private static final ItemStack CANCEL_ITEM;
    private static final ItemStack CONFIRM_ITEM;
    static {
        CANCEL_ITEM = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta meta = CANCEL_ITEM.getItemMeta();
        meta.setDisplayName("\u00A7cCancel");
        meta.setLore(Collections.singletonList("\u00A77Cancel current action and return to previous menu."));
        CANCEL_ITEM.setItemMeta(meta);

        CONFIRM_ITEM = new ItemStack(Material.GREEN_TERRACOTTA);
        meta = CONFIRM_ITEM.getItemMeta();
        meta.setDisplayName("\u00A7aConfirm");
        meta.setLore(Arrays.asList("\u00A77Confirm current action.",
                "\u00A74Cation: This action can't be canceled."));
        CONFIRM_ITEM.setItemMeta(meta);
    }

    public ConfirmActionGui(@NotNull TextHolder title, String actionDescription, Gui backOnCancel, Gui backOnConfirm, Supplier<Boolean> onConfirm) {
        super(3, title, CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "000000000",
                "00B0I0S00",
                "000000000"
        );
        PatternPane pane = new PatternPane(0, 0, pattern.getLength(), pattern.getHeight(), pattern);
        addPane(pane);

        pane.bindItem('0', GuiGlobalItems.backgroundItem());

        pane.bindItem('B', new GuiItem(CANCEL_ITEM, GuiGlobalActions.openGuiAction(backOnCancel), CustomAnvil.instance));
        pane.bindItem('S', new GuiItem(CONFIRM_ITEM, event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }

            boolean success;
            try{
                success = onConfirm.get();
            }catch (Exception e){
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not process confirmation supplier.", e);
                success = false;
            }

            if(!success){
                event.getWhoClicked().sendMessage("\u00A7cAction could not be completed. ");
            }
            backOnConfirm.show(player);

        }, CustomAnvil.instance));

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();

        infoMeta.setDisplayName("\u00A7eAre you sure ?");
        infoMeta.setLore(Arrays.asList(actionDescription.split("\n")));

        infoItem.setItemMeta(infoMeta);

        pane.bindItem('I', new GuiItem(infoItem, GuiGlobalActions.stayInPlace, CustomAnvil.instance));
    }

}
