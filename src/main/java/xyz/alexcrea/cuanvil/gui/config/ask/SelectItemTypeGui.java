package xyz.alexcrea.cuanvil.gui.config.ask;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.function.BiConsumer;

@NotNullByDefault
public class SelectItemTypeGui extends AbstractAskGui {

    private final Message actionDescription;
    private final String descriptionParam;

    private final BiConsumer<ItemStack, HumanEntity> onSave;

    private final boolean materialOnly;

    private final GuiItem selectItem;

    public SelectItemTypeGui(
            Message title,
            String titleParam,
            Message actionDescription,
            String descriptionParam,
            Gui backOnCancel,
            BiConsumer<ItemStack, HumanEntity> onSave,
            boolean materialOnly
    ) {
        super(3, title, titleParam, backOnCancel);
        this.actionDescription = actionDescription;
        this.descriptionParam = descriptionParam;

        this.onSave = onSave;

        this.materialOnly = materialOnly;

        // Save item
        this.pane.bindItem('S', GuiGlobalItems.backgroundItem());

        // Select item
        ItemStack selectItemStack = setDisplayMeta(new ItemStack(Material.BARRIER), actionDescription, descriptionParam);

        selectItem = new GuiItem(selectItemStack, this::selectHandler,
                CustomAnvil.instance
        );

        this.pane.bindItem('V', selectItem);

        // Temporary leave item
        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_STAINED_GLASS_PANE, this);

        this.pane.bindItem('s', temporaryLeave);
    }

    private void selectHandler(
            InventoryClickEvent event
    ) {
        event.setCancelled(true);

        ItemStack cursor = event.getWhoClicked().getItemOnCursor();
        if(MaterialUtil.INSTANCE.isAir(cursor)) return;

        ItemStack finalItem;
        if(this.materialOnly) {
            finalItem = setDisplayMeta(
                    new ItemStack(cursor.getType()),
                    this.actionDescription,
                    this.descriptionParam
            );
        } else {
            finalItem = cursor.clone();
        }
        this.selectItem.setItem(finalItem);

        var selectedItem = finalItem.clone();
        // Save item
        var confirmItem = GuiGlobalItems.confirmItem(confirmEvent ->
                confirmHandler(confirmEvent, selectedItem)
        );
        this.pane.bindItem('S', confirmItem);

        update();
    }

    private void confirmHandler(
            InventoryClickEvent event,
            ItemStack selected
    ) {
        event.setCancelled(true);
        HumanEntity player = event.getWhoClicked();

        if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
            player.closeInventory();
            MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
            return;
        }

        this.onSave.accept(selected, player);
    }

    private ItemStack setDisplayMeta(
            ItemStack item,
            Message actionDescription,
            String param
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
