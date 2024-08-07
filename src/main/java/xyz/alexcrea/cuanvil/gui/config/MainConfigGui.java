package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager;
import xyz.alexcrea.cuanvil.gui.config.global.*;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;

import java.util.Collections;

public class MainConfigGui extends ChestGui {

    private static final MainConfigGui INSTANCE = new MainConfigGui();

    public static MainConfigGui getInstance() {
        return INSTANCE;
    }

    private MainConfigGui() {
        super(3, "\u00A78Anvil Config", CustomAnvil.instance);
    }

    public void init(PacketManager packetManager) {
        Pattern pattern = new Pattern(
                "000000000",
                "012304567",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        ItemStack basicConfigItemstack = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta basicConfigMeta = basicConfigItemstack.getItemMeta();
        assert basicConfigMeta != null;

        basicConfigMeta.setDisplayName("\u00A7aBasic Config Menu");
        basicConfigMeta.setLore(Collections.singletonList("\u00A77Click here to open basic config menu"));
        basicConfigItemstack.setItemMeta(basicConfigMeta);

        GuiItem basicConfigItem = GuiGlobalItems.goToGuiItem(basicConfigItemstack, new BasicConfigGui(packetManager));
        pane.bindItem('1', basicConfigItem);

        // enchant level limit item
        ItemStack enchantLimitItemstack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta enchantLimitMeta = enchantLimitItemstack.getItemMeta();
        assert enchantLimitMeta != null;

        enchantLimitMeta.setDisplayName("\u00A7aEnchantment Level Limit");
        enchantLimitMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment level limit menu"));
        enchantLimitItemstack.setItemMeta(enchantLimitMeta);

        GuiItem enchantLimitItem = GuiGlobalItems.goToGuiItem(enchantLimitItemstack, new EnchantLimitConfigGui());
        pane.bindItem('2', enchantLimitItem);

        // enchant cost item
        ItemStack enchantCostItemstack = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta enchantCostMeta = enchantCostItemstack.getItemMeta();
        assert enchantCostMeta != null;

        enchantCostMeta.setDisplayName("\u00A7aEnchantment Cost");
        enchantCostMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment costs menu"));
        enchantCostItemstack.setItemMeta(enchantCostMeta);

        GuiItem enchantCostItem = GuiGlobalItems.goToGuiItem(enchantCostItemstack, new EnchantCostConfigGui());
        pane.bindItem('3', enchantCostItem);

        // Enchantment Conflicts item
        ItemStack enchantConflictItemstack = new ItemStack(Material.OAK_FENCE);
        ItemMeta enchantConflictMeta = enchantConflictItemstack.getItemMeta();
        assert enchantConflictMeta != null;

        enchantConflictMeta.setDisplayName("\u00A7aEnchantment Conflict");
        enchantConflictMeta.setLore(Collections.singletonList("\u00A77Click here to open enchantment conflict menu"));
        enchantConflictItemstack.setItemMeta(enchantConflictMeta);

        GuiItem enchantConflictItem = GuiGlobalItems.goToGuiItem(enchantConflictItemstack, EnchantConflictGui.getInstance());
        pane.bindItem('4', enchantConflictItem);

        // Group config items
        ItemStack groupItemstack = new ItemStack(Material.CHEST);
        ItemMeta groupMeta = groupItemstack.getItemMeta();
        assert groupMeta != null;

        groupMeta.setDisplayName("\u00A7aGroups");
        groupMeta.setLore(Collections.singletonList("\u00A77Click here to open material group menu"));
        groupItemstack.setItemMeta(groupMeta);

        GuiItem groupConfigItem = GuiGlobalItems.goToGuiItem(groupItemstack, GroupConfigGui.getInstance());

        pane.bindItem('5', groupConfigItem);

        // Unit repair item
        ItemStack unirRepairItemstack = new ItemStack(Material.DIAMOND);
        ItemMeta unitRepairMeta = unirRepairItemstack.getItemMeta();
        assert unitRepairMeta != null;

        unitRepairMeta.setDisplayName("\u00A7aUnit Repair");
        unitRepairMeta.setLore(Collections.singletonList("\u00A77Click here to open anvil unit repair menu"));
        unirRepairItemstack.setItemMeta(unitRepairMeta);

        GuiItem unitRepairItem = GuiGlobalItems.goToGuiItem(unirRepairItemstack, UnitRepairConfigGui.getInstance());
        pane.bindItem('6', unitRepairItem);

        // Custom recipe item
        ItemStack customRecipeItemstack = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta customRecipeMeta = customRecipeItemstack.getItemMeta();
        assert customRecipeMeta != null;

        customRecipeMeta.setDisplayName("\u00A7aCustom recipes");
        customRecipeMeta.setLore(Collections.singletonList("\u00A77Click here to open anvil custom recipe menu"));
        customRecipeItemstack.setItemMeta(customRecipeMeta);

        GuiItem customRecipeItem = GuiGlobalItems.goToGuiItem(customRecipeItemstack, CustomRecipeConfigGui.getInstance());
        pane.bindItem('7', customRecipeItem);

        // quit item
        ItemStack quitItemstack = new ItemStack(Material.BARRIER);
        ItemMeta quitMeta = quitItemstack.getItemMeta();
        assert quitMeta != null;

        quitMeta.setDisplayName("\u00A7cQuit");
        quitItemstack.setItemMeta(quitMeta);

        GuiItem quitItem = new GuiItem(quitItemstack, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }, CustomAnvil.instance);
        pane.bindItem('Q', quitItem);

    }

}
