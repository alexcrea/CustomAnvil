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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SelectItemTypeGui extends AbstractAskGui {

    private ItemStack selectedItem;

    public SelectItemTypeGui(@NotNull Message title,
                             @NotNull String titleParam,
                             @NotNull Message actionDescription,
                             @NotNull String descriptionParam,
                             @NotNull Gui backOnCancel,
                             @NotNull BiConsumer<ItemStack, HumanEntity> onSave,
                             boolean materialOnly) {
        super(3, title, titleParam, backOnCancel);
        this.selectedItem = null;

        // Save item
        GuiItem confirmItem = new GuiItem(GuiSharedConstant.CONFIRM_ITEM, event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
                return;
            }

            onSave.accept(this.selectedItem, player);

        }, CustomAnvil.instance);
        this.pane.bindItem('S', GuiGlobalItems.backgroundItem());

        // Select item
        ItemStack selectItem = setDisplayMeta(new ItemStack(Material.BARRIER), actionDescription, descriptionParam);

        AtomicReference<GuiItem> selectGuiItem = new AtomicReference<>();
        selectGuiItem.set(new GuiItem(selectItem, event -> {
            event.setCancelled(true);

            ItemStack cursor = event.getWhoClicked().getItemOnCursor();
            if(MaterialUtil.INSTANCE.isAir(cursor)) return;

            ItemStack finalItem;
            if(materialOnly) {
                finalItem = setDisplayMeta(new ItemStack(cursor.getType()), actionDescription, descriptionParam);
            } else {
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

    @NotNull
    private ItemStack setDisplayMeta(
            @NotNull ItemStack item,
            @NotNull Message actionDescription,
            @NotNull String param
    ) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getSELECT_ITEM_TYPE_PLACE_HERE());
        ComponentUtil.INSTANCE.applyLore(actionDescription.formatted(param), meta);

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
