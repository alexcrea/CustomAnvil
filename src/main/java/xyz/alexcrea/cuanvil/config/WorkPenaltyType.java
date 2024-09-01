package xyz.alexcrea.cuanvil.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.gui.config.settings.AbstractSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnumSettingGui;

import java.util.ArrayList;
import java.util.List;

public enum WorkPenaltyType implements EnumSettingGui.ConfigurableEnum {
    DEFAULT("default",  true, true, "§aDefault", Material.LIME_TERRACOTTA),
    ADDITIVE("add_only", false, true, "§eAdd Only", Material.YELLOW_TERRACOTTA),
    INCREASE("increase_only", true, false, "§eIncrease Only", Material.YELLOW_TERRACOTTA),
    DISABLED("disabled", false, false, "§cDisabled", Material.RED_TERRACOTTA),
    ;

    private final String name;
    private final boolean penaltyIncrease;
    private final boolean penaltyAdditive;

    private final String configName;
    private final Material configMaterial;

    WorkPenaltyType(String name, boolean penaltyIncrease, boolean penaltyAdditive, String configName, Material configMaterial) {
        this.name = name;
        this.penaltyIncrease = penaltyIncrease;
        this.penaltyAdditive = penaltyAdditive;
        this.configName = configName;
        this.configMaterial = configMaterial;
    }

    public boolean isPenaltyIncreasing() {
        return penaltyIncrease;
    }

    public boolean isPenaltyAdditive() {
        return penaltyAdditive;
    }

    private boolean doRepresentThisType(String toTest){
        return name.equalsIgnoreCase(toTest);
    }

    @NotNull
    public static WorkPenaltyType fromString(@Nullable String toTest){
        if(toTest == null) return DEFAULT;

        // Test if it matches any of values
        for (WorkPenaltyType value : values()) {
            if(value.doRepresentThisType(toTest)){
                return value;
            }
        }

        // Use default if not found
        return DEFAULT;
    }

    @NotNull
    public static WorkPenaltyType next(@NotNull WorkPenaltyType now){
        return switch (now){
            case DEFAULT -> ADDITIVE;
            case ADDITIVE -> INCREASE;
            case INCREASE -> DISABLED;
            case DISABLED -> DEFAULT;

        };

    }

    @Override
    public ItemStack configurationGuiItem() {
        ItemStack displayedItem = new ItemStack(this.configMaterial);
        ItemMeta valueMeta = displayedItem.getItemMeta();
        assert valueMeta != null;

        valueMeta.setDisplayName(this.configName);

        List<String> lore = new ArrayList<>();

        lore.add(configDisplayForAdd());
        lore.add(configDisplayForIncrease());
        lore.add("");

        lore.add(AbstractSettingGui.CLICK_LORE);
        valueMeta.setLore(lore);

        displayedItem.setItemMeta(valueMeta);

        return displayedItem;
    }

    public String configDisplayForAdd(){
        return ("§7Add penalty:        " + (penaltyAdditive ? "§aYes" : "§cNo"));
    }

    public String configDisplayForIncrease(){
        return ("§7Increase penalty: " + (penaltyIncrease ? "§aYes" : "§cNo"));
    }

    @Override
    public String configName() {
        return this.name;
    }

    @Override
    public String configurationGuiName() {
        return this.configName;
    }
}
