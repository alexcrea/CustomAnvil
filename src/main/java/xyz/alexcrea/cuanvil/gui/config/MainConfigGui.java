package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerBase;
import xyz.alexcrea.cuanvil.gui.config.global.*;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class MainConfigGui extends ChestGui {

    private static final MainConfigGui INSTANCE = new MainConfigGui();

    public static MainConfigGui getInstance() {
        return INSTANCE;
    }

    private MainConfigGui() {
        super(3, "§8Anvil Config", CustomAnvil.instance);
    }

    public void init(PacketManagerBase packetManager) {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "012345678",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        ItemStack basicConfigItemstack = ItemType.COMMAND_BLOCK.createItemStack();
        ItemMeta basicConfigMeta = basicConfigItemstack.getItemMeta();
        assert basicConfigMeta != null;

        basicConfigMeta.setDisplayName("§aBasic Config Menu");
        basicConfigMeta.setLore(Collections.singletonList("§7Click here to open basic config menu"));
        basicConfigItemstack.setItemMeta(basicConfigMeta);

        GuiItem basicConfigItem = GuiGlobalItems.goToGuiItem(basicConfigItemstack, new BasicConfigGui(packetManager));
        pane.bindItem('1', basicConfigItem);

        // enchant level limit item
        ItemStack enchantLimitItemstack = ItemType.ENCHANTED_BOOK.createItemStack();
        ItemMeta enchantLimitMeta = enchantLimitItemstack.getItemMeta();
        assert enchantLimitMeta != null;

        enchantLimitMeta.setDisplayName("§aEnchantment Level Limit");
        enchantLimitMeta.setLore(Collections.singletonList("§7Click here to open enchantment level limit menu"));
        enchantLimitItemstack.setItemMeta(enchantLimitMeta);

        GuiItem enchantLimitItem = GuiGlobalItems.goToGuiItem(enchantLimitItemstack, new EnchantLimitConfigGui());
        pane.bindItem('2', enchantLimitItem);

        // enchant level limit item
        ItemStack enchantMergeLimitItemstack = ItemType.ENCHANTED_BOOK.createItemStack();
        ItemMeta enchantMergeLimitMeta = enchantMergeLimitItemstack.getItemMeta();
        assert enchantMergeLimitMeta != null;

        enchantMergeLimitMeta.setDisplayName("§aEnchantment Merge Limit");
        enchantMergeLimitMeta.setLore(Collections.singletonList("§7Click here to open enchantment merge limit menu"));
        enchantMergeLimitItemstack.setItemMeta(enchantMergeLimitMeta);

        GuiItem enchantMergeLimitItem = GuiGlobalItems.goToGuiItem(enchantMergeLimitItemstack, new EnchantMergeLimitConfigGui());
        pane.bindItem('3', enchantMergeLimitItem);

        // enchant cost item
        ItemStack enchantCostItemstack = ItemType.EXPERIENCE_BOTTLE.createItemStack();
        ItemMeta enchantCostMeta = enchantCostItemstack.getItemMeta();
        assert enchantCostMeta != null;

        enchantCostMeta.setDisplayName("§aEnchantment Cost");
        enchantCostMeta.setLore(Collections.singletonList("§7Click here to open enchantment costs menu"));
        enchantCostItemstack.setItemMeta(enchantCostMeta);

        GuiItem enchantCostItem = GuiGlobalItems.goToGuiItem(enchantCostItemstack, new EnchantCostConfigGui());
        pane.bindItem('4', enchantCostItem);

        // Enchantment Conflicts item
        ItemStack enchantConflictItemstack = ItemType.OAK_FENCE.createItemStack();
        ItemMeta enchantConflictMeta = enchantConflictItemstack.getItemMeta();
        assert enchantConflictMeta != null;

        enchantConflictMeta.setDisplayName("§aEnchantment Conflict");
        enchantConflictMeta.setLore(Collections.singletonList("§7Click here to open enchantment conflict menu"));
        enchantConflictItemstack.setItemMeta(enchantConflictMeta);

        GuiItem enchantConflictItem = GuiGlobalItems.goToGuiItem(enchantConflictItemstack, EnchantConflictGui.getInstance());
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        ItemStack groupItemstack = ItemType.CHEST.createItemStack();
        ItemMeta groupMeta = groupItemstack.getItemMeta();
        assert groupMeta != null;

        groupMeta.setDisplayName("§aItem Groups");
        groupMeta.setLore(Collections.singletonList("§7Click here to open item group menu"));
        groupItemstack.setItemMeta(groupMeta);

        GuiItem groupConfigItem = GuiGlobalItems.goToGuiItem(groupItemstack, GroupConfigGui.getInstance());

        pane.bindItem('6', groupConfigItem);

        // Unit repair item
        ItemStack unirRepairItemstack = ItemType.DIAMOND.createItemStack();
        ItemMeta unitRepairMeta = unirRepairItemstack.getItemMeta();
        assert unitRepairMeta != null;

        unitRepairMeta.setDisplayName("§aUnit Repair");
        unitRepairMeta.setLore(Collections.singletonList("§7Click here to open anvil unit repair menu"));
        unirRepairItemstack.setItemMeta(unitRepairMeta);

        GuiItem unitRepairItem = GuiGlobalItems.goToGuiItem(unirRepairItemstack, UnitRepairConfigGui.getInstance());
        pane.bindItem('7', unitRepairItem);

        // Custom recipe item
        ItemStack customRecipeItemstack = ItemType.CRAFTING_TABLE.createItemStack();
        ItemMeta customRecipeMeta = customRecipeItemstack.getItemMeta();
        assert customRecipeMeta != null;

        customRecipeMeta.setDisplayName("§aCustom recipes");
        customRecipeMeta.setLore(Collections.singletonList("§7Click here to open anvil custom recipe menu"));
        customRecipeItemstack.setItemMeta(customRecipeMeta);

        GuiItem customRecipeItem = GuiGlobalItems.goToGuiItem(customRecipeItemstack, CustomRecipeConfigGui.getInstance());
        pane.bindItem('8', customRecipeItem);

        // quit item
        ItemStack quitItemstack = ItemType.BARRIER.createItemStack();
        ItemMeta quitMeta = quitItemstack.getItemMeta();
        assert quitMeta != null;

        quitMeta.setDisplayName("§cQuit");
        quitItemstack.setItemMeta(quitMeta);

        GuiItem quitItem = new GuiItem(quitItemstack, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }, CustomAnvil.instance);
        pane.bindItem('Q', quitItem);

    }

}
