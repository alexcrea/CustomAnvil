package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.anvil.AnvilUseType;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.WorkPenaltyType;
import xyz.alexcrea.cuanvil.gui.config.global.BasicConfigGui;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class WorkPenaltyTypeSettingGui extends AbstractSettingGui {

    private final @NotNull WorkPenaltyType currentType;
    private final @NotNull Map<AnvilUseType, WorkPenaltyType.WorkPenaltyPart> items;

    public WorkPenaltyTypeSettingGui(@NotNull BasicConfigGui parent) {
        super(4, MsgUI.INSTANCE.getBASIC_WORK_PENALTY_TITLE(), parent);

        this.currentType = ConfigOptions.INSTANCE.getWorkPenaltyType();
        this.items = new EnumMap<>(this.currentType.getPartMap());

        for(AnvilUseType type : useTypes.keySet()) {
            updateGuiForType(type);
        }
    }

    public static GuiItem getDisplayItem(@NotNull BasicConfigGui parent,
                                         @NotNull Material itemMat,
                                         @NotNull Message name) {
        var item = new ItemStack(itemMat);

        var meta = item.getItemMeta();
        assert meta != null;

        var lore = new ArrayList<Message>();
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_LORE());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_INCREASING());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_ADDITIVE());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_LORE_BREAK());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_SHARED());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_EXCLUSIVE());

        ComponentUtil.INSTANCE.setMessageName(meta, name);
        ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(lore), meta);

        item.setItemMeta(meta);

        return new GuiItem(item, (event) -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            // Do not allow to open inventory if player do not have edit configuration permission
            if(!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                MsgUI.INSTANCE.getSHARED_CONFIG_NO_EDIT_PERM().send(player);
                return;
            }
            new WorkPenaltyTypeSettingGui(parent).show(player);
        }, CustomAnvil.instance);
    }

    private static final Map<AnvilUseType, String> useTypes =
            Map.of(
                    AnvilUseType.RENAME_ONLY, "a1z9Z",
                    AnvilUseType.MERGE, "b2y8Y",
                    AnvilUseType.UNIT_REPAIR, "c3x7X",
                    AnvilUseType.CUSTOM_CRAFT, "d4w6W"
            );

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern( // Yeah that a mess
                "00a1z9Z00",
                "00b2y8Y00",
                "00c3x7X00",
                "B004w600S"
        );
    }

    public void updateGuiForType(AnvilUseType type) {
        PatternPane pane = getPane();

        String typeVals = useTypes.get(type);

        char increment = typeVals.charAt(0);
        char additive = typeVals.charAt(1);
        char display = typeVals.charAt(2);
        char exclusiveIncrement = typeVals.charAt(3);
        char exclusiveAdditive = typeVals.charAt(4);

        WorkPenaltyType.WorkPenaltyPart part = items.get(type);
        //TODO MESSAGE
        String increasingStr = (part.penaltyIncrease() ? "§a" : "§c") + "Increasing";
        String additiveStr = (part.penaltyAdditive() ? "§a" : "§c") + "Additive";
        String exclusiveIncreasingStr = (part.exclusivePenaltyIncrease() ? "§a" : "§c") + "Increasing";
        String exclusiveAdditiveStr = (part.exclusivePenaltyAdditive() ? "§a" : "§c") + "Additive";

        // Display item
        ItemStack displayItem = new ItemStack(type.getDisplayMat());

        ArrayList<String> displayLore = new ArrayList<>();
        displayLore.add("§eShared§7: " + additiveStr + " §7| " + increasingStr);
        displayLore.add("§eExclusive§7: " + exclusiveAdditiveStr + " §7| " + exclusiveIncreasingStr);

        ItemMeta meta = displayItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§e" + type.getDisplayName());
        meta.setLore(displayLore);
        displayItem.setItemMeta(meta);

        pane.bindItem(display, new GuiItem(displayItem, (event) -> {
            event.setCancelled(true);
        }));

        // Can probably put this in a function but this works so
        // "Increment" item
        ItemStack incrementItem = new ItemStack(part.penaltyIncrease() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = incrementItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(increasingStr);

        var lore = new ArrayList<Message>();
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_INCREASING());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_SHARED());

        ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(lore), meta);
        incrementItem.setItemMeta(meta);

        pane.bindItem(increment, new GuiItem(incrementItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(
                    !part.penaltyIncrease(), part.penaltyAdditive(),
                    part.exclusivePenaltyIncrease(), part.exclusivePenaltyAdditive());
            items.replace(type, newPart);
            updateGuiForType(type);
            update();
        }));

        // "Additive" item
        ItemStack additiveItem = new ItemStack(part.penaltyAdditive() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = additiveItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(additiveStr);

        lore.clear();
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_ADDITIVE());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_SHARED());

        ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(lore), meta);
        additiveItem.setItemMeta(meta);

        pane.bindItem(additive, new GuiItem(additiveItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(
                    part.penaltyIncrease(), !part.penaltyAdditive(),
                    part.exclusivePenaltyIncrease(), part.exclusivePenaltyAdditive());
            items.replace(type, newPart);
            updateGuiForType(type);
            update();
        }));

        // exclusive "Increment" item
        ItemStack exclusiveIncrementItem = new ItemStack(part.exclusivePenaltyIncrease() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = exclusiveIncrementItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(exclusiveIncreasingStr);

        lore.clear();
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_INCREASING());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_EXCLUSIVE());

        ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(lore), meta);
        exclusiveIncrementItem.setItemMeta(meta);

        pane.bindItem(exclusiveIncrement, new GuiItem(exclusiveIncrementItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(
                    part.penaltyIncrease(), part.penaltyAdditive(),
                    !part.exclusivePenaltyIncrease(), part.exclusivePenaltyAdditive());
            items.replace(type, newPart);
            updateGuiForType(type);
            update();
        }));

        // exclusive "Additive" item
        ItemStack exclusiveAdditiveItem = new ItemStack(part.exclusivePenaltyAdditive() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = exclusiveAdditiveItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(exclusiveAdditiveStr);

        lore.clear();
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_ADDITIVE());
        lore.add(MsgUI.INSTANCE.getBASIC_WORK_PENALTY_EXPLAIN_EXCLUSIVE());

        ComponentUtil.INSTANCE.applyLore(ComponentUtil.INSTANCE.asComponents(lore), meta);
        exclusiveAdditiveItem.setItemMeta(meta);

        pane.bindItem(exclusiveAdditive, new GuiItem(exclusiveAdditiveItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(
                    part.penaltyIncrease(), part.penaltyAdditive(),
                    part.exclusivePenaltyIncrease(), !part.exclusivePenaltyAdditive());
            items.replace(type, newPart);
            updateGuiForType(type);
            update();
        }));
    }

    @Override
    public boolean onSave() {
        return saveWorkPenalty(items);
    }

    public static boolean saveWorkPenalty(Map<AnvilUseType, WorkPenaltyType.WorkPenaltyPart> partEnum) {
        ConfigHolder configHolder = ConfigHolder.DEFAULT_CONFIG;
        FileConfiguration config = configHolder.getConfig();

        partEnum.forEach((key, value) -> {
            String partPath = key.getPath();

            if(key.getDefaultPenalty().equals(value)) {
                config.set(partPath, null);
                return;
            }

            config.set(partPath + '.' + ConfigOptions.WORK_PENALTY_INCREASE, value.penaltyIncrease());
            config.set(partPath + '.' + ConfigOptions.WORK_PENALTY_ADDITIVE, value.penaltyAdditive());
            config.set(partPath + '.' + ConfigOptions.EXCLUSIVE_WORK_PENALTY_INCREASE, value.exclusivePenaltyIncrease());
            config.set(partPath + '.' + ConfigOptions.EXCLUSIVE_WORK_PENALTY_ADDITIVE, value.exclusivePenaltyAdditive());
        });

        return configHolder.saveToDisk(true);
    }

    @Override
    public boolean hadChange() {
        for(AnvilUseType type : items.keySet()) {
            if(!currentType.getPenaltyInfo(type).equals(items.get(type))) {
                return true;
            }
        }

        return false;
    }
}
