package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.gui.config.global.*;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Collections;

public class MainConfigGui extends ChestGui {

    public final static MainConfigGui INSTANCE = new MainConfigGui();

    static {
        INSTANCE.init();
    }

    private MainConfigGui() {
        super(3, "\u00A78Anvil Config", CustomAnvil.instance);

    }

    private void init() {
        Pattern pattern = new Pattern(
                "I00000000",
                "012304567",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        ItemStack basicConfigItemstack = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta basicConfigMeta = basicConfigItemstack.getItemMeta();

        basicConfigMeta.setDisplayName("\u00A7aBasic Config Menu");
        basicConfigMeta.setLore(Collections.singletonList("\u00A77Click here to open basic config menu"));
        basicConfigItemstack.setItemMeta(basicConfigMeta);

        GuiItem basicConfigItem = GuiGlobalItems.goToGuiItem(basicConfigItemstack, BasicConfigGui.INSTANCE);
        pane.bindItem('1', basicConfigItem);

        // enchant level limit item
        ItemStack enchantLimitItemstack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta enchantLimitMeta = enchantLimitItemstack.getItemMeta();

        enchantLimitMeta.setDisplayName("\u00A7aEnchantment Level Limit");
        enchantLimitMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment level limit menu"));
        enchantLimitItemstack.setItemMeta(enchantLimitMeta);

        GuiItem enchantLimitItem = GuiGlobalItems.goToGuiItem(enchantLimitItemstack, EnchantLimitConfigGui.INSTANCE);
        pane.bindItem('2', enchantLimitItem);

        // enchant cost item
        ItemStack enchantCostItemstack = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta enchantCostMeta = enchantCostItemstack.getItemMeta();

        enchantCostMeta.setDisplayName("\u00A7aEnchantment Cost");
        enchantCostMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment costs menu"));
        enchantCostItemstack.setItemMeta(enchantCostMeta);

        GuiItem enchantCostItem = GuiGlobalItems.goToGuiItem(enchantCostItemstack, EnchantCostConfigGui.INSTANCE);
        pane.bindItem('3', enchantCostItem);

        // Enchantment Conflicts item
        ItemStack EnchantConflictItemstack = new ItemStack(Material.OAK_FENCE);
        ItemMeta enchantConflictMeta = EnchantConflictItemstack.getItemMeta();

        enchantConflictMeta.setDisplayName("\u00A7aEnchantment Conflict");
        enchantConflictMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment conflict menu"));
        EnchantConflictItemstack.setItemMeta(enchantConflictMeta);

        GuiItem enchantConflictItem = GuiGlobalItems.goToGuiItem(EnchantConflictItemstack, EnchantConflictGui.INSTANCE);
        pane.bindItem('4', enchantConflictItem);

        // WIP configuration items
        ItemStack wipItemstack = new ItemStack(Material.BARRIER);
        ItemMeta wipMeta = wipItemstack.getItemMeta();
        wipMeta.setDisplayName("\u00A7cWIP");
        wipItemstack.setItemMeta(wipMeta);

        GuiItem wip = new GuiItem(wipItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);

        pane.bindItem('5', wip);

        // Unit repair item
        ItemStack unirRepairItemstack = new ItemStack(Material.DIAMOND);
        ItemMeta unitRepairMeta = unirRepairItemstack.getItemMeta();

        unitRepairMeta.setDisplayName("\u00A7aUnit Repair");
        unitRepairMeta.setLore(Collections.singletonList("\u00A77Click here to open anvil custom recipe menu"));
        unirRepairItemstack.setItemMeta(unitRepairMeta);

        GuiItem unitRepairItem = GuiGlobalItems.goToGuiItem(unirRepairItemstack, UnitRepairConfigGui.INSTANCE);
        pane.bindItem('6', unitRepairItem);

        // Custom recipe item
        ItemStack customRecipeItemstack = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta customRecipeMeta = EnchantConflictItemstack.getItemMeta();

        customRecipeMeta.setDisplayName("\u00A7aCustom recipes");
        customRecipeMeta.setLore(Collections.singletonList("\u00A77Click here to open anvil custom recipe menu"));
        customRecipeItemstack.setItemMeta(customRecipeMeta);

        GuiItem customRecipeItem = GuiGlobalItems.goToGuiItem(customRecipeItemstack, CustomRecipeConfigGui.INSTANCE);
        pane.bindItem('7', customRecipeItem);

        // quit item
        ItemStack quitItemstack = new ItemStack(Material.BARRIER);
        ItemMeta quitMeta = quitItemstack.getItemMeta();
        quitMeta.setDisplayName("\u00A7cQuit");
        quitItemstack.setItemMeta(quitMeta);

        GuiItem quitItem = new GuiItem(quitItemstack, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }, CustomAnvil.instance);
        pane.bindItem('Q', quitItem);

        // create & bind "info" item
        ItemStack infoItemstack = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItemstack.getItemMeta();

        infoMeta.setDisplayName("\u00A7eThis is a alpha version of the gui !");
        infoMeta.setLore(Collections.singletonList("\u00A77If you have feedback or idea you can send them to the dev !"));
        infoItemstack.setItemMeta(infoMeta);

        GuiItem infoItem = new GuiItem(infoItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance);
        pane.bindItem('I', infoItem);

    }

}
