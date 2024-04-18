package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.ConflictSubSettingGui;

import java.util.ArrayList;
import java.util.List;

public class GroupConfigGui extends MappedGuiListConfigGui<AbstractMaterialGroup, ConflictSubSettingGui> {

    public final static GroupConfigGui INSTANCE = new GroupConfigGui();

    static {
        INSTANCE.init();
    }

    public GroupConfigGui() {
        super("Group Config");
    }

    @Override
    protected ItemStack createItemForGeneric(AbstractMaterialGroup group) {
        ItemStack item = new ItemStack(group.getRepresentativeMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(group.getName());

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected List<AbstractMaterialGroup> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<AbstractMaterialGroup> includeGroups = new ArrayList<>();

        for (AbstractMaterialGroup group : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(group instanceof IncludeGroup){
                includeGroups.add(group);
            }
        }
        return includeGroups;
    }

    @Override
    protected ConflictSubSettingGui newInstanceOfGui(AbstractMaterialGroup group, GuiItem item) {
        return new ConflictSubSettingGui(this, group, item);
    }

    @Override
    protected String genericDisplayedName() {
        return "material group";
    }

    @Override
    protected AbstractMaterialGroup createAndSaveNewEmptyGeneric(String name) {

        return null;
    }

}
