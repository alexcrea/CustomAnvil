package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import io.delilaheve.CustomAnvil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.SettingGui;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.MiniMessageUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * A utility class to store function that create generic GUI item.
 */
@NotNullByDefault
public class GuiGlobalItems {

    private static final Component EMPTY_NAME_COMPONENT = MiniMessageUtil.mm.deserialize("<red>");

    public static final Material DEFAULT_SAVE_ITEM = Material.LIME_DYE;
    public static final Material DEFAULT_NO_CHANGE_ITEM = Material.GRAY_DYE;

    private static final Material DEFAULT_BACKGROUND_MAT = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    /**
     * Create a GuiItem that open the given GUi.
     *
     * @param item The item to display in the GUI.
     * @param goal The GUI to open on click.
     * @return An GuiItem that open goal on click.
     */
    public static GuiItem goToGuiItem(ItemStack item, Gui goal) {
        return new GuiItem(item, GuiGlobalActions.openGuiAction(goal), CustomAnvil.instance);
    }

    private static ItemStack getBackItemStack() {
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta meta = back.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_BACK_ITEM_TITLE);
        back.setItemMeta(meta);

        return back;
    }

    /**
     * Create back button item from default back GuiItem.
     * The back item will open the goal inventory when clicked.
     *
     * @param goal The GUI to go back to.
     * @return An GuiItem that go back to goal on click.
     */
    public static GuiItem backItem(Gui goal) {
        return goToGuiItem(getBackItemStack(), goal);
    }

    /**
     * Add default back item to a GUI pattern with the reserved character key <strong>B</strong>.
     * The back item will open the target inventory when clicked.
     *
     * @param target The pattern to add the back item.
     * @param goal   The GUI to go back to.
     */
    public static void addBackItem(
            PatternPane target,
            Gui goal
    ) {
        target.bindItem('B', backItem(goal));
    }

    /**
     * Get a background item with backgroundMat as the displayed material.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     *
     * @param backgroundMat The material to which the background item should be made of.
     * @return A background item with backgroundMat as material.
     */
    public static GuiItem backgroundItem(Material backgroundMat) {
        ItemStack item = new ItemStack(backgroundMat);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        PlatformUtil.INSTANCE.setComponentDisplayName(meta, EMPTY_NAME_COMPONENT, null);
        item.setItemMeta(meta);
        return new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }

    /**
     * Get default background GuiItem.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     *
     * @return A new instance of the default background item.
     */
    public static GuiItem backgroundItem() {
        return backgroundItem(DEFAULT_BACKGROUND_MAT);
    }

    /**
     * Add default background item to a GUI pattern with the reserved character key <strong>0</strong>.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     *
     * @param target        The pattern to add the background item.
     * @param backgroundMat The material of the background item.
     */
    public static void addBackgroundItem(
            PatternPane target,
            Material backgroundMat
    ) {
        target.bindItem('0', backgroundItem(backgroundMat));
    }

    /**
     * Add default background item to a GUI pattern with the reserved character key <strong>0</strong>.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     *
     * @param target The pattern to add the background item.
     */
    public static void addBackgroundItem(PatternPane target) {
        addBackgroundItem(target, DEFAULT_BACKGROUND_MAT);
    }

    /**
     * Create a new save setting GuiItem.
     * A save setting item is a GuiItem that save a changed setting when clicked.
     * This item also check if the player who interacted with the item have the permission to save before saving.
     *
     * @param setting The setting to change.
     * @param goal    Parent GUI of this setting GUI. as setting will be change the display of goal GUI will be updated.
     * @return A save setting item.
     */
    public static GuiItem saveItem(
            SettingGui setting,
            ValueUpdatableGui goal
    ) {
        ItemStack item = new ItemStack(DEFAULT_SAVE_ITEM);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_SAVE_ITEM_TITLE);
        item.setItemMeta(meta);
        return new GuiItem(item,
                GuiGlobalActions.saveSettingAction(setting, goal),
                CustomAnvil.instance);
    }

    /**
     * Get the global "no change" GuiItem.
     * The no change item do nothing when interacted, only the title is change to show there is no change.
     *
     * @return The global "no change" item.
     */
    public static GuiItem noChangeItem() {
        ItemStack item = new ItemStack(DEFAULT_NO_CHANGE_ITEM);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_NO_CHANGE_ITEM_TITLE);
        item.setItemMeta(meta);
        return new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }

    /**
     * Create a new "create and go to the setting GUI" GuiItem.
     * This item will create and open a setting GUI from the factory.
     *
     * @param item    The item that will be displayed.
     * @param factory The setting's GUI factory.
     * @return A formatted GuiItem that will create and open a GUI for the setting.
     */
    public static GuiItem openSettingGuiItem(
            ItemStack item,
            SettingGui.SettingGuiFactory factory
    ) {
        return new GuiItem(item, GuiGlobalActions.openSettingGuiAction(factory), CustomAnvil.instance);
    }

    /**
     * Create an arbitrary GuiItem from a unique setting and item's property.
     *
     * @param factory     The setting's GUI factory.
     * @param itemMat     Displayed material of the item.
     * @param itemName    Name of the item.
     * @param value       Value of the setting when the item is created.
     *                    Will not update automatically, if the setting's value change, the item need to be created again.
     * @param displayLore Gui display item lore.
     * @return A formatted GuiItem that will create and open a GUI for the setting.
     */
    public static GuiItem createGuiItemFromProperties(
            SettingGui.SettingGuiFactory factory,
            Material itemMat,
            Component itemName,
            Object value,//TODO ????
            @Nullable List<Message> displayLore,
            boolean displayValuePrefix,
            @Nullable Object... params
    ) {
        // Prepare lore
        var loreHeader = (displayValuePrefix ?
                MsgUI.INSTANCE.getGLOBAL_ITEM_ITEM_LORE_PREFIX() :
                MsgUI.INSTANCE.getGLOBAL_ITEM_ITEM_LORE_PREFIX_ALONE());

        List<Component> lore = loreHeader.formatted(value);
        if(displayLore != null) {
            lore.add(Component.empty());
            for(Message message : displayLore) {
                lore.addAll(message.formatted(params));
            }
        }

        // Create & initialise item
        ItemStack item = new ItemStack(itemMat);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        PlatformUtil.INSTANCE.setComponentDisplayName(itemMeta, itemName, null);
        ComponentUtil.applyLore(lore, itemMeta);
        itemMeta.addItemFlags(ItemFlag.values());

        item.setItemMeta(itemMeta);
        // Create GuiItem
        return openSettingGuiItem(item, factory);
    }

    /**
     * Get the setting name from the setting path.
     * For example: "gui.command.name" will return "name".
     *
     * @param path The setting's path.
     * @return The setting's name.
     */
    public static String getConfigNameFromPath(String path) {
        // Get index of first dot
        int indexOfDot = path.indexOf(".");
        // when indexOfDot == -1 (not fond), it is implied that indexOfDot+1 = 0. substring will keep the full path as expected
        return path.substring(indexOfDot + 1);
    }

    public static GuiItem temporaryCloseGuiToSelectItem(Material itemMaterial, Gui openBack) {
        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_TEMPORARY_CLOSE_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.SHARED_TEMPORARY_CLOSE_LORE);
        item.setItemMeta(meta);

        return new GuiItem(item, event -> {
            event.setCancelled(true);

            HumanEntity player = event.getWhoClicked();

            CustomAnvil.Companion.getChatListener().setListenedCallback(player, (message) -> {

                if(message == null) return;
                openBack.show(player);

            });

            MsgUI.SHARED_TEMPORARY_CLOSE_RETURN.send(player);
            player.closeInventory();
        }, CustomAnvil.instance);
    }

    public static GuiItem cancelAndGoBackItem(Gui backOnCancel) {
        var item = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_CANCEL_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.SHARED_CANCEL_LORE);
        item.setItemMeta(meta);

        return new GuiItem(item, GuiGlobalActions.openGuiAction(backOnCancel), CustomAnvil.instance);
    }

    public static GuiItem confirmItem(Consumer<InventoryClickEvent> action) {
        return confirmItem(false, action);
    }

    public static GuiItem confirmItem(boolean permanent, Consumer<InventoryClickEvent> action) {
        var item = new ItemStack(Material.GREEN_TERRACOTTA);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_CONFIRM_TITLE);
        var lore = MsgUI.SHARED_CONFIRM_LORE.formatted();
        if(permanent) {
            lore.addAll(MsgUI.SHARED_CONFIRM_PERMANENT_LORE.formatted());
        }

        item.setItemMeta(meta);
        return new GuiItem(item, action, CustomAnvil.instance);
    }

}
