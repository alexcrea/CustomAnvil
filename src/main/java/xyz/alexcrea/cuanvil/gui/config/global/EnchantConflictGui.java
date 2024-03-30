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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.gui.config.settings.subsetting.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

        prepareOtherValues();
        reloadValues();
    }


    private GuiItem goLeftItem;
    private GuiItem goRightItem;
    private void prepareOtherValues() {
        // Left item creation for consumer & bind
        this.goLeftItem = new GuiItem(new ItemStack(Material.RED_TERRACOTTA), event -> {
            HumanEntity viewer = event.getWhoClicked();
            UUID playerUUID = viewer.getUniqueId();
            int page = this.pageMap.getOrDefault(playerUUID, 0);
            this.pageMap.put(playerUUID, page-1);

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
            this.pageMap.put(playerUUID, page+1);

            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            show(viewer);

            viewer.setItemOnCursor(cursor);
        }, CustomAnvil.instance);

        // Create new conflict item
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();

        createMeta.setDisplayName("\u00A7aCreate new conflict");
        createMeta.setLore(Arrays.asList(
                "\u00A77Create a new anvil restriction.",
                "\u00A77You will be asked to name the conflict in chat.",
                "\u00A77Then, you should edit the conflict config as you need"
        ));

        createItem.setItemMeta(createMeta);

        this.backgroundPane.bindItem('C', new GuiItem(createItem, (clickEvent)->{
            clickEvent.setCancelled(true);
            HumanEntity player = clickEvent.getWhoClicked();

            // check permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            player.closeInventory();

            player.sendMessage("\u00A7eWrite the conflict you want to create in the chat.\n" +
                    "\u00A7eOr write \u00A7ccancel \u00A7eto go back to conflict config menu");

            CustomAnvil.Companion.getChatListener().setListenedCallback(player, prepareCreateItemConsumer(player));

        }, CustomAnvil.instance));
    }

    private Consumer<String> prepareCreateItemConsumer(HumanEntity player) {
        AtomicReference<Consumer<String>> selfRef = new AtomicReference<>();
        Consumer<String> selfCallback = (message) -> {
            if(message == null) return;

            message = message.toLowerCase(Locale.ROOT);
            if("cancel".equalsIgnoreCase(message)) {
                player.sendMessage("conflict creation cancelled...");
                show(player);
                return;
            }

            message = message.replace(' ', '_');

            // Try to find if it already exists in a for loop
            // Not the most efficient on large number of conflict, but it should not run often.
            for (EnchantConflictGroup conflict : ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList()) {
                if(conflict.getName().equalsIgnoreCase(message)){
                    player.sendMessage("\u00A7cPlease enter a conflict name that do not already exist...");
                    // wait next message.
                    CustomAnvil.Companion.getChatListener().setListenedCallback(player, selfRef.get());
                    return;
                }
            }

            // Create new empty conflict and display it to the admin
            EnchantConflictGroup conflict = new EnchantConflictGroup(
                    message,
                    new IncludeGroup("new_group"),
                    0);

            ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList().add(conflict);
            updateValueForConflict(conflict, true);

            // save empty conflict in config
            String[] emptyStringArray = new String[0];

            FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();
            config.set(message+".enchantments", emptyStringArray);
            config.set(message+".notAffectedGroups", emptyStringArray);
            config.set(message+".maxEnchantmentBeforeConflict", 0);

            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
                ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            // show the new conflict config to the player
            this.conflictGuiMap.get(conflict).show(player);

        };

        selfRef.set(selfCallback);
        return selfCallback;
    }

    private OutlinePane createEmptyPage(){
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

    public static ItemStack createItemForConflict(EnchantConflictGroup conflict){
        ItemStack item = new ItemStack(conflict.getRepresentativeMaterial());

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(conflict.getName()) + " \u00A7fConflict");
        meta.setLore(Arrays.asList(
                "\u00A77Enchantment count:      \u00A7e"+conflict.getEnchants().size(),
                "\u00A77Group count:            \u00A7e"+conflict.getCantConflictGroup().getGroups().size(),
                "\u00A77Min enchantments count: \u00A7e"+conflict.getMinBeforeBlock()
        ));

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
            // Replace item with the updated one
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
            page = createEmptyPage();
            this.pages.add(page);
        }

        page.addItem(guiItem);
    }

    private void removeFromPage(GuiItem guiItem) {
        // get item page
        OutlinePane page = null;
        int pageID = 0;
        while(pageID < this.pages.size()){
            OutlinePane tempPage = this.pages.get(pageID);
            if(tempPage.getItems().contains(guiItem)){
                page = tempPage;
                break;
            }
            pageID++;
        }

        if(page == null){// Why...
            return;
        }
        removeFromPage(page, pageID, guiItem);
    }

    private void removeFromPage(OutlinePane page, int pageID, GuiItem guiItem) {
        page.removeItem(guiItem);

        // There is now a slot available, let fill it if possible
        if(pageID < (this.pages.size() - 1)){
            OutlinePane newPage = this.pages.get(pageID+1);
            GuiItem nextPageItem = newPage.getItems().get(0);

            removeFromPage(newPage, pageID+1, nextPageItem);

            OutlinePane thisPage = this.pages.get(pageID);
            thisPage.addItem(nextPageItem);
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

    public void placeArrow(int page, boolean customise){

        // Place left arrow
        addPane(this.backgroundPane);
        if(page > 0){
            if(customise){
                ItemStack leftItem = this.goLeftItem.getItem();
                ItemMeta leftMeta = leftItem.getItemMeta();

                leftMeta.setDisplayName("\u00A7eReturn to page " +(page));

                leftItem.setItemMeta(leftMeta);
                this.goLeftItem.setItem(leftItem);
            }

            this.backgroundPane.bindItem('L', this.goLeftItem);
        }else{
            this.backgroundPane.bindItem('L', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);
        }

        // Place right arrow
        if(page < pages.size()-1){
            if(customise){
                ItemStack rightItem = this.goRightItem.getItem();
                ItemMeta rightMeta = rightItem.getItemMeta();

                rightMeta.setDisplayName("\u00A7eGo to page " +(page+2));

                rightItem.setItemMeta(rightMeta);
                this.goRightItem.setItem(rightItem);
            }

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
        placeArrow(pageID, true);
        // and add actual page
        addPane(page);

        // set title
        setTitle("Conflict Config ("+(pageID+1)+"/"+(pages.size())+")");

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

}
