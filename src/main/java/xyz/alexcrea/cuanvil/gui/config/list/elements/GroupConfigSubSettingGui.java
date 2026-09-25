package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.api.ConflictAPI;
import xyz.alexcrea.cuanvil.api.MaterialGroupApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.SelectMaterialContainer;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.MaterialSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NotNullByDefault
public class GroupConfigSubSettingGui extends MappedToListSubSettingGui implements SelectGroupContainer, SelectMaterialContainer {

    private final GroupConfigGui parent;
    private final IncludeGroup group;
    private final PatternPane pane;
    private boolean usable = true;

    public GroupConfigSubSettingGui(
            GroupConfigGui parent,
            IncludeGroup group
    ) {
        super(3, CasedStringUtil.snakeToUpperSpacedCase(group.getName()));
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

    private GuiItem materialSelection;
    private GuiItem groupSelection;

    private void prepareStaticValues() {
        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        assert deleteMeta != null;

        ComponentUtil.setMessageName(deleteMeta, MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_BUTTON_NAME());
        ComponentUtil.applyLore(
                MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_BUTTON_LORE().formatted(),
                deleteMeta
        );

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, openGuiAndCheckAction(), CustomAnvil.instance));

        // Displayed item will be updated later
        var materialSelectionName = MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_SELECTED_MATERIALS();
        var name = CasedStringUtil.snakeToUpperSpacedCase(group.getName());

        ItemStack selectItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta selectItemMeta = selectItem.getItemMeta();
        assert selectItemMeta != null;

        ComponentUtil.setMessageName(selectItemMeta, materialSelectionName, name, null, null);

        selectItem.setItemMeta(selectItemMeta);
        this.materialSelection = new GuiItem(selectItem, (event) -> {
            event.setCancelled(true);
            MaterialSelectSettingGui selectGui = new MaterialSelectSettingGui(this,
                    materialSelectionName, name//TODO MESSAGE maybe need (%page/%max_page)
                    , this);
            selectGui.show(event.getWhoClicked());

        }, CustomAnvil.instance);

        var selectGroupName = MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_SELECTED_SUB_GROUPS();
        ItemStack selectGroup = new ItemStack(Material.CHEST);
        ItemMeta selectGroupMeta = selectGroup.getItemMeta();
        assert selectGroupMeta != null;

        ComponentUtil.setMessageName(selectGroupMeta, selectGroupName, name);

        selectGroup.setItemMeta(selectGroupMeta);
        this.groupSelection = new GuiItem(selectGroup, (event) -> {
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    selectGroupName, name,
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.pane.bindItem('1', this.materialSelection);
        this.pane.bindItem('2', this.groupSelection);
    }

    private Consumer<InventoryClickEvent> openGuiAndCheckAction() {
        ConfirmActionGui deleteGui = createDeleteGui();
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
                return;
            }
            // test if group is used & cancel & warn user if so
            if(testAndWarnIfUsed(player)) return;

            deleteGui.show(player);
        };
    }

    private ConfirmActionGui createDeleteGui() {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return createDeleteGui(lock.get());
        }
    }

    private ConfirmActionGui createDeleteGui(ConfigHolder.ItemGroupConfigHolder holder) {
        Supplier<Boolean> deleteSupplier = () -> {
            // test if group is used & cancel if so
            if(!getUsedLocations(this.group).isEmpty()) return false;

            ItemGroupManager manager = holder.getItemGroupsManager();

            // Remove from manager
            manager.getGroupMap().remove(this.group.getName());

            // Remove from parent
            this.parent.removeGeneric(this.group);

            // Remove self
            cleanAndBeUnusable();

            // Update config file storage
            holder.delete(this.group.getName());

            // Save
            boolean success = true;
            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = holder.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        var type = CasedStringUtil.snakeToUpperSpacedCase(this.group.toString());
        return new ConfirmActionGui(MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_TITLE(), type,
                MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_DESCRIPTION(), type,
                this, this.parent, deleteSupplier
        );
    }

    public boolean testAndWarnIfUsed(HumanEntity player) {
        List<String> usedLoc = getUsedLocations(this.group);
        if(usedLoc.isEmpty()) {
            return false;
        }
        StringBuilder stb = new StringBuilder("<red>Can't delete group " + this.group.getName() +
                "\n<yellow>Used by:");
        int maxIndex = usedLoc.size();
        int nbMore = 0;
        if(maxIndex > 10) {
            nbMore = maxIndex - 9;
            maxIndex = 9;
        }
        for(int i = 0; i < maxIndex; i++) {
            stb.append("\n§r-<yellow> ").append(usedLoc.get(i));
        }
        if(nbMore > 0) {
            stb.append("<red>And ").append(nbMore).append(" More...");
        }

        player.sendMessage(stb.toString());
        return true;
    }

    // return a string containing every instance of where this group is used
    public static List<String> getUsedLocations(AbstractMaterialGroup group) {
        ArrayList<String> usageList = new ArrayList<>();

        // Test used by another group
        try(var lock = ConfigHolder.ITEM_GROUP.read) {
            ItemGroupManager groupManager = lock.get().getItemGroupsManager();
            for(AbstractMaterialGroup otherGroup : groupManager.getGroupMap().values()) {
                if(otherGroup.getGroups().contains(group)) {
                    usageList.add("group " + otherGroup.getName());
                }
            }
        }

        // Test if used for conflict
        try(var lock = ConfigHolder.CONFLICT.read) {
            EnchantConflictManager conflictManager = lock.get().getConflictManager();
            for(EnchantConflictGroup conflict : conflictManager.getConflictList()) {
                if(conflict.getCantConflictGroup().getGroups().contains(group)) {
                    usageList.add("conflict " + conflict);
                }
            }
        }

        return usageList;
    }

    @Override
    public void updateGuiValues() {
        if(!this.usable) return;
        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.group, true);

    }

    @Override
    public void updateLocal() {
        if(!this.usable) return;
        // Prepare material lore
        List<Component> matLore = SelectMaterialContainer.getMaterialLore(this, "group", "include");

        // Prepare group lore
        List<Component> groupLore = SelectGroupContainer.getGroupLore(this, "group", "include");

        // Configure included material setting item
        ItemStack matSelectItem = this.materialSelection.getItem();
        ItemMeta matSelectMeta = matSelectItem.getItemMeta();

        assert matSelectMeta != null;
        matSelectMeta.setDisplayName("<green>Select included <yellow>Materials <green>Settings");//TODO MESSAGE
        ComponentUtil.applyLore(matLore, matSelectMeta);
        matSelectMeta.addItemFlags(ItemFlag.values());

        matSelectItem.setItemMeta(matSelectMeta);

        this.materialSelection.setItem(matSelectItem); // Just in case

        // Configure enchant setting item
        ItemStack groupSelectItem = this.groupSelection.getItem();
        ItemMeta groupSelectMeta = groupSelectItem.getItemMeta();

        assert groupSelectMeta != null;
        groupSelectMeta.setDisplayName("<green>Select included §3Groups <green>Settings");//TODO MESSAGE
        ComponentUtil.applyLore(groupLore, groupSelectMeta);

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
    public void show(HumanEntity player) {
        if(!this.usable) {
            this.parent.show(player);
            return;
        }
        super.show(player);
    }

    // ----------------------------
    // SelectGroupContainer related methods
    // ----------------------------

    @Override
    public Set<AbstractMaterialGroup> getSelectedGroups() {
        return this.group.getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractMaterialGroup> groups) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            var holder = lock.get();

            // update group and referencing groups
            updateGroup(holder, this.group, groups);

            // Save file configuration to disk
            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                return holder.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }
        }

        return true;
    }

    private void updateGroup(ConfigHolder.ItemGroupConfigHolder holder, AbstractMaterialGroup group, Set<AbstractMaterialGroup> groups) {
        // Set live configuration
        group.setGroups(groups);

        // Write to file configuration
        groups = group.getGroups(); // Maybe some group may have been rejected
        String[] groupNames = new String[groups.size()];
        int index = 0;
        for(AbstractMaterialGroup otherGroup : groups) {
            groupNames[index++] = otherGroup.getName();
        }

        holder.getConfig().set(group.getName() + "." + ItemGroupManager.GROUP_LIST_PATH, groupNames);

        // Try to update referencing group. kind of expensive operation in some case.
        updateDirectReferencingGroups(group);

        // We assume a backup & save call will be done soon after
    }

    @Override
    public Set<AbstractMaterialGroup> illegalGroups() {
        Set<AbstractMaterialGroup> illegal = new HashSet<>();

        for(AbstractMaterialGroup otherGroup : MaterialGroupApi.getRegisteredGroupCollection()) {
            if(otherGroup.isReferencing(this.group)) {
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
    public Set<NamespacedKey> getSelectedMaterials() {
        return this.group.getNonGroupInheritedMaterials();
    }

    @Override
    public boolean setSelectedMaterials(Set<NamespacedKey> materials) {
        try(var lock = ConfigHolder.ITEM_GROUP.write) {
            return setSelectedMaterials(lock.get(), materials);
        }
    }

    private boolean setSelectedMaterials(ConfigHolder.ItemGroupConfigHolder holder, Set<NamespacedKey> materials) {
        this.group.setNonGroupInheritedMaterials(materials);

        // Write to file configuration
        String[] groupNames = new String[materials.size()];
        int index = 0;
        for(NamespacedKey otherGroup : materials) {
            groupNames[index++] = otherGroup.getKey().toLowerCase();
        }

        holder.getConfig().set(this.group.getName() + "." + ItemGroupManager.MATERIAL_LIST_PATH, groupNames);

        // update referencing groups
        updateDirectReferencingGroups(this.group);

        // Save file configuration to disk
        if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return holder.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }
        return true;
    }

    @Override
    public Set<NamespacedKey> illegalMaterials() {
        return Set.of(Material.AIR.getKey());
    }

    // ----------------------------
    // End of SelectMaterialContainer related methods
    // ----------------------------

    private void updateDirectReferencingGroups(AbstractMaterialGroup referenceTo) {
        var everyStoredGroups = MaterialGroupApi.getRegisteredGroupCollection();
        var everyConflicts = ConflictAPI.getRegisteredConflict();

        HashSet<AbstractMaterialGroup> toUpdate = new HashSet<>();
        HashSet<AbstractMaterialGroup> updateFuture = new HashSet<>();
        HashSet<AbstractMaterialGroup> conflictGroupPlanned = new HashSet<>();

        updateFuture.add(referenceTo);
        while(!updateFuture.isEmpty()) {
            HashSet<AbstractMaterialGroup> temp = updateFuture;
            updateFuture = toUpdate;
            updateFuture.clear();
            toUpdate = temp;

            for(AbstractMaterialGroup testGroup : toUpdate) {
                // Update other stored group
                for(AbstractMaterialGroup otherGroup : everyStoredGroups) {
                    if(otherGroup.getGroups().contains(testGroup)) {
                        otherGroup.updateMaterials();
                        updateFuture.add(otherGroup);
                    }
                }

                // plan update for conflict groups
                for(EnchantConflictGroup everyConflict : everyConflicts) {
                    AbstractMaterialGroup conflictGroup = everyConflict.getCantConflictGroup();
                    if(conflictGroup.getGroups().contains(testGroup)) {
                        conflictGroupPlanned.add(conflictGroup);
                    }
                }

                // Update parent & local by extension
                if(testGroup instanceof IncludeGroup) {
                    this.parent.updateValueForGeneric((IncludeGroup) testGroup, false);
                }
            }
        }
        this.parent.update();

        // Update conflict group
        //TODO #130 part 3 should make this outdated
        for(AbstractMaterialGroup conflictGroup : conflictGroupPlanned) {
            conflictGroup.updateMaterials();
        }

    }

}
