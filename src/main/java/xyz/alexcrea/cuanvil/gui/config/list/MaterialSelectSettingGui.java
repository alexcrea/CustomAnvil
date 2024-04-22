package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.config.SelectMaterialContainer;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MaterialSelectSettingGui extends MappedElementListConfigGui<Material, GuiItem> {

    private final SelectMaterialContainer selector;
    private final List<Material> defaultMaterials;
    private final int defaultMaterialHash;
    private int nowMaterialHash;

    public MaterialSelectSettingGui(
            @NotNull SelectMaterialContainer selector,
            @NotNull String title,
            @NotNull Gui backGui) {
        super(title);
        this.selector = selector;
        this.defaultMaterials = new ArrayList<>(this.selector.getSelectedMaterials());

        this.defaultMaterialHash = hashFromMaterialList(this.defaultMaterials);
        this.nowMaterialHash = this.defaultMaterialHash;

        init();

        // Change back item
        this.backgroundPane.bindItem('B', GuiGlobalItems.backItem(backGui));
    }

    @Override
    protected Pattern getBackgroundPattern(){
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "BT1LAR11S"
        );
    }

    private GuiItem saveItem;
    private GuiItem noChangeItem;

    @Override
    protected void prepareStaticValues() {
        super.prepareStaticValues();

        // Temporary leave item
        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_TERRACOTTA, this);
        this.backgroundPane.bindItem('T', temporaryLeave);

        // Select new mat item
        ItemStack selectItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta selectMeta = selectItem.getItemMeta();

        selectMeta.setDisplayName("aaaaaaa");

        selectItem.setItemMeta(selectMeta);

        this.backgroundPane.bindItem('A', new GuiItem(selectItem, setItemAsCursor(), CustomAnvil.instance));

        // Save item
        this.saveItem = GuiGlobalItems.noChangeItem();

        this.noChangeItem = GuiGlobalItems.noChangeItem();
        this.backgroundPane.bindItem('S', this.noChangeItem);

    }

    /**
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> setItemAsCursor() {
        return event -> {
            event.setCancelled(true);

            HumanEntity player = event.getWhoClicked();
            ItemStack cursor = player.getItemOnCursor();

            if(cursor.getType().isAir()) return;

            Material cursorMat = cursor.getType();
            if(!this.elementGuiMap.containsKey(cursorMat)){
                updateValueForGeneric(cursorMat, true);
                this.nowMaterialHash ^= cursorMat.hashCode();

                testCanSave();
            }

            update();
        };
    }

    @Override
    protected ItemStack createItemForGeneric(Material material) {
        return new ItemStack(material); //this is temp TODO the function
    }

    @Override
    protected List<Material> getEveryDisplayableInstanceOfGeneric() {
        return this.defaultMaterials;
    }

    @Override
    protected void updateElement(Material generic, GuiItem element) {
        if(ConfigOptions.INSTANCE.getDebugLog()){
            CustomAnvil.instance.getLogger().log(Level.INFO,
                    "Call that should not happen happened...",
                    new IllegalStateException());
        }
    }

    @Override
    protected GuiItem newElementRequested(Material generic, GuiItem newItem) {
        newItem.setAction(GuiGlobalActions.stayInPlace); //TODO ask to remove the item on click (or instant remove)
        return newItem;
    }

    @Override
    protected GuiItem findItemFromElement(Material generic, GuiItem element) {
        return element;
    }

    @Override
    protected GuiItem findGuiItemForRemoval(Material generic, GuiItem element) {
        return element;
    }

    private static int hashFromMaterialList(List<Material> materialList){
        int defaultMaterialHash = 0;
        for (Material material : materialList) {
            defaultMaterialHash ^= material.hashCode();
        }
        return defaultMaterialHash;
    }

    private void testCanSave() {
        if(this.defaultMaterialHash == this.nowMaterialHash){
            this.backgroundPane.bindItem('S', this.noChangeItem);
        }else{
            this.backgroundPane.bindItem('S', this.saveItem);
        }

    }



    // Unused functions.
    @Override
    protected GuiItem prepareCreateNewItem() {// Not used
        return null;
    }
    @Override
    protected Consumer<String> prepareCreateItemConsumer(HumanEntity player) {// Not used
        return null;
    }

    @Override
    protected String genericDisplayedName() {// Not Used
        return null;
    }
}
