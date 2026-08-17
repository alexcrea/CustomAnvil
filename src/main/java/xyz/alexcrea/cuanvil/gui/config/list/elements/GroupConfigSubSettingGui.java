package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.*;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GroupConfigSubSettingGui extends MappedToListSubSettingGui implements SelectGroupContainer, SelectMaterialContainer {

    private final GroupConfigGui parent;
    private final IncludeGroup group;
    private final PatternPane pane;
    private boolean usable = true;

    public GroupConfigSubSettingGui(
            @NotNull GroupConfigGui parent,
            @NotNull IncludeGroup group) {
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

        ComponentUtil.INSTANCE.setMessageName(deleteMeta, MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_BUTTON_NAME());
        ComponentUtil.INSTANCE.applyLore(
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

        ComponentUtil.INSTANCE.setMessageName(selectItemMeta, materialSelectionName, name);

        selectItem.setItemMeta(selectItemMeta);
        this.materialSelection = new GuiItem(selectItem, (event) -> {
            event.setCancelled(true);
            MaterialSelectSettingGui selectGui = new MaterialSelectSettingGui(this,
                    materialSelectionName, name
                    , this);
            selectGui.show(event.getWhoClicked());

        }, CustomAnvil.instance);

        var selectGroupName = MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_SELECTED_SUB_GROUPS();
        ItemStack selectGroup = new ItemStack(Material.CHEST);
        ItemMeta selectGroupMeta = selectGroup.getItemMeta();
        assert selectGroupMeta != null;

        ComponentUtil.INSTANCE.setMessageName(selectGroupMeta, selectGroupName, name);

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

    private @NotNull Consumer<InventoryClickEvent> openGuiAndCheckAction() {
        ConfirmActionGui deleteGui = createDeleteGui();
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
                return;
            }
            // test if group is used & cancel & warn user if so
            if(testAndWarnIfUsed(player)) return;

            deleteGui.show(player);
        };
    }

    private @NotNull ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            // test if group is used & cancel if so
            if(!getUsedLocations(this.group).isEmpty()) return false;

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

        var type = CasedStringUtil.snakeToUpperSpacedCase(this.group.toString());
        return new ConfirmActionGui(MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_TITLE(), type,
                MsgUI.INSTANCE.getMATERIAL_GROUP_ELEMENT_DELETE_DESCRIPTION(), type,
                this, this.parent, deleteSupplier
        );
    }

    public boolean testAndWarnIfUsed(HumanEntity player){
        List<String> usedLoc = getUsedLocations(this.group);
        if(usedLoc.isEmpty()){
            return false;
        }
        StringBuilder stb = new StringBuilder("§cCan't delete group " +this.group.getName()+
                "\n§eUsed by:");
        int maxIndex = usedLoc.size();
        int nbMore = 0;
        if(maxIndex > 10){
            nbMore = maxIndex - 9;
            maxIndex = 9;
        }
        for (int i = 0; i < maxIndex; i++) {
            stb.append("\n§r-§e ").append(usedLoc.get(i));
        }
        if(nbMore > 0){
            stb.append("§cAnd ").append(nbMore).append(" More...");
        }

        player.sendMessage(stb.toString());
        return true;
    }

    // return a string containing every instance of where this group is used
    public static List<String> getUsedLocations(AbstractMaterialGroup group){
        ArrayList<String> usageList = new ArrayList<>();

        // Test used by another group
        ItemGroupManager groupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        for (AbstractMaterialGroup otherGroup : groupManager.getGroupMap().values()) {
            if(otherGroup.getGroups().contains(group)) {
                usageList.add("group " + otherGroup.getName());
            }
        }

        // Test if used for conflict
        EnchantConflictManager conflictManager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();
        for (EnchantConflictGroup conflict : conflictManager.getConflictList()) {
            if(conflict.getCantConflictGroup().getGroups().contains(group)) {
                usageList.add("conflict " + conflict);
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
        List<String> matLore = SelectMaterialContainer.getMaterialLore(this, "group", "include");

        // Prepare group lore
        List<String> groupLore = SelectGroupContainer.getGroupLore(this, "group", "include");

        // Configure included material setting item
        ItemStack matSelectItem = this.materialSelection.getItem();
        ItemMeta matSelectMeta = matSelectItem.getItemMeta();

        assert matSelectMeta != null;
        matSelectMeta.setDisplayName("§aSelect included §eMaterials §aSettings");//TODO MESSAGE
        matSelectMeta.setLore(matLore);
        matSelectMeta.addItemFlags(ItemFlag.values());

        matSelectItem.setItemMeta(matSelectMeta);

        this.materialSelection.setItem(matSelectItem); // Just in case

        // Configure enchant setting item
        ItemStack groupSelectItem = this.groupSelection.getItem();
        ItemMeta groupSelectMeta = groupSelectItem.getItemMeta();

        assert groupSelectMeta != null;
        groupSelectMeta.setDisplayName("§aSelect included §3Groups §aSettings");//TODO MESSAGE
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
        // update group and referencing groups
        updateGroup(this.group, groups);

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    private void updateGroup(@NotNull AbstractMaterialGroup group, Set<AbstractMaterialGroup> groups){
        // Set live configuration
        group.setGroups(groups);

        // Write to file configuration
        groups = group.getGroups(); // Maybe some group may have been rejected
        String[] groupNames = new String[groups.size()];
        int index = 0;
        for (AbstractMaterialGroup otherGroup : groups) {
            groupNames[index++] = otherGroup.getName();
        }

        ConfigHolder.ITEM_GROUP_HOLDER.getConfig().set(group.getName()+"."+ItemGroupManager.GROUP_LIST_PATH, groupNames);

        // Try to update referencing group. kind of expensive operation in some case.
        updateDirectReferencingGroups(group);

        // We assume a backup & save call will be done soon after
    }

    @Override
    public Set<AbstractMaterialGroup> illegalGroups() {
        Set<AbstractMaterialGroup> illegal = new HashSet<>();

        for (AbstractMaterialGroup otherGroup : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(otherGroup.isReferencing(this.group)){
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
        this.group.setNonGroupInheritedMaterials(materials);

        // Write to file configuration
        String[] groupNames = new String[materials.size()];
        int index = 0;
        for (NamespacedKey otherGroup : materials) {
            groupNames[index++] = otherGroup.getKey().toLowerCase();
        }

        ConfigHolder.ITEM_GROUP_HOLDER.getConfig().set(this.group.getName()+"."+ItemGroupManager.MATERIAL_LIST_PATH, groupNames);

        // update referencing groups
        updateDirectReferencingGroups(this.group);

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
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

    private void updateDirectReferencingGroups(AbstractMaterialGroup referenceTo){
        Collection<AbstractMaterialGroup> everyStoredGroups = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values();
        List<EnchantConflictGroup> everyConflicts = ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList();

        HashSet<AbstractMaterialGroup> toUpdate = new HashSet<>();
        HashSet<AbstractMaterialGroup> updateFuture = new HashSet<>();
        HashSet<AbstractMaterialGroup> conflictGroupPlanned = new HashSet<>();

        updateFuture.add(referenceTo);
        while (!updateFuture.isEmpty()){
            HashSet<AbstractMaterialGroup> temp = updateFuture;
            updateFuture = toUpdate;
            updateFuture.clear();
            toUpdate = temp;

            for (AbstractMaterialGroup testGroup : toUpdate) {
                // Update other stored group
                for (AbstractMaterialGroup otherGroup : everyStoredGroups) {
                    if(otherGroup.getGroups().contains(testGroup)){
                        otherGroup.updateMaterials();
                        updateFuture.add(otherGroup);
                    }
                }

                // plan update for conflict groups
                for (EnchantConflictGroup everyConflict : everyConflicts) {
                    AbstractMaterialGroup conflictGroup = everyConflict.getCantConflictGroup();
                    if(conflictGroup.getGroups().contains(testGroup)){
                        conflictGroupPlanned.add(conflictGroup);
                    }
                }

                // Update parent & local by extension
                if(testGroup instanceof IncludeGroup){
                    this.parent.updateValueForGeneric((IncludeGroup) testGroup, false);
                }
            }
        }
        this.parent.update();

        // Update conflict group
        for (AbstractMaterialGroup conflictGroup : conflictGroupPlanned) {
            conflictGroup.updateMaterials();
        }

    }

}
