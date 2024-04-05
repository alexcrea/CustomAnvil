package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.gui.config.settings.subsetting.EnchantConflictSubSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class EnchantConflictGui extends ElementListGlobalConfigGui<EnchantConflictGroup> {

    public final static EnchantConflictGui INSTANCE = new EnchantConflictGui();

    static {
        INSTANCE.init();
    }

    private final HashMap<EnchantConflictGroup, EnchantConflictSubSettingGui> conflictGuiMap;

    private EnchantConflictGui() {
        super(6, "Conflict Config");
        this.conflictGuiMap = new HashMap<>();
    }

    @Override
    protected GuiItem prepareCreateNewItem(){
        // Create new conflict item
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();

        createMeta.setDisplayName("\u00A7aCreate new conflict");
        createMeta.setLore(Arrays.asList(
                "\u00A77Create a new anvil restriction.",
                "\u00A77You will be asked to name the conflict in chat.",
                "\u00A77Then, you should edit the conflict config as you need"
        ));

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, (clickEvent) -> {
            clickEvent.setCancelled(true);
            HumanEntity player = clickEvent.getWhoClicked();

            // check permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            player.closeInventory();

            player.sendMessage("\u00A7eWrite the conflict you want to create in the chat.\n" +
                    "\u00A7eOr write \u00A7ccancel \u00A7eto go back to conflict config menu");

            CustomAnvil.Companion.getChatListener().setListenedCallback(player, prepareCreateItemConsumer(player));

        }, CustomAnvil.instance);
    }

    private Consumer<String> prepareCreateItemConsumer(HumanEntity player) {
        AtomicReference<Consumer<String>> selfRef = new AtomicReference<>();
        Consumer<String> selfCallback = (message) -> {
            if (message == null) return;

            // check permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }

            message = message.toLowerCase(Locale.ROOT);
            if ("cancel".equalsIgnoreCase(message)) {
                player.sendMessage("conflict creation cancelled...");
                show(player);
                return;
            }

            message = message.replace(' ', '_');

            // Try to find if it already exists in a for loop
            // Not the most efficient on large number of conflict, but it should not run often.
            for (EnchantConflictGroup conflict : ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList()) {
                if (conflict.getName().equalsIgnoreCase(message)) {
                    player.sendMessage("\u00A7cPlease enter a conflict name that do not already exist...");
                    // wait next message.
                    CustomAnvil.Companion.getChatListener().setListenedCallback(player, selfRef.get());
                    return;
                }
            }

            // Create new empty conflict and display it to the admin
            EnchantConflictGroup conflict = new EnchantConflictGroup(
                    message,
                    new IncludeGroup("new_group"),
                    0);

            ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList().add(conflict);
            updateValueForGeneric(conflict, true);

            // save empty conflict in config
            String[] emptyStringArray = new String[0];

            FileConfiguration config = ConfigHolder.CONFLICT_HOLDER.getConfig();
            config.set(message + ".enchantments", emptyStringArray);
            config.set(message + ".notAffectedGroups", emptyStringArray);
            config.set(message + ".maxEnchantmentBeforeConflict", 0);

            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            // show the new conflict config to the player
            this.conflictGuiMap.get(conflict).show(player);

        };

        selfRef.set(selfCallback);
        return selfCallback;
    }

    @Override
    public void reloadValues() {
        this.conflictGuiMap.forEach((conflict, gui) -> gui.cleanUnused());
        this.conflictGuiMap.clear();

        super.reloadValues();
    }

    @Override
    public ItemStack createItemForGeneric(EnchantConflictGroup conflict) {
        ItemStack item = new ItemStack(conflict.getRepresentativeMaterial());

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(conflict.getName()) + " \u00A7fConflict");
        meta.setLore(Arrays.asList(
                "\u00A77Enchantment count:      \u00A7e" + conflict.getEnchants().size(),
                "\u00A77Group count:            \u00A7e" + conflict.getCantConflictGroup().getGroups().size(),
                "\u00A77Min enchantments count: \u00A7e" + conflict.getMinBeforeBlock()
        ));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected void updateGeneric(EnchantConflictGroup conflict, ItemStack usedItem) {
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);

        GuiItem guiItem;
        if (gui == null) {
            // Create new sub setting gui
            guiItem = new GuiItem(usedItem, CustomAnvil.instance);
            gui = new EnchantConflictSubSettingGui(this, conflict, guiItem);

            guiItem.setAction(GuiGlobalActions.openGuiAction(gui));

            this.conflictGuiMap.put(conflict, gui);
            addToPage(guiItem);
        } else {
            // Replace item with the updated one
            guiItem = gui.getParentItemForThisGui();
            guiItem.setItem(usedItem);
        }
        gui.updateLocal();

    }

    @Override
    protected GuiItem findGuiItemForRemoval(EnchantConflictGroup conflict) {
        EnchantConflictSubSettingGui gui = this.conflictGuiMap.get(conflict);
        if (gui == null) return null;

        this.conflictGuiMap.remove(conflict);
        return gui.getParentItemForThisGui();
    }

    @Override
    protected List<EnchantConflictGroup> getEveryDisplayableInstanceOfGeneric() {
        return ConfigHolder.CONFLICT_HOLDER.getConflictManager().getConflictList();
    }

}
