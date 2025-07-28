package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.*;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.SelectItemTypeContainer;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.ItemTypeSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class GroupConfigSubSettingGui extends MappedToListSubSettingGui implements SelectGroupContainer, SelectItemTypeContainer {

    private final GroupConfigGui parent;
    private final IncludeItemTypeGroup group;
    private final PatternPane pane;
    private boolean usable = true;

    public GroupConfigSubSettingGui(
            @NotNull GroupConfigGui parent,
            @NotNull IncludeItemTypeGroup group) {
        super(3,
                "§e" + CasedStringUtil.snakeToUpperSpacedCase(group.getName()) + " §rConfig");
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

    private GuiItem itemSelection;
    private GuiItem groupSelection;

    private void prepareStaticValues() {
        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = ItemType.RED_TERRACOTTA.createItemStack();
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("§4DELETE GROUP");
        deleteMeta.setLore(Collections.singletonList("§cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, openGuiAndCheckAction(), CustomAnvil.instance));

        // Displayed item will be updated later
        String selectionName = "§e" + CasedStringUtil.snakeToUpperSpacedCase(group.getName()) + " §rItems";
        ItemStack selectItem = ItemType.DIAMOND_SWORD.createItemStack();
        ItemMeta selectItemMeta = selectItem.getItemMeta();
        selectItemMeta.setDisplayName(selectionName);

        selectItem.setItemMeta(selectItemMeta);
        this.itemSelection = new GuiItem(selectItem, (event) -> {
            event.setCancelled(true);
            ItemTypeSelectSettingGui selectGui = new ItemTypeSelectSettingGui(this,
                    selectionName
                    , this);
            selectGui.show(event.getWhoClicked());

        }, CustomAnvil.instance);

        String selectGroupName = "§e" + CasedStringUtil.snakeToUpperSpacedCase(this.group.getName()) + " §rGroups";
        ItemStack selectGroup = ItemType.CHEST.createItemStack();
        ItemMeta selectGroupMeta = selectGroup.getItemMeta();
        selectGroupMeta.setDisplayName(selectGroupName);

        selectGroup.setItemMeta(selectGroupMeta);
        this.groupSelection = new GuiItem(selectGroup, (event) -> {
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    selectGroupName,
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.pane.bindItem('1', this.itemSelection);
        this.pane.bindItem('2', this.groupSelection);
    }

    private @NotNull Consumer<InventoryClickEvent> openGuiAndCheckAction() {
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
            // test if group is used & cancel & warn user if so
            if (testAndWarnIfUsed(player)) return;

            deleteGui.show(player);
        };
    }

    private @NotNull ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            // test if group is used & cancel if so
            if (!getUsedLocations(this.group).isEmpty()) return false;

            ItemGroupManager manager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();

            // Remove from manager
            manager.getGroupMap().remove(this.group.getName());

            // Remove from parent
            this.parent.removeGeneric(this.group);

            // Remove self
            cleanAndBeUnusable();

            // Update config file storage
            ConfigHolder.CUSTOM_RECIPE_HOLDER.delete(this.group.getName());

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("§cDelete §e" + CasedStringUtil.snakeToUpperSpacedCase(this.group.toString()) + "§c?",
                "§7Confirm that you want to delete this group.",
                this, this.parent, deleteSupplier
        );
    }

    public boolean testAndWarnIfUsed(HumanEntity player) {
        List<String> usedLoc = getUsedLocations(this.group);
        if (usedLoc.isEmpty()) {
            return false;
        }
        StringBuilder stb = new StringBuilder("§cCan't delete group " + this.group.getName() +
                "\n§eUsed by:");
        int maxIndex = usedLoc.size();
        int nbMore = 0;
        if (maxIndex > 10) {
            nbMore = maxIndex - 9;
            maxIndex = 9;
        }
        for (int i = 0; i < maxIndex; i++) {
            stb.append("\n§r-§e ").append(usedLoc.get(i));
        }
        if (nbMore > 0) {
            stb.append("§cAnd ").append(nbMore).append(" More...");
        }

        player.sendMessage(stb.toString());
        return true;
    }

    // return a string containing every instance of where this group is used
    public static List<String> getUsedLocations(AbstractItemTypeGroup group) {
        ArrayList<String> usageList = new ArrayList<>();

        // Test used by another group
        ItemGroupManager groupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        for (AbstractItemTypeGroup otherGroup : groupManager.getGroupMap().values()) {
            if (otherGroup.getGroups().contains(group)) {
                usageList.add("group " + otherGroup.getName());
            }
        }

        // Test if used for conflict
        EnchantConflictManager conflictManager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();
        for (EnchantConflictGroup conflict : conflictManager.getConflictList()) {
            if (conflict.getCantConflictGroup().getGroups().contains(group)) {
                usageList.add("conflict " + conflict);
            }
        }

        return usageList;
    }

    @Override
    public void updateGuiValues() {
        if (!this.usable) return;
        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.group, true);

    }

    @Override
    public void updateLocal() {
        if (!this.usable) return;
        // Prepare material lore
        List<String> matLore = SelectItemTypeContainer.getItemLore(this, "group", "include");

        // Prepare group lore
        List<String> groupLore = SelectGroupContainer.getGroupLore(this, "group", "include");

        // Configure included material setting item
        ItemStack matSelectItem = this.itemSelection.getItem();
        ItemMeta matSelectMeta = matSelectItem.getItemMeta();

        matSelectMeta.setDisplayName("§aSelect included §eMaterials §aSettings");
        matSelectMeta.setLore(matLore);
        matSelectMeta.addItemFlags(ItemFlag.values());

        matSelectItem.setItemMeta(matSelectMeta);

        this.itemSelection.setItem(matSelectItem); // Just in case

        // Configure enchant setting item
        ItemStack groupSelectItem = this.groupSelection.getItem();
        ItemMeta groupSelectMeta = groupSelectItem.getItemMeta();

        groupSelectMeta.setDisplayName("§aSelect included §3Groups §aSettings");
        groupSelectMeta.setLore(groupLore);

        groupSelectItem.setItemMeta(groupSelectMeta);

        this.groupSelection.setItem(groupSelectItem); // Just in case
    }

    @Override
    public void cleanAndBeUnusable() {
        this.usable = false;
        this.pane.bindItem('1', GuiGlobalItems.backgroundItem());
        this.pane.bindItem('2', GuiGlobalItems.backgroundItem());
        this.pane.bindItem('D', GuiGlobalItems.backgroundItem());

    }

    @Override
    public void show(@NotNull HumanEntity player) {
        if (!this.usable) {
            this.parent.show(player);
            return;
        }
        super.show(player);
    }

    // ----------------------------
    // SelectGroupContainer related methods
    // ----------------------------

    @Override
    public Set<AbstractItemTypeGroup> getSelectedGroups() {
        return this.group.getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractItemTypeGroup> groups) {
        // update group and referencing groups
        updateGroup(this.group, groups);

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    private void updateGroup(@NotNull AbstractItemTypeGroup group, Set<AbstractItemTypeGroup> groups) {
        // Set live configuration
        group.setGroups(groups);

        // Write to file configuration
        groups = group.getGroups(); // Maybe some group may have been rejected
        String[] groupNames = new String[groups.size()];
        int index = 0;
        for (AbstractItemTypeGroup otherGroup : groups) {
            groupNames[index++] = otherGroup.getName();
        }

        ConfigHolder.ITEM_GROUP_HOLDER.getConfig().set(group.getName() + "." + ItemGroupManager.GROUP_LIST_PATH, groupNames);

        // Try to update referencing group. kind of expensive operation in some case.
        updateDirectReferencingGroups(group);

        // We assume a backup & save call will be done soon after
    }

    @Override
    public Set<AbstractItemTypeGroup> illegalGroups() {
        Set<AbstractItemTypeGroup> illegal = new HashSet<>();

        for (AbstractItemTypeGroup otherGroup : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if (otherGroup.isReferencing(this.group)) {
                illegal.add(otherGroup);
            }
        }
        illegal.add(this.group);

        return illegal;
    }

    // ----------------------------
    // End of SelectGroupContainer related methods
    // ----------------------------
    // SelectMaterialContainer related methods
    // ----------------------------

    @Override
    public Set<ItemType> getSelectedItems() {
        return this.group.getNonGroupInheritedItemTypes();
    }

    @Override
    public boolean setSelectedItems(Set<ItemType> types) {
        this.group.setNonGroupInheritedItemTypes(types);

        // Write to file configuration
        String[] groupNames = new String[types.size()];
        int index = 0;
        for (ItemType otherGroup : types) {
            groupNames[index++] = otherGroup.key().value().toLowerCase();
        }

        ConfigHolder.ITEM_GROUP_HOLDER.getConfig().set(this.group.getName() + "." + ItemGroupManager.ITEMS_LIST_PATH, groupNames);

        // update referencing groups
        updateDirectReferencingGroups(this.group);

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    private static final Set<ItemType> ONLY_AIR_ITEM_SET;

    static {
        Set<ItemType> onlyAir = new HashSet<>();
        onlyAir.add(ItemType.AIR);
        ONLY_AIR_ITEM_SET = Collections.unmodifiableSet(onlyAir);
    }

    @Override
    public Set<ItemType> illegalItems() {
        return ONLY_AIR_ITEM_SET;
    }

    // ----------------------------
    // End of SelectMaterialContainer related methods
    // ----------------------------

    private void updateDirectReferencingGroups(AbstractItemTypeGroup referenceTo) {
        Collection<AbstractItemTypeGroup> everyStoredGroups = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values();
        List<EnchantConflictGroup> everyConflicts = ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList();

        HashSet<AbstractItemTypeGroup> toUpdate = new HashSet<>();
        HashSet<AbstractItemTypeGroup> updateFuture = new HashSet<>();
        HashSet<AbstractItemTypeGroup> conflictGroupPlanned = new HashSet<>();

        updateFuture.add(referenceTo);
        while (!updateFuture.isEmpty()) {
            HashSet<AbstractItemTypeGroup> temp = updateFuture;
            updateFuture = toUpdate;
            updateFuture.clear();
            toUpdate = temp;

            for (AbstractItemTypeGroup testGroup : toUpdate) {
                // Update other stored group
                for (AbstractItemTypeGroup otherGroup : everyStoredGroups) {
                    if (otherGroup.getGroups().contains(testGroup)) {
                        otherGroup.update();
                        updateFuture.add(otherGroup);
                    }
                }

                // plan update for conflict groups
                for (EnchantConflictGroup everyConflict : everyConflicts) {
                    AbstractItemTypeGroup conflictGroup = everyConflict.getCantConflictGroup();
                    if (conflictGroup.getGroups().contains(testGroup)) {
                        conflictGroupPlanned.add(conflictGroup);
                    }
                }

                // Update parent & local by extension
                if (testGroup instanceof IncludeItemTypeGroup) {
                    this.parent.updateValueForGeneric((IncludeItemTypeGroup) testGroup, false);
                }
            }
        }
        this.parent.update();

        // Update conflict group
        for (AbstractItemTypeGroup conflictGroup : conflictGroupPlanned) {
            conflictGroup.update();
        }

    }

}
