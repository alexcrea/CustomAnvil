package xyz.alexcrea.cuanvil.gui.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.BoolSettingsGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;

/**
 * A utility class to store function that create generic GUI item.
 */
public class GuiGlobalItems {

    // statically create default back itemstack
    private static final ItemStack BACK_ITEM;
    static {
        BACK_ITEM = new ItemStack(Material.BARRIER);
        ItemMeta meta = BACK_ITEM.getItemMeta();
        meta.setDisplayName("\u00A7cBack");
        BACK_ITEM.setItemMeta(meta);
    }

    /**
     * Create a GuiItem that open the given GUi.
     * @param item The item to display in the GUI.
     * @param goal The GUI to open on click.
     * @return An GuiItem that open goal on click.
     */
    public static GuiItem goToGuiItem(@NotNull ItemStack item, @NotNull Gui goal){
        return new GuiItem(item, GuiGlobalActions.openGuiAction(goal), CustomAnvil.instance);
    }

    /**
     * Create back button item from default back GuiItem.
     * The back item will open the goal inventory when clicked.
     * @param goal The GUI to go back to.
     * @return An GuiItem that go back to goal on click.
     */
    public static GuiItem backItem(@NotNull Gui goal){
        return goToGuiItem(BACK_ITEM, goal);
    }

    /**
     * Add default back item to a GUI pattern with the reserved character key <strong>B</strong>.
     * The back item will open the target inventory when clicked.
     * @param target The pattern to add the back item.
     * @param goal The GUI to go back to.
     */
    public static void addBackItem(@NotNull PatternPane target,
                                   @NotNull Gui goal){
        target.bindItem('B', backItem(goal));
    }

    private static final Material DEFAULT_BACKGROUND_MAT = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    /**
     * Get a background item with backgroundMat as the displayed material.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     * @param backgroundMat The material to which the background item should be made of.
     * @return A background item with backgroundMat as material.
     */
    public static GuiItem backgroundItem(Material backgroundMat){
        ItemStack item = new ItemStack(backgroundMat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A7c");
        item.setItemMeta(meta);
        return new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }
    /**
     * Get default background GuiItem.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     * @return A new instance of the default background item.
     */
    public static GuiItem backgroundItem(){
        return backgroundItem(DEFAULT_BACKGROUND_MAT);
    }

    /**
     * Add default background item to a GUI pattern with the reserved character key <strong>0</strong>.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     * @param target The pattern to add the background item.
     * @param backgroundMat The material of the background item.
     */
    public static void addBackgroundItem(@NotNull PatternPane target,
                                         @NotNull Material backgroundMat){
        target.bindItem('0', backgroundItem(backgroundMat));
    }

    /**
     * Add default background item to a GUI pattern with the reserved character key <strong>0</strong>.
     * A background item is a GuiItem that do nothing when interacted with and have an empty name.
     * @param target The pattern to add the background item.
     */
    public static void addBackgroundItem(@NotNull PatternPane target){
        addBackgroundItem(target, DEFAULT_BACKGROUND_MAT);
    }

    private static final Material DEFAULT_SAVE_ITEM = Material.LIME_DYE;
    private static final Material DEFAULT_NO_CHANGE_ITEM = Material.GRAY_DYE;

    /**
     * Create a new save setting GuiItem.
     * A save setting item is a GuiItem that save a changed setting when clicked.
     * This item also check if the player who interacted with the item have the permission to save before saving.
     * @param setting The setting to change.
     * @param goal Parent GUI of this setting GUI. as setting will be change the display of goal GUI will be updated.
     * @return A save setting item.
     */
    public static GuiItem saveItem(
            @NotNull AbstractSettingGui setting,
            @NotNull ValueUpdatableGui goal){

        ItemStack item = new ItemStack(DEFAULT_SAVE_ITEM);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A7aSave");
        item.setItemMeta(meta);
        return new GuiItem(item,
                GuiGlobalActions.saveSettingAction(setting, goal),
                CustomAnvil.instance);
    }

    // Create static non change item
    private static final GuiItem NO_CHANGE_ITEM;
    static {
        ItemStack item = new ItemStack(DEFAULT_NO_CHANGE_ITEM);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A77No change. can't save.");
        item.setItemMeta(meta);
        NO_CHANGE_ITEM = new GuiItem(item,GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }

    /**
     * Get the global "no change" GuiItem.
     * The no change item do nothing when interacted, only the title is change to show there is no change.
     * @return The global "no change" item.
     */
    public static GuiItem noChangeItem(){
        return NO_CHANGE_ITEM;
    }

    /**
     * Create a new "create and go to the setting GUI" GuiItem.
     * This item will create and open a setting GUI from the factory.
     * @param item The item that will be displayed.
     * @param factory The setting's GUI factory.
     * @return A formatted GuiItem that will create and open a GUI for the setting.
     */
    public static GuiItem openSettingGuiItem(
            @NotNull ItemStack item,
            @NotNull AbstractSettingGui.SettingGuiFactory factory
    ){
        return new GuiItem(item, GuiGlobalActions.openSettingGuiAction(factory), CustomAnvil.instance);
    }

    // Prefix of the one line lore that will be added to setting's item.
    public static final String SETTING_ITEM_LORE_PREFIX = "\u00A77value: ";

    /**
     * Create a new Boolean setting GuiItem.
     * This item will create and open a boolean setting GUI from the factory.
     * The item will have its value written in the lore part of the item.
     * @param factory The setting's GUI factory.
     * @param name Name of the item.
     * @return A formatted GuiItem that will create and open a GUI for the boolean setting.
     */
    public static GuiItem boolSettingGuiItem(
            @NotNull BoolSettingsGui.BoolSettingFactory factory,
            @NotNull String name
    ){
        // Get item properties
        boolean value = factory.getConfiguredValue();

        Material itemMat;
        StringBuilder itemName = new StringBuilder("\u00A7");
        if(value){
            itemMat = Material.GREEN_TERRACOTTA;
            itemName.append("a");
        }else{
            itemMat = Material.RED_TERRACOTTA;
            itemName.append("c");
        }
        itemName.append(name);

        return createGuiItemFromProperties(factory, itemMat, itemName, value);
    }

    /**
     * Create a new boolean setting GuiItem.
     * This item will create and open a boolean setting GUI from the factory.
     * The item will have its value written in the lore part of the item.
     * Item's name will be the factory set title.
     * @param factory The setting's GUI factory.
     * @return A formatted GuiItem that will create and open a GUI for the boolean setting.
     */
    public static GuiItem boolSettingGuiItem(
            @NotNull BoolSettingsGui.BoolSettingFactory factory
    ){
        String configPath = getConfigNameFromPath(factory.getConfigPath());
        return boolSettingGuiItem(factory, CasedStringUtil.snakeToUpperSpacedCase(configPath));
    }

    /**
     * Create a new int setting GuiItem.
     * This item will create and open an int setting GUI from the factory.
     * The item will have its value written in the lore part of the item.
     * @param factory The setting's GUI factory.
     * @param itemMat Displayed material of the item.
     * @param name Name of the item.
     * @return A formatted GuiItem that will create and open a GUI for the int setting.
     */
    public static GuiItem intSettingGuiItem(
            @NotNull IntSettingsGui.IntSettingFactory factory,
            @NotNull Material itemMat,
            @NotNull String name
    ){
        // Get item properties
        int value = factory.getConfiguredValue();
        StringBuilder itemName = new StringBuilder("\u00A7a").append(name);

        return createGuiItemFromProperties(factory, itemMat, itemName, value);
    }

    /**
     * Create a new int setting GuiItem.
     * This item will create and open an int setting GUI from the factory.
     * The item will have its value written in the lore part of the item.
     * Item's name will be the factory set title.
     * @param factory The setting's GUI factory.
     * @param itemMat Displayed material of the item.
     * @return A formatted GuiItem that will create and open a GUI for the int setting.
     */
    public static GuiItem intSettingGuiItem(
            @NotNull IntSettingsGui.IntSettingFactory factory,
            @NotNull Material itemMat
    ){
        String configPath = getConfigNameFromPath(factory.getConfigPath());
        return intSettingGuiItem(factory, itemMat, CasedStringUtil.snakeToUpperSpacedCase(configPath));
    }

    /**
     * Create an arbitrary GuiItem from a unique setting and item's property.
     * @param factory The setting's GUI factory.
     * @param itemMat Displayed material of the item.
     * @param itemName Name of the item.
     * @param value Value of the setting when the item is created.
     *              Will not update automatically, if the setting's value change, the item need to be created again.
     * @return A formatted GuiItem that will create and open a GUI for the setting.
     */
    private static GuiItem createGuiItemFromProperties(
            @NotNull AbstractSettingGui.SettingGuiFactory factory,
            @NotNull Material itemMat,
            @NotNull StringBuilder itemName,
            @NotNull Object value
    ){
        // Create & initialise item
        ItemStack item = new ItemStack(itemMat);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(itemName.toString());
        itemMeta.setLore(Collections.singletonList(SETTING_ITEM_LORE_PREFIX+value));

        item.setItemMeta(itemMeta);
        // Create GuiItem
        return openSettingGuiItem(item, factory);
    }

    /**
     * Get the setting name from the setting path.
     * For example: "gui.command.name" will return "name".
     * @param path The setting's path.
     * @return The setting's name.
     */
    public static String getConfigNameFromPath(String path){
        // Get index of first dot
        int indexOfDot = path.indexOf(".");
        // when indexOfDot == -1 (not fond), it is implied that indexOfDot+1 = 0. substring will keep the full path as expected
        return path.substring(indexOfDot+1);
    }

}
