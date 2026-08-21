package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.GroupType;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.GroupConfigSubSettingGui;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.Collection;

public class GroupConfigGui extends MappedGuiListConfigGui<IncludeGroup, MappedGuiListConfigGui.LazyElement<GroupConfigSubSettingGui>> {

    private static GroupConfigGui INSTANCE;

    @Nullable
    public static GroupConfigGui getCurrentInstance(){
        return INSTANCE;
    }

    @NotNull
    public static GroupConfigGui getInstance(){
        if(INSTANCE == null) INSTANCE = new GroupConfigGui();

        return INSTANCE;
    }

    public GroupConfigGui() {
        super(MsgUI.INSTANCE.getMATERIAL_GROUP_TITLE());

        init();
    }

    public GroupConfigGui(Gui parent) {
        super(MsgUI.INSTANCE.getMATERIAL_GROUP_TITLE(), parent);
    }

    @Override
    protected ItemStack createItemForGeneric(IncludeGroup group) {
        ItemStack item = new ItemStack(group.getRepresentativeMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        ComponentUtil.INSTANCE.setMessageName(
                meta,
                MsgUI.INSTANCE.getMATERIAL_GROUP_NAME(),
                CasedStringUtil.snakeToUpperSpacedCase(group.getName())
        );
        ComponentUtil.INSTANCE.applyLore(
                MsgUI.INSTANCE.getMATERIAL_GROUP_LORE().formatted(
                        group.getGroups().size(),
                        group.getNonGroupInheritedMaterials().size(),
                        group.getMaterials().size()
                ),
                meta
        );

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Collection<IncludeGroup> getEveryInstanceOfGeneric() {
        ArrayList<IncludeGroup> includeGroups = new ArrayList<>();

        for (AbstractMaterialGroup group : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(group instanceof IncludeGroup){
                includeGroups.add((IncludeGroup) group);
            }
        }
        return includeGroups;
    }

    @Override
    protected LazyElement<GroupConfigSubSettingGui> newInstanceOfGui(IncludeGroup group, GuiItem item) {
        return new LazyElement<>(item, () -> new GroupConfigSubSettingGui(this, group));
    }

    @Override
    protected Message genericDisplayedName() {
        return MsgUI.INSTANCE.getMATERIAL_GROUP_GENERIC_NAME();
    }

    @Override
    protected IncludeGroup createAndSaveNewEmptyGeneric(String name) {
        ItemGroupManager manager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        if(manager.getGroupMap().containsKey(name)) return null;

        ConfigurationSection config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        config.set(name+"."+ItemGroupManager.GROUP_TYPE_PATH, GroupType.INCLUDE.getGroupID());

        return (IncludeGroup) manager.createGroup(config, name);
    }

}
