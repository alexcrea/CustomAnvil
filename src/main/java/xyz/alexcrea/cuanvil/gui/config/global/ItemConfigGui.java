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
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Collections;

public class ItemConfigGui extends ChestGui {

    public EnchantConflictGui enchantConflictGui;
    public GroupConfigGui groupConfigGui;
    public UnitRepairConfigGui unitRepairConfigGui;
    public CustomRecipeConfigGui customRecipeConfigGui;

    public ItemConfigGui(Material display, NamespacedKey material) {
        super(3, material.getKey() + " Config", CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "0000D0000",
                "056000780",
                "000000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        ItemStack displayItemstack = new ItemStack(display);
        ItemMeta displayMeta = displayItemstack.getItemMeta();
        assert displayMeta != null;

        displayMeta.setDisplayName("§aConfiguring " + material);
        displayItemstack.setItemMeta(displayMeta);
        pane.bindItem('D',  new GuiItem(displayItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));

        // Enchantment Conflicts item
        ItemStack enchantConflictItemstack = new ItemStack(Material.OAK_FENCE);
        ItemMeta enchantConflictMeta = enchantConflictItemstack.getItemMeta();
        assert enchantConflictMeta != null;

        enchantConflictMeta.setDisplayName("§aEnchantment Conflict");
        enchantConflictMeta.setLore(Collections.singletonList("§7Click here to open enchantment conflict menu"));
        enchantConflictItemstack.setItemMeta(enchantConflictMeta);

        GuiItem enchantConflictItem = GuiGlobalItems.goToGuiItem(enchantConflictItemstack, getEnchantConflictGui(material));
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        ItemStack groupItemstack = new ItemStack(Material.CHEST);
        ItemMeta groupMeta = groupItemstack.getItemMeta();
        assert groupMeta != null;

        groupMeta.setDisplayName("§aItem Groups");
        groupMeta.setLore(Collections.singletonList("§7Click here to open item group menu"));
        groupItemstack.setItemMeta(groupMeta);

        GuiItem groupConfigItem = GuiGlobalItems.goToGuiItem(groupItemstack, getGroupConfigGui(material));

        pane.bindItem('6', groupConfigItem);

        // Unit repair item
        ItemStack unirRepairItemstack = new ItemStack(Material.DIAMOND);
        ItemMeta unitRepairMeta = unirRepairItemstack.getItemMeta();
        assert unitRepairMeta != null;

        unitRepairMeta.setDisplayName("§aUnit Repair");
        unitRepairMeta.setLore(Collections.singletonList("§7Click here to open anvil unit repair menu"));
        unirRepairItemstack.setItemMeta(unitRepairMeta);

        //TODO
        GuiItem unitRepairItem = GuiGlobalItems.goToGuiItem(unirRepairItemstack, UnitRepairConfigGui.getInstance());
        pane.bindItem('7', unitRepairItem);

        // Custom recipe item
        ItemStack customRecipeItemstack = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta customRecipeMeta = customRecipeItemstack.getItemMeta();
        assert customRecipeMeta != null;

        customRecipeMeta.setDisplayName("§aCustom recipes");
        customRecipeMeta.setLore(Collections.singletonList("§7Click here to open anvil custom recipe menu"));
        customRecipeItemstack.setItemMeta(customRecipeMeta);

        //TODO
        GuiItem customRecipeItem = GuiGlobalItems.goToGuiItem(customRecipeItemstack, CustomRecipeConfigGui.getInstance());
        pane.bindItem('8', customRecipeItem);
    }

    private EnchantConflictGui getEnchantConflictGui(NamespacedKey material) {
        if (enchantConflictGui == null) {
            enchantConflictGui = new EnchantConflictGui(this);
            enchantConflictGui.setFilter(group ->
                    group.getCantConflictGroup().contain(material)
            );
            enchantConflictGui.init();
        }

        return enchantConflictGui;
    }

    private GroupConfigGui getGroupConfigGui(NamespacedKey material) {
        if (groupConfigGui == null) {
            groupConfigGui = new GroupConfigGui(this);
            groupConfigGui.setFilter(group ->
                    group.contain(material)
            );
            groupConfigGui.init();
        }

        return groupConfigGui;
    }

    /*private UnitRepairConfigGui getUnitRepairConfigGui(NamespacedKey material) {
        if (unitRepairConfigGui == null) {
            unitRepairConfigGui = new UnitRepairConfigGui(this);
            unitRepairConfigGui.setFilter(otherMat ->
                    group.contain(material) //TODO check material & what inside
            );
            unitRepairConfigGui.init();
        }

        return unitRepairConfigGui;
    }*/

}
