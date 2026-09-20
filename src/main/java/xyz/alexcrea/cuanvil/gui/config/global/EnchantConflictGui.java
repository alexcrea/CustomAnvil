package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.Collection;

@NotNullByDefault
public class EnchantConflictGui extends MappedGuiListConfigGui<EnchantConflictGroup,
        MappedGuiListConfigGui.LazyElement<EnchantConflictSubSettingGui>> {

    //TODO #130 part 3
    private static @Nullable EnchantConflictGui INSTANCE;

    @Nullable
    public static EnchantConflictGui getCurrentInstance() {
        return INSTANCE;
    }

    public static EnchantConflictGui getInstance() {
        if(INSTANCE == null) INSTANCE = new EnchantConflictGui();

        return INSTANCE;
    }

    // Need to init myself
    public EnchantConflictGui(Gui parent) {
        super(MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_TITLE(), parent);
    }

    private EnchantConflictGui() {
        super(MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_TITLE());

        init();
    }

    @Override
    protected EnchantConflictGroup createAndSaveNewEmptyGeneric(String name) {
        // Create new empty conflict and display it to the admin
        EnchantConflictGroup conflict = new EnchantConflictGroup(
                name,
                new IncludeGroup(MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_DEFAULT_NEW().unformatted()),
                0);

        try(var lock = ConfigHolder.CONFLICT.write) {
            var holder = lock.get();

            holder.getConflictManager().addConflict(conflict);

            // save empty conflict in config
            String[] emptyStringArray = new String[0];

            FileConfiguration config = holder.getConfig();
            config.set(name + ".enchantments", emptyStringArray);
            config.set(name + ".notAffectedGroups", emptyStringArray);
            config.set(name + ".maxEnchantmentBeforeConflict", 0);

            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                holder.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }
        }

        return conflict;
    }

    @Override
    public ItemStack createItemForGeneric(EnchantConflictGroup conflict) {
        ItemStack item = new ItemStack(conflict.getRepresentativeMaterial());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        var name = CasedStringUtil.snakeToUpperSpacedCase(conflict.toString());

        ComponentUtil.INSTANCE.setMessageName(meta, MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_NAME(), name);
        ComponentUtil.INSTANCE.applyLore(MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_LORE().formatted(
                conflict.getEnchants().size(),
                conflict.getCantConflictGroup().getGroups().size(),
                conflict.getMinBeforeBlock()
        ), meta);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected LazyElement<EnchantConflictSubSettingGui> newInstanceOfGui(EnchantConflictGroup conflict, GuiItem item) {
        return new LazyElement<>(item, () -> new EnchantConflictSubSettingGui(this, conflict));
    }

    @Override
    protected Message genericDisplayedName() {
        return MsgUI.INSTANCE.getENCHANTMENT_CONFLICT_GENERIC_NAME();
    }

    @Override
    protected Collection<EnchantConflictGroup> getEveryInstanceOfGeneric() {
        try(var lock = ConfigHolder.CONFLICT.read) {
            return lock.get().getConflictManager().getConflictList();
        }
    }

}
