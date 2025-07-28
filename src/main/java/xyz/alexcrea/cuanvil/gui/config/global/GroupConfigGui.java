package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractItemTypeGroup;
import xyz.alexcrea.cuanvil.group.GroupType;
import xyz.alexcrea.cuanvil.group.IncludeItemTypeGroup;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.GroupConfigSubSettingGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class GroupConfigGui extends MappedGuiListConfigGui<IncludeItemTypeGroup, MappedGuiListConfigGui.LazyElement<GroupConfigSubSettingGui>> {

    private static GroupConfigGui INSTANCE;

    @Nullable
    public static GroupConfigGui getCurrentInstance() {
        return INSTANCE;
    }

    @NotNull
    public static GroupConfigGui getInstance() {
        if (INSTANCE == null) INSTANCE = new GroupConfigGui();

        return INSTANCE;
    }

    public GroupConfigGui() {
        super("Group Config");

        init();
    }

    @Override
    protected ItemStack createItemForGeneric(IncludeItemTypeGroup group) {
        ItemStack item = group.getRepresentativeItem().createItemStack();
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§e" + CasedStringUtil.snakeToUpperSpacedCase(group.getName()) + " §fGroup");
        meta.setLore(Arrays.asList(
                "§7Number of selected groups : " + group.getGroups().size(),
                "§7Number of included material : " + group.getNonGroupInheritedItemTypes().size(),
                "",
                "§7Total number of included material " + group.getItemTypes().size()));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Collection<IncludeItemTypeGroup> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<IncludeItemTypeGroup> includeGroups = new ArrayList<>();

        for (AbstractItemTypeGroup group : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if (group instanceof IncludeItemTypeGroup) {
                includeGroups.add((IncludeItemTypeGroup) group);
            }
        }
        return includeGroups;
    }

    @Override
    protected LazyElement<GroupConfigSubSettingGui> newInstanceOfGui(IncludeItemTypeGroup group, GuiItem item) {
        return new LazyElement<>(item, () -> new GroupConfigSubSettingGui(this, group));
    }

    @Override
    protected String genericDisplayedName() {
        return "material group";
    }

    @Override
    protected IncludeItemTypeGroup createAndSaveNewEmptyGeneric(String name) {
        ItemGroupManager manager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        if (manager.getGroupMap().containsKey(name)) return null;

        ConfigurationSection config = ConfigHolder.ITEM_GROUP_HOLDER.getConfig();
        config.set(name + "." + ItemGroupManager.GROUP_TYPE_PATH, GroupType.INCLUDE.getGroupID());

        return (IncludeItemTypeGroup) manager.createGroup(config, name);
    }

}
