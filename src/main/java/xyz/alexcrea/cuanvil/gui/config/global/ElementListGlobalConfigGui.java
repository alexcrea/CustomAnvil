package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
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
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class ElementListGlobalConfigGui< T > extends ValueUpdatableGui {


    public ElementListGlobalConfigGui(@NotNull String title) {
        super(6, title, CustomAnvil.instance);
    }


    protected OutlinePane firstPage;
    protected ArrayList<OutlinePane> pages;
    protected HashMap<UUID, Integer> pageMap;
    protected PatternPane backgroundPane;

    public void init() {
        // Back item panel
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B11L1R11C"
        );
        this.backgroundPane = new PatternPane(0, 0, 9, 6, Pane.Priority.LOW, pattern);

        GuiGlobalItems.addBackItem(this.backgroundPane, MainConfigGui.INSTANCE);

        GuiGlobalItems.addBackgroundItem(this.backgroundPane);
        this.backgroundPane.bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        addPane(this.backgroundPane);

        // Page init
        this.pages = new ArrayList<>();
        this.pageMap = new HashMap<>();

        // enchant item panel
        this.firstPage = createEmptyPage();
        this.pages.add(this.firstPage);

        prepareStaticValues();
        reloadValues();
    }

    protected GuiItem goLeftItem;
    protected GuiItem goRightItem;

    protected void prepareStaticValues(){
        // Left item creation for consumer & bind
        this.goLeftItem = new GuiItem(new ItemStack(Material.RED_TERRACOTTA), event -> {
            HumanEntity viewer = event.getWhoClicked();
            UUID playerUUID = viewer.getUniqueId();
            int page = this.pageMap.getOrDefault(playerUUID, 0);
            this.pageMap.put(playerUUID, page - 1);

            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            show(viewer);

            viewer.setItemOnCursor(cursor);
        }, CustomAnvil.instance);

        // Right item creation for consumer & bind
        this.goRightItem = new GuiItem(new ItemStack(Material.GREEN_TERRACOTTA), event -> {
            HumanEntity viewer = event.getWhoClicked();
            UUID playerUUID = viewer.getUniqueId();
            int page = pageMap.getOrDefault(playerUUID, 0);
            this.pageMap.put(playerUUID, page + 1);

            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            show(viewer);

            viewer.setItemOnCursor(cursor);
        }, CustomAnvil.instance);

        this.backgroundPane.bindItem('C', prepareCreateNewItem());
    }
    protected void reloadValues(){
        this.firstPage.clear();
        this.pages.clear();
        this.pages.add(this.firstPage);

        for (T conflict : getEveryDisplayableInstanceOfGeneric()) {
            updateValueForGeneric(conflict, false);
        }

        update();
    }

    protected abstract GuiItem prepareCreateNewItem();

    protected OutlinePane createEmptyPage() {
        OutlinePane page = new OutlinePane(0, 0, 9, 5);
        page.align(OutlinePane.Alignment.BEGIN);
        page.setOrientation(Orientable.Orientation.HORIZONTAL);

        return page;
    }

    public int getPlayerPageID(UUID uuid) {
        int pageId = this.pageMap.getOrDefault(uuid, 0);
        if (pageId >= this.pages.size()) {
            pageId = this.pages.size() - 1;
        }
        return pageId;
    }

    protected void addToPage(GuiItem guiItem) {
        // Get first available page or create one
        OutlinePane page = this.pages.get(this.pages.size() - 1);
        if (page.getItems().size() >= 5 * 9) {
            page = createEmptyPage();
            this.pages.add(page);
        }

        page.addItem(guiItem);
    }

    private void removeFromPage(GuiItem guiItem) {
        // get item page
        OutlinePane page = null;
        int pageID = 0;
        while (pageID < this.pages.size()) {
            OutlinePane tempPage = this.pages.get(pageID);
            if (tempPage.getItems().contains(guiItem)) {
                page = tempPage;
                break;
            }
            pageID++;
        }

        if (page == null) {// Why...
            return;
        }
        removeFromPage(page, pageID, guiItem);
    }

    private void removeFromPage(OutlinePane page, int pageID, GuiItem guiItem) {
        page.removeItem(guiItem);

        // There is now a slot available, let fill it if possible
        if (pageID < (this.pages.size() - 1)) {
            OutlinePane newPage = this.pages.get(pageID + 1);
            GuiItem nextPageItem = newPage.getItems().get(0);

            removeFromPage(newPage, pageID + 1, nextPageItem);

            OutlinePane thisPage = this.pages.get(pageID);
            thisPage.addItem(nextPageItem);
        } else if (pageID > 0 && page.getItems().isEmpty()) {
            this.pages.remove(pageID);
        }
    }

    public void placeArrow(int page, boolean customise) {

        // Place left arrow
        addPane(this.backgroundPane);
        if (page > 0) {
            if (customise) {
                ItemStack leftItem = this.goLeftItem.getItem();
                ItemMeta leftMeta = leftItem.getItemMeta();

                leftMeta.setDisplayName("\u00A7eReturn to page " + (page));

                leftItem.setItemMeta(leftMeta);
                this.goLeftItem.setItem(leftItem);
            }

            this.backgroundPane.bindItem('L', this.goLeftItem);
        } else {
            this.backgroundPane.bindItem('L', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        }

        // Place right arrow
        if (page < pages.size() - 1) {
            if (customise) {
                ItemStack rightItem = this.goRightItem.getItem();
                ItemMeta rightMeta = rightItem.getItemMeta();

                rightMeta.setDisplayName("\u00A7eGo to page " + (page + 2));

                rightItem.setItemMeta(rightMeta);
                this.goRightItem.setItem(rightItem);
            }

            this.backgroundPane.bindItem('R', this.goRightItem);
        } else {
            this.backgroundPane.bindItem('R', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        }
    }

    @Override // assume will not be called in multiple thread
    public void show(@NotNull HumanEntity humanEntity) {
        int pageID = getPlayerPageID(humanEntity.getUniqueId());
        OutlinePane page = this.pages.get(pageID);

        getPanes().clear();

        // display the page arrow pane
        placeArrow(pageID, true);
        // and add actual page
        addPane(page);

        // set title
        setTitle("Conflict Config (" + (pageID + 1) + "/" + (pages.size()) + ")");

        super.show(humanEntity);

    }

    @Override // assume will not be called in multiple thread
    public void click(@NotNull InventoryClickEvent event) {
        int pageID = getPlayerPageID(event.getWhoClicked().getUniqueId());
        OutlinePane page = this.pages.get(pageID);

        getPanes().clear();

        // set the page arrow pane
        placeArrow(pageID, false);
        // and add actual page
        addPane(page);

        super.click(event);
    }

    // -------------------------
    // Methods using generic T
    // -------------------------

    public void updateValueForGeneric(T generic, boolean shouldUpdate) {
        ItemStack item = createItemForGeneric(generic);

        updateGeneric(generic, item);

        if (shouldUpdate) {
            update();
        }

    }

    public void removeGeneric(T generic) {
        GuiItem item = findGuiItemForRemoval(generic);
        if(item == null) return;
        removeFromPage(item);

        update();
    }

    protected abstract GuiItem findGuiItemForRemoval(T generic);

    protected abstract ItemStack createItemForGeneric(T generic);

    protected abstract void updateGeneric(T generic, ItemStack usedItem);

    protected abstract List<T> getEveryDisplayableInstanceOfGeneric();

    @Override
    public void updateGuiValues() {
        // Not the optimised way to update this gui
        // TODO maybe rework ValueUpdatableGui and it's dependency to allow a 1 item reload every time.

        reloadValues();
    }

}
