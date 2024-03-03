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
import xyz.alexcrea.cuanvil.util.StringUtil;

import java.util.Collections;

// maybe use builder patern ?
public class GuiGlobalItems {

    // return
    public static GuiItem goToGuiItem(@NotNull ItemStack item, @NotNull Gui goal){
        return new GuiItem(item, GuiGlobalActions.openGuiAction(goal), CustomAnvil.instance);
    }

    // statically create back itemstack
    private static final ItemStack BACK_ITEM = new ItemStack(Material.BARRIER);
    static {
        // todo add what I need to add to the back item
        ItemMeta meta = BACK_ITEM.getItemMeta();
        meta.setDisplayName("\u00A7cBack");
        BACK_ITEM.setItemMeta(meta);
    }
    public static GuiItem backItem(@NotNull Gui goal){
        // simple go back item
        return goToGuiItem(BACK_ITEM, goal);
    }
    public static void addBackItem(@NotNull PatternPane target,
                                   @NotNull Gui goal){
        target.bindItem('B', backItem(goal));
    }

    private static final Material DEFAULT_BACKGROUND_MAT = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
    public static GuiItem backgroundItem(Material backgroundMat){
        ItemStack item = new ItemStack(backgroundMat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A7c");
        item.setItemMeta(meta);
        return new GuiItem(item, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }
    public static GuiItem backgroundItem(){
        return backgroundItem(DEFAULT_BACKGROUND_MAT);
    }
    public static void addBackgroundItem(@NotNull PatternPane target,
                                            @NotNull Material backgroundMat){
        target.bindItem('0', backgroundItem(backgroundMat));
    }
    public static void addBackgroundItem(@NotNull PatternPane target){
        addBackgroundItem(target, DEFAULT_BACKGROUND_MAT);
    }

    private static final Material DEFAULT_SAVE_ITEM = Material.LIME_DYE;
    private static final Material DEFAULT_NO_CHANGE_ITEM = Material.GRAY_DYE;
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

    private static final GuiItem NO_CHANGE_ITEM;
    static {
        ItemStack item = new ItemStack(DEFAULT_NO_CHANGE_ITEM);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00A77No change. can't save.");
        item.setItemMeta(meta);
        NO_CHANGE_ITEM = new GuiItem(item,GuiGlobalActions.stayInPlace, CustomAnvil.instance);
    }

    public static GuiItem noChangeItem(){
        return NO_CHANGE_ITEM;
    }

    public static GuiItem openSettingGuiItem(
            @NotNull ItemStack item,
            @NotNull AbstractSettingGui.SettingGuiFactory factory
    ){
        return new GuiItem(item, GuiGlobalActions.openSettingGuiAction(factory), CustomAnvil.instance);
    }

    private static final String SETTING_ITEM_LORE_PREFIX = "\u00A77value: ";

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

    public static GuiItem boolSettingGuiItem(
            @NotNull BoolSettingsGui.BoolSettingFactory factory
    ){
        String configPath = getConfigNameFromPath(factory.getConfigPath());
        return boolSettingGuiItem(factory, StringUtil.snakeToUpperSpacedCase(configPath));
    }


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


    public static GuiItem intSettingGuiItem(
            @NotNull IntSettingsGui.IntSettingFactory factory,
            @NotNull Material itemMat
    ){
        String configPath = getConfigNameFromPath(factory.getConfigPath());
        return intSettingGuiItem(factory, itemMat, StringUtil.snakeToUpperSpacedCase(configPath));
    }
    private static GuiItem createGuiItemFromProperties(
            @NotNull AbstractSettingGui.SettingGuiFactory factory,
            @NotNull Material itemMat,
            @NotNull StringBuilder itemName,
            @NotNull Object value
    ){
        // Create item
        ItemStack item = new ItemStack(itemMat);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(itemName.toString());
        itemMeta.setLore(Collections.singletonList(SETTING_ITEM_LORE_PREFIX+value));

        item.setItemMeta(itemMeta);
        return openSettingGuiItem(item, factory);
    }

    public static String getConfigNameFromPath(String path){
        int indexOfDot = path.indexOf(".");
        //if(indexOfDot == -1) return path;
        // indexOfDot == -1 (not fond) imply indexOfDot+1 = 0. substring will keep the full path as expected
        return path.substring(indexOfDot+1);
    }

}
