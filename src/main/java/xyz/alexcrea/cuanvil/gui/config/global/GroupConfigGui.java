package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
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
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class GroupConfigGui extends MappedGuiListConfigGui<IncludeGroup, GroupConfigSubSettingGui> {

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
        super("Group Config");

        init();
    }

    @Override
    protected ItemStack createItemForGeneric(IncludeGroup group) {
        ItemStack item = new ItemStack(group.getRepresentativeMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§e" + CasedStringUtil.snakeToUpperSpacedCase(group.getName())+ " §fGroup");
        meta.setLore(Arrays.asList(
                "§7Number of selected groups : " + group.getGroups().size(),
                "§7Number of included material : " + group.getNonGroupInheritedMaterials().size(),
                "",
                "§7Total number of included material "+group.getMaterials().size()));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Collection<IncludeGroup> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<IncludeGroup> includeGroups = new ArrayList<>();

        for (AbstractMaterialGroup group : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(group instanceof IncludeGroup){
                includeGroups.add((IncludeGroup) group);
            }
        }
        return includeGroups;
    }

    @Override
    protected GroupConfigSubSettingGui newInstanceOfGui(IncludeGroup group, GuiItem item) {
        return new GroupConfigSubSettingGui(this, group, item);
    }

    @Override
    protected String genericDisplayedName() {
        return "material group";
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
