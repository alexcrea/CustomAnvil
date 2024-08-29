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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SelectItemTypeGui extends AbstractAskGui {

    private ItemStack selectedItem;
    public SelectItemTypeGui(@NotNull String title,
                             @NotNull String actionDescription,
                             @NotNull Gui backOnCancel,
                             @NotNull BiConsumer<ItemStack, HumanEntity> onSave,
                             boolean materialOnly) {
        super(3, title, backOnCancel);
        this.selectedItem = null;

        // Save item
        GuiItem confirmItem = new GuiItem(GuiSharedConstant.CONFIRM_ITEM, event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }

            onSave.accept(this.selectedItem, player);

        }, CustomAnvil.instance);
        this.pane.bindItem('S', GuiGlobalItems.backgroundItem());

        // Select item
        ItemStack selectItem = setDisplayMeta(new ItemStack(Material.BARRIER), actionDescription);

        AtomicReference<GuiItem> selectGuiItem = new AtomicReference<>();
        selectGuiItem.set(new GuiItem(selectItem, event -> {
            event.setCancelled(true);

            ItemStack cursor = event.getWhoClicked().getItemOnCursor();
            if(cursor.getType().isAir()) return;

            ItemStack finalItem;
            if(materialOnly){
                finalItem = setDisplayMeta(new ItemStack(cursor.getType()), actionDescription);
            }else{
                finalItem = cursor.clone();
            }
            this.selectedItem = finalItem.clone();

            selectGuiItem.get().setItem(finalItem);
            this.pane.bindItem('S', confirmItem);

            update();
        }, CustomAnvil.instance));

        this.pane.bindItem('V', selectGuiItem.get());

        // Temporary leave item
        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_STAINED_GLASS_PANE, this);

        this.pane.bindItem('s', temporaryLeave);

    }

    private ItemStack setDisplayMeta(ItemStack item, String actionDescription){
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§ePlace an item here");
        meta.setLore(Arrays.asList(actionDescription.split("\n")));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "0000V000s",
                "B0000000S"
        );
    }
}
