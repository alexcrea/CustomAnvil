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
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.WorkPenaltyType;
import xyz.alexcrea.cuanvil.gui.config.global.BasicConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.util.AnvilUseType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WorkPenaltyTypeSettingGui extends AbstractSettingGui {

    private static final String INCREASING_EXPLANATION = "§eIncreasing§7: will penalty be increased (in item)";
    private static final String ADDING_EXPLANATION = "§eAdditive§7: will penalty be added to the cost";

    private final @NotNull WorkPenaltyType currentType;
    private final @NotNull Map<AnvilUseType, WorkPenaltyType.WorkPenaltyPart> items;

    public WorkPenaltyTypeSettingGui(@NotNull BasicConfigGui parent) {
        super(4, "§8Work Penalty Type", parent);

        this.currentType = ConfigOptions.INSTANCE.getWorkPenaltyType();
        this.items = new EnumMap<>(this.currentType.getPartMap());

        for (AnvilUseType type : AnvilUseType.getEntries()) {
            updateGuiForType(type);
        }
    }

    public static GuiItem getDisplayItem(@NotNull BasicConfigGui parent,
                                         @NotNull Material itemMat,
                                         @NotNull String name) {
        List<String> displayLore = new ArrayList<>();

        displayLore.add("§7Work penalty increase the price for every anvil use.");
        displayLore.add("§7This config allow you to choose the comportment of work penalty.");
        displayLore.add(INCREASING_EXPLANATION);
        displayLore.add(ADDING_EXPLANATION);
        displayLore.add("");

        WorkPenaltyType workPenalty = ConfigOptions.INSTANCE.getWorkPenaltyType();
        for (AnvilUseType type : AnvilUseType.getEntries()) {
            String increasing = (workPenalty.isPenaltyIncreasing(type) ? "§a" : "§c") + "Increasing";
            String additive = (workPenalty.isPenaltyAdditive(type) ? "§a" : "§c") + "Additive";

            displayLore.add("§e" + type.getTypeName() + ": " + additive + " §7| " + increasing);
        }

        ItemStack item = new ItemStack(itemMat);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(displayLore);

        item.setItemMeta(meta);

        return new GuiItem(item, (event) -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            new WorkPenaltyTypeSettingGui(parent).show(player);
        }, CustomAnvil.instance);
    }

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern(
                "00zy0xw00",
                "001203400",
                "00ab0c000",
                "B0000000S"
        );
    }

    public void updateGuiForType(AnvilUseType type) {
        PatternPane pane = getPane();
        int ordinal = type.ordinal();

        int display = 'z' - ordinal;
        int increment = 'a' + ordinal;
        int additive = '1' + ordinal;

        WorkPenaltyType.WorkPenaltyPart part = items.get(type);
        String increasingStr = (part.penaltyIncrease() ? "§a" : "§c") + "Increasing";
        String additiveStr = (part.penaltyAdditive() ? "§a" : "§c") + "Additive";

        // Display item
        ItemStack displayItem = new ItemStack(type.getDisplayMat());

        ArrayList<String> displayLore = new ArrayList<>();
        displayLore.add("§e" + type.getTypeName() + ": " + additiveStr + " §7| " + increasingStr);

        ItemMeta meta = displayItem.getItemMeta();
        meta.setDisplayName("§e" + type.getDisplayName());
        meta.setLore(displayLore);
        displayItem.setItemMeta(meta);

        pane.bindItem(display, new GuiItem(displayItem, (event) -> {
            event.setCancelled(true);
        }));

        // "Increment" item
        ItemStack incrementItem = new ItemStack(part.penaltyIncrease() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = incrementItem.getItemMeta();
        meta.setDisplayName(increasingStr);
        meta.setLore(List.of(INCREASING_EXPLANATION));
        incrementItem.setItemMeta(meta);

        pane.bindItem(increment, new GuiItem(incrementItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(!part.penaltyIncrease(), part.penaltyAdditive());
            items.replace(type, newPart);
            updateGuiForType(type);
            update();
        }));

        // "Additive" item
        ItemStack additiveItem = new ItemStack(part.penaltyAdditive() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA);

        meta = additiveItem.getItemMeta();
        meta.setDisplayName(additiveStr);
        meta.setLore(List.of(ADDING_EXPLANATION));
        additiveItem.setItemMeta(meta);

        pane.bindItem(additive, new GuiItem(additiveItem, (event) -> {
            event.setCancelled(true);

            WorkPenaltyType.WorkPenaltyPart newPart = new WorkPenaltyType.WorkPenaltyPart(part.penaltyIncrease(), !part.penaltyAdditive());
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
        String path = ConfigOptions.WORK_PENALTY_ROOT;
        ConfigHolder configHolder = ConfigHolder.DEFAULT_CONFIG;
        FileConfiguration config = configHolder.getConfig();

        partEnum.forEach((key, value) -> {
            String partPath = path + "." + key.getTypeName();

            if (key.getDefaultPenalty().equals(value)) {
                config.set(partPath, null);
                return;
            }

            config.set(partPath + '.' + ConfigOptions.WORK_PENALTY_INCREASE, value.penaltyIncrease());
            config.set(partPath + '.' + ConfigOptions.WORK_PENALTY_ADDITIVE, value.penaltyAdditive());
        });

        return configHolder.saveToDisk(true);
    }

    @Override
    public boolean hadChange() {
        for (AnvilUseType type : items.keySet()) {
            if (!currentType.getPenaltyInfo(type).equals(items.get(type))) {
                return true;
            }
        }

        return false;
    }
}
