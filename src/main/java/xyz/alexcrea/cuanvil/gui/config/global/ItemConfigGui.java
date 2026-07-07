package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;
import xyz.alexcrea.cuanvil.util.UnitRepairUtil;

public class ItemConfigGui extends ChestGui {

    public EnchantConflictGui enchantConflictGui;
    public GroupConfigGui groupConfigGui;
    public UnitRepairConfigGui unitRepairConfigGui;
    public CustomRecipeConfigGui customRecipeConfigGui;

    public ItemConfigGui(@NotNull Material display, @NotNull NamespacedKey material) {
        super(3, CasedStringUtil.snakeToUpperSpacedCase(material.getKey().toLowerCase()) + " Config", CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "0000D0000",
                "056000780",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        ItemStack displayItemstack = new ItemStack(display);
        ItemMeta displayMeta = displayItemstack.getItemMeta();
        assert displayMeta != null;

        displayMeta.setDisplayName("§aConfiguring " + material);
        displayItemstack.setItemMeta(displayMeta);
        pane.bindItem('D', new GuiItem(displayItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));

        // Enchantment Conflicts item
        GuiItem enchantConflictItem = MainConfigGui.enchantConflictItem(getEnchantConflictGui(material));
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        GuiItem groupConfigItem = MainConfigGui.groupConfigItem(getGroupConfigGui(material));
        pane.bindItem('6', groupConfigItem);

        // Unit repair item
        GuiItem unitRepairItem = MainConfigGui.unitRepairItem(getUnitRepairConfigGui(material));
        pane.bindItem('7', unitRepairItem);

        // Custom recipe item
        GuiItem customRecipeItem = MainConfigGui.customRecipeItem(getCustomRecipeConfigGui(material));
        pane.bindItem('8', customRecipeItem);

        // quit item
        pane.bindItem('Q', MainConfigGui.quitItem());
    }

    private EnchantConflictGui getEnchantConflictGui(@NotNull NamespacedKey material) {
        if (enchantConflictGui == null) {
            enchantConflictGui = new EnchantConflictGui(this);
            enchantConflictGui.setFilter(group ->
                    group.getCantConflictGroup().contain(material)
            );
            enchantConflictGui.init();
        }

        return enchantConflictGui;
    }

    private GroupConfigGui getGroupConfigGui(@NotNull NamespacedKey material) {
        if (groupConfigGui == null) {
            groupConfigGui = new GroupConfigGui(this);
            groupConfigGui.setFilter(group ->
                    group.contain(material)
            );
            groupConfigGui.init();
        }

        return groupConfigGui;
    }

    private UnitRepairConfigGui getUnitRepairConfigGui(@NotNull NamespacedKey material) {
        if (unitRepairConfigGui == null) {
            unitRepairConfigGui = new UnitRepairConfigGui(this);
            unitRepairConfigGui.setFilter(otherMat ->
                    otherMat.equals(material) || UnitRepairUtil.INSTANCE.findRawRepairValue(
                            material, otherMat, ConfigHolder.UNIT_REPAIR_HOLDER.getConfig()) != null
            );
            unitRepairConfigGui.init();
        }

        return unitRepairConfigGui;
    }

    private CustomRecipeConfigGui getCustomRecipeConfigGui(@NotNull NamespacedKey material) {
        if (customRecipeConfigGui == null) {
            customRecipeConfigGui = new CustomRecipeConfigGui(this);
            customRecipeConfigGui.setFilter(recipe -> {
                        if (isMaterial(recipe.getLeftItem(), material)) return true;
                        if (isMaterial(recipe.getRightItem(), material)) return true;
                        return isMaterial(recipe.getResultItem(), material);
                    }
            );
            customRecipeConfigGui.init();
        }

        return customRecipeConfigGui;
    }

    private boolean isMaterial(@Nullable ItemStack item, @NotNull NamespacedKey material) {
        if (item == null) return false;

        return material.equals(MaterialUtil.INSTANCE.getCustomType(item));
    }

}
