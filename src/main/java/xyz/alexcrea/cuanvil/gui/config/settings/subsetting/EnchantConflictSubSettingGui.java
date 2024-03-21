package xyz.alexcrea.cuanvil.gui.config.settings.subsetting;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.openable.EnchantConflictGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.function.Supplier;

public class EnchantConflictSubSettingGui extends ValueUpdatableGui {

    private final EnchantConflictGui parent;
    private final EnchantConflictGroup enchantConflict;
    private final PatternPane pane;
    private boolean canOpen = true;

    public EnchantConflictSubSettingGui(
            @NotNull EnchantConflictGui parent,
            @NotNull EnchantConflictGroup enchantConflict) {
        super(3, "\u00A7aConfig for \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()), CustomAnvil.instance);
        this.parent = parent;
        this.enchantConflict = enchantConflict;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "00EGM000D",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(this.pane);

        prepareStaticValues();
        //updateGuiValues();
    }

    private void prepareStaticValues() {

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);
        GuiGlobalItems.addBackgroundItem(pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE CONFLICT");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

    }

    private ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () ->{
            EnchantConflictManager manager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();

            // Remove from manager
            for (Enchantment enchantment : enchantConflict.getEnchants()) {
                manager.removeConflictFromMap(enchantment, enchantConflict);
            }
            manager.conflictList.remove(enchantConflict);

            // Update config file storage
            ConfigHolder.CONFLICT_HOLDER.getConfig().set(enchantConflict.getName(), null);

            // Save
            boolean success = true;
            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e"+CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName())+"\u00A7c ?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
                );
    }

    @Override
    public void updateGuiValues() {
        this.parent.updateValueForConflict(this.enchantConflict);
        // Parent should call updateLocal
    }

    public void updateLocal(){
        if(!this.canOpen) return;


    }

    public void cleanUnused(){
        for (HumanEntity viewer : getViewers()) {
            this.parent.show(viewer);
        }
        this.canOpen = false;

        // Just in case something is extremely wrong
        GuiItem background = GuiGlobalItems.backgroundItem();
        pane.bindItem('E', background);
        pane.bindItem('G', background);
        pane.bindItem('M', background);
        pane.bindItem('D', background);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if(this.canOpen){
            super.show(humanEntity);
        }else{
            parent.show(humanEntity);
        }
    }
}
