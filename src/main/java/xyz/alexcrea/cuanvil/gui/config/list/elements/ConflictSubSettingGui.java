package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.recipe.CustomAnvilRecipeManager;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConflictSubSettingGui extends MappedToListSubSettingGui {

    private final GroupConfigGui parent;
    private final AbstractMaterialGroup group;
    private final PatternPane pane;

    public ConflictSubSettingGui(
            @NotNull GroupConfigGui parent,
            @NotNull AbstractMaterialGroup group,
            @NotNull GuiItem item) {
        super(item, 3, group.getName());
        this.parent = parent;
        this.group = group;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "00102000D",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(this.pane);

        prepareStaticValues();
    }

    private void prepareStaticValues() {
        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE RECIPE");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

    }

    private @NotNull Consumer<InventoryClickEvent> openGuiAction() {
        ConfirmActionGui deleteGui = createDeleteGui();
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            // TODO test if group is used & cancel if so

            deleteGui.show(player);
        };
    }

    private @NotNull ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            ItemGroupManager manager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
            // TODO test if group is used & cancel if so

            // TODO remove group
            Bukkit.broadcastMessage("todo");


            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.group.toString()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this group.",
                this, this.parent, deleteSupplier
        );
    }

    @Override
    public void updateGuiValues() {
        // TODO update value from config to conflict

        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.group, true);
    }

    @Override
    public void updateLocal() {

    }

    @Override
    public void cleanAndBeUnusable() {

    }
}
