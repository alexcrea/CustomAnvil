package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import java.util.List;

public class GroupConfigGui extends MappedGuiListConfigGui<IncludeGroup, GroupConfigSubSettingGui> {

    public final static GroupConfigGui INSTANCE = new GroupConfigGui();

    static {
        INSTANCE.init();
    }

    public GroupConfigGui() {
        super("Group Config");
    }

    @Override
    protected ItemStack createItemForGeneric(IncludeGroup group) {
        ItemStack item = new ItemStack(group.getRepresentativeMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(group.getName())+ " \u00A7fGroup");
        meta.setLore(Arrays.asList(
                "\u00A77Number of selected groups : " + group.getGroups().size(),
                "\u00A77Number of included material: " + group.getNonGroupInheritedMaterials().size(),
                "",
                "\u00A77Total number of included material "+group.getMaterials().size()));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected List<IncludeGroup> getEveryDisplayableInstanceOfGeneric() {
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
