package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.config.SelectItemTypeContainer;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.list.MappedElementListConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class MaterialSelectSettingGui extends MappedElementListConfigGui<ItemType, GuiItem> {

    private final SelectItemTypeContainer selector;
    private final Gui backGui;
    private boolean instantRemove;

    private final List<ItemType> defaultMaterials;
    private final Set<ItemType> illegalMaterials;
    private final int defaultMaterialHash;
    private int nowMaterialHash;

    public MaterialSelectSettingGui(
            @NotNull SelectItemTypeContainer selector,
            @NotNull String title,
            @NotNull Gui backGui) {
        super(title);
        this.selector = selector;
        this.backGui = backGui;
        this.instantRemove = false;

        this.defaultMaterials = new ArrayList<>(this.selector.getSelectedMaterials());
        this.illegalMaterials = this.selector.illegalMaterials();

        this.defaultMaterialHash = hashFromItemTypeList(this.defaultMaterials);
        this.nowMaterialHash = this.defaultMaterialHash;

        init();

        // Change back item
        this.backgroundPane.bindItem('B', GuiGlobalItems.backItem(backGui));
    }

    @Override
    protected Pattern getBackgroundPattern() {
        return new Pattern(
                GuiSharedConstant.UPPER_FILLER_FULL_PLANE,
                GuiSharedConstant.EMPTY_FILLER_FULL_LINE,
                GuiSharedConstant.EMPTY_FILLER_FULL_LINE,
                GuiSharedConstant.EMPTY_FILLER_FULL_LINE,
                GuiSharedConstant.EMPTY_FILLER_FULL_LINE,
                "BT1LAR1IS"
        );
    }

    private GuiItem saveItem;
    private GuiItem noChangeItem;

    private GuiItem instantRemoveOn;
    private GuiItem instantRemoveOff;

    @Override
    protected void prepareStaticValues() {
        super.prepareStaticValues();

        // Temporary leave item
        GuiItem temporaryLeave = GuiGlobalItems.temporaryCloseGuiToSelectItem(Material.YELLOW_STAINED_GLASS_PANE, this);
        this.backgroundPane.bindItem('T', temporaryLeave);

        // Select new mat item
        ItemStack selectItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta selectMeta = selectItem.getItemMeta();
        assert selectMeta != null;

        selectMeta.setDisplayName("§aAdd Item");
        selectMeta.setLore(Arrays.asList(
                "§7Click here with an item to add",
                "§7it's Material to the list."));

        selectItem.setItemMeta(selectMeta);

        this.backgroundPane.bindItem('A', new GuiItem(selectItem, setItemAsCursor(), CustomAnvil.instance));

        // Save item
        this.saveItem = prepareSaveItem();

        this.noChangeItem = GuiGlobalItems.noChangeItem();
        this.backgroundPane.bindItem('S', this.noChangeItem);

        // Instant Remove On item
        ItemStack instantRemoveOnItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta instantRemoveOnMeta = instantRemoveOnItem.getItemMeta();
        assert instantRemoveOnMeta != null;

        instantRemoveOnMeta.setDisplayName("§eInstant remove is §aEnabled §e!");
        instantRemoveOnMeta.setLore(
                Collections.singletonList("§7Click here to disable the instant remove"));

        instantRemoveOnItem.setItemMeta(instantRemoveOnMeta);

        // Instant Remove Off item
        ItemStack instantRemoveOffItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta instantRemoveOffMeta = instantRemoveOffItem.getItemMeta();
        assert instantRemoveOffMeta != null;

        instantRemoveOffMeta.setDisplayName("§eInstant remove is §cDisabled §e!");
        instantRemoveOffMeta.setLore(
                Collections.singletonList("§7Click here to enable the instant remove"));

        instantRemoveOffItem.setItemMeta(instantRemoveOffMeta);

        // Instant Remove gui items
        this.instantRemoveOn = new GuiItem(instantRemoveOnItem, (event -> {
            this.instantRemove = false;
            this.backgroundPane.bindItem('I', this.instantRemoveOff);
            update();
        }), CustomAnvil.instance);

        this.instantRemoveOff = new GuiItem(instantRemoveOffItem, (event -> {
            this.instantRemove = true;
            this.backgroundPane.bindItem('I', this.instantRemoveOn);
            update();
        }), CustomAnvil.instance);

        this.backgroundPane.bindItem('I', this.instantRemoveOff);
    }

    private GuiItem prepareSaveItem() {
        ItemStack saveItemStack = new ItemStack(GuiGlobalItems.DEFAULT_SAVE_ITEM);
        ItemMeta saveMeta = saveItemStack.getItemMeta();
        assert saveMeta != null;

        saveMeta.setDisplayName("§aSave");

        saveItemStack.setItemMeta(saveMeta);

        return new GuiItem(saveItemStack, event -> {
            event.setCancelled(true);

            HumanEntity player = event.getWhoClicked();
            // Do not allow to save configuration if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            if (testCantSave()) return;


            // Save setting
            Set<ItemType> result = new HashSet<>(this.elementGuiMap.keySet());

            if (!this.selector.setSelectedItems(result)) {
                player.sendMessage("§cSomething went wrong while saving the change of value.");
            }

            // Return to parent
            this.backGui.show(player);

        }, CustomAnvil.instance);
    }

    /**
     * @return A consumer to update the current setting's value.
     */
    protected Consumer<InventoryClickEvent> setItemAsCursor() {
        return event -> {
            event.setCancelled(true);

            HumanEntity player = event.getWhoClicked();
            ItemStack cursor = player.getItemOnCursor();

            // Test if cursor material allowed
            ItemType cursorMat = cursor.getType().asItemType();
            if (cursorMat == ItemType.AIR) return;
            if (this.illegalMaterials.contains(cursorMat)) return;

            // Update gui only if item did not exist before.
            if (!this.elementGuiMap.containsKey(cursorMat)) {
                updateValueForGeneric(cursorMat, true);
                this.nowMaterialHash ^= cursorMat.hashCode();

                setSaveItem();
                update();
            }
        };
    }

    @Override
    protected ItemStack createItemForGeneric(ItemType type) {
        ItemStack item = type.createItemStack();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;
        meta.setDisplayName("§a" + CasedStringUtil.snakeToUpperSpacedCase(type.key().value().toLowerCase()));
        meta.setLore(Collections.singletonList("§7Click here to remove this material from the list"));
        meta.addItemFlags(ItemFlag.values());

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected Collection<ItemType> getEveryDisplayableInstanceOfGeneric() {
        return this.defaultMaterials;
    }

    @Override
    protected void updateElement(ItemType type, GuiItem element) {
        // Nothing happen here I think
    }

    @Override
    protected GuiItem newElementRequested(ItemType type, GuiItem newItem) {
        newItem.setAction(event -> {
            if (this.instantRemove) {
                removeItemType(type);
            } else {
                String materialName = CasedStringUtil.snakeToUpperSpacedCase(type.key().value().toLowerCase());

                // Create and show confirm remove gui.
                ConfirmActionGui confirmGui = new ConfirmActionGui(
                        "Remove " + materialName,
                        "§7Confirm Remove " + materialName.toLowerCase() + " from this list.",
                        this, this,
                        () -> {
                            removeItemType(type);
                            return true;
                        }, false
                );
                confirmGui.show(event.getWhoClicked());

            }
        });
        return newItem;
    }

    private void removeItemType(ItemType type) {
        if (this.elementGuiMap.containsKey(type)) {
            this.nowMaterialHash ^= type.hashCode(); //TODO check would this be valid with item type
            setSaveItem();
            removeGeneric(type);
        }
    }

    @Override
    protected GuiItem findItemFromElement(ItemType type, GuiItem element) {
        return element;
    }

    @Override
    protected GuiItem findGuiItemForRemoval(ItemType type, GuiItem element) {
        return element;
    }

    private static int hashFromItemTypeList(List<ItemType> itemTypeList) {
        int defaultMaterialHash = 0;
        for (ItemType type : itemTypeList) {
            defaultMaterialHash ^= type.hashCode(); //TODO check would this be valid with item type
        }
        return defaultMaterialHash;
    }

    private void setSaveItem() {
        if (testCantSave()) {
            this.backgroundPane.bindItem('S', this.noChangeItem);
        } else {
            this.backgroundPane.bindItem('S', this.saveItem);
        }

    }

    private boolean testCantSave() {
        return this.defaultMaterialHash == this.nowMaterialHash;
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
