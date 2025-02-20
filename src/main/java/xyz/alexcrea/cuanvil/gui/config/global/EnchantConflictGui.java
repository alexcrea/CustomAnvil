package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Collection;

public class EnchantConflictGui extends MappedGuiListConfigGui<EnchantConflictGroup,
        MappedGuiListConfigGui.LazyElement<EnchantConflictSubSettingGui>> {

    private static EnchantConflictGui INSTANCE;

    @Nullable
    public static EnchantConflictGui getCurrentInstance(){
        return INSTANCE;
    }

    @NotNull
    public static EnchantConflictGui getInstance(){
        if(INSTANCE == null) INSTANCE = new EnchantConflictGui();

        return INSTANCE;
    }


    private EnchantConflictGui() {
        super( "Conflict Config");

        init();
    }

    @Override
    protected EnchantConflictGroup createAndSaveNewEmptyGeneric(String name){
        // Create new empty conflict and display it to the admin
        EnchantConflictGroup conflict = new EnchantConflictGroup(
                name,
                new IncludeGroup("new_group"),
                0);

        ConfigHolder.CONFLICT_HOLDER.getConflictManager().addConflict(conflict);

        // save empty conflict in config
        String[] emptyStringArray = new String[0];

        FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();
        config.set(name + ".enchantments", emptyStringArray);
        config.set(name + ".notAffectedGroups", emptyStringArray);
        config.set(name + ".maxEnchantmentBeforeConflict", 0);

        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return conflict;
    }

    @Override
    public ItemStack createItemForGeneric(EnchantConflictGroup conflict) {
        ItemStack item = new ItemStack(conflict.getRepresentativeMaterial());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§e" + CasedStringUtil.snakeToUpperSpacedCase(conflict.toString()) + " §fConflict");
        meta.setLore(Arrays.asList(
                "§7Enchantment count:       §e" + conflict.getEnchants().size(),
                "§7Group count:               §e" + conflict.getCantConflictGroup().getGroups().size(),
                "§7Min enchantments count: §e" + conflict.getMinBeforeBlock()
        ));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected LazyElement<EnchantConflictSubSettingGui> newInstanceOfGui(EnchantConflictGroup conflict, GuiItem item) {
        return new LazyElement<>(item, () -> new EnchantConflictSubSettingGui(this, conflict));
    }

    @Override
    protected String genericDisplayedName() {
        return "conflict";
    }

    @Override
    protected Collection<EnchantConflictGroup> getEveryDisplayableInstanceOfGeneric() {
        return ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList();
    }

}
