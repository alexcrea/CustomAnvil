package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.gui.config.settings.subsetting.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class EnchantConflictGui extends ChestGui {

    public final static EnchantConflictGui INSTANCE = new EnchantConflictGui();
    static {
        INSTANCE.init();
    }

    private final HashMap<EnchantConflictGroup, EnchantConflictSubSettingGui> conflictGuiMap;

    private EnchantConflictGui() {
        super(6, "Conflict Config", CustomAnvil.instance);
        this.conflictGuiMap = new HashMap<>();
    }

    private OutlinePane firstPage;
    private ArrayList<OutlinePane> pages;
    private HashMap<UUID, Integer> pageMap;
    private PatternPane backgroundPane;

    private void init(){
        // Back item panel
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B11L1R11C"
        );
        backgroundPane = new PatternPane(0, 0, 9, 6, Pane.Priority.LOW, pattern);

        GuiGlobalItems.addBackItem(backgroundPane, MainConfigGui.INSTANCE);

        GuiGlobalItems.addBackgroundItem(backgroundPane);
        backgroundPane.bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        backgroundPane.bindItem('C', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        addPane(backgroundPane);

        // Page init
        this.pages = new ArrayList<>();
        this.pageMap = new HashMap<>();

        // enchant item panel
        this.firstPage = creatEmptyPage();
        this.pages.add(this.firstPage);

        prepareOtherValues();
        reloadValues();
    }


    private GuiItem goLeftItem;
    private GuiItem goRightItem;
    private void prepareOtherValues() {
        //TODO item x2
        this.goLeftItem = new GuiItem(new ItemStack(Material.PAPER), event -> {
            HumanEntity viewer = event.getWhoClicked();
            UUID playerUUID = viewer.getUniqueId();
            int page =this.pageMap.getOrDefault(playerUUID, 0);
            this.pageMap.put(playerUUID, page-1);

            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            viewer.closeInventory();// Hacky trick to make the inventory update properly
            show(viewer);

            viewer.setItemOnCursor(cursor);
        }, CustomAnvil.instance);

        this.goRightItem = new GuiItem(new ItemStack(Material.PAPER), event -> {
            HumanEntity viewer = event.getWhoClicked();
            UUID playerUUID = viewer.getUniqueId();
            int page = pageMap.getOrDefault(playerUUID, 0);
            this.pageMap.put(playerUUID, page+1);

            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            viewer.closeInventory();// Hacky trick to make the inventory update properly
            show(viewer);

            viewer.setItemOnCursor(cursor);
        }, CustomAnvil.instance);

    }

    private OutlinePane creatEmptyPage(){
        OutlinePane page = new OutlinePane(0, 0, 9, 5);
        page.align(OutlinePane.Alignment.BEGIN);
        page.setOrientation(Orientable.Orientation.HORIZONTAL);

        return page;
    }

    public void reloadValues(){
        this.conflictGuiMap.forEach((conflict, gui) -> gui.cleanUnused());
        this.conflictGuiMap.clear();
        this.firstPage.clear();
        this.pages.clear();
        this.pages.add(this.firstPage);

        for (EnchantConflictGroup conflict : ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList()) {
            updateValueForConflict(conflict, false);
        }

        update();
    }

    public ItemStack createItemForConflict(EnchantConflictGroup conflict){
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        //TODO item
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(conflict.getName());

        item.setItemMeta(meta);
        return item;
    }

    public void updateValueForConflict(EnchantConflictGroup conflict, boolean shouldUpdate){
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);
        ItemStack item = createItemForConflict(conflict);

        GuiItem guiItem;
        if(gui == null){
            // Create new sub setting gui
            guiItem = new GuiItem(item, CustomAnvil.instance);
            gui = new EnchantConflictSubSettingGui(this, conflict, guiItem);

            guiItem.setAction(GuiGlobalActions.openGuiAction(gui));

            this.conflictGuiMap.put(conflict, gui);
            addToPage(guiItem);
        }else{
            // replace item with the updated one
            guiItem = gui.getParentItemForThisGui();
            guiItem.setItem(item);
        }

        gui.updateLocal();
        if(shouldUpdate){
            update();
        }

    }

    public void removeConflict(EnchantConflictGroup conflict){
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);
        if(gui == null) return;

        this.conflictGuiMap.remove(conflict);
        removeFromPage(gui.getParentItemForThisGui());

        update();
    }

    private void addToPage(GuiItem guiItem) {
        // Get first available page or create one
        OutlinePane page = this.pages.get(this.pages.size()-1);
        if(page.getItems().size() >= 5*9){
            page = creatEmptyPage();
            this.pages.add(page);
        }

        page.addItem(guiItem);
    }

    private void removeFromPage(GuiItem guiItem) {
        // get item page
        OutlinePane page = null;
        int pageID = 0;
        while(pageID < this.pages.size()){
            OutlinePane tempPage = this.pages.get(pageID++);
            if(tempPage.getItems().contains(guiItem)){
                page = tempPage;
                break;
            }
        }

        if(page == null){// Why...
            return;
        }
        removeFromPage(page, pageID, guiItem);
    }

    private void removeFromPage(OutlinePane page, int pageID, GuiItem guiItem) {
        page.removeItem(guiItem);

        // There is now a slot available, let fill it if possible
        if(pageID < this.pages.size() - 1){
            OutlinePane newPage = this.pages.get(pageID+1);
            GuiItem newItem = newPage.getItems().get(0);
            removeFromPage(newPage, pageID+1, newItem);
        }else if(pageID > 0 && page.getItems().isEmpty()){
            this.pages.remove(pageID);
        }
    }


    public int getPlayerPageID(UUID uuid){
        int pageId = this.pageMap.getOrDefault(uuid, 0);
        if(pageId >= this.pages.size()){
            pageId = this.pages.size()-1;
        }
        return pageId;
    }

    public void placeArrow(int page){
        // Place left arrow
        addPane(this.backgroundPane);
        if(page > 0){
            this.backgroundPane.bindItem('L', this.goLeftItem);
        }else{
            this.backgroundPane.bindItem('L', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        }

        // Place right arrow
        if(page < pages.size()-1){
            this.backgroundPane.bindItem('R', this.goRightItem);
        }else{
            this.backgroundPane.bindItem('R', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        }
    }

    @Override // assume will not be called in multiple thread
    public void show(@NotNull HumanEntity humanEntity) {
        int pageID = getPlayerPageID(humanEntity.getUniqueId());
        OutlinePane page = this.pages.get(pageID);

        getPanes().clear();

        // display the page arrow pane
        placeArrow(pageID);
        // and add actual page
        addPane(page);

        super.show(humanEntity);

    }

    @Override // assume will not be called in multiple thread
    public void click(@NotNull InventoryClickEvent event) {
        int pageID = getPlayerPageID(event.getWhoClicked().getUniqueId());
        OutlinePane page = this.pages.get(pageID);

        getPanes().clear();

        // set the page arrow pane
        placeArrow(pageID);
        // and add actual page
        addPane(page);

        super.click(event);
    }

}
