package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager;
import xyz.alexcrea.cuanvil.gui.config.global.*;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;

import java.util.Collections;

public class MainConfigGui extends ChestGui {

    private static final MainConfigGui INSTANCE = new MainConfigGui();

    public static MainConfigGui getInstance() {
        return INSTANCE;
    }

    private MainConfigGui() {
        super(3, "§8Anvil Config", CustomAnvil.instance);
    }

    public void init(PacketManager packetManager) {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "012345678",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        var basicConfigItem = basicConfigItem(new BasicConfigGui(packetManager));
        pane.bindItem('1', basicConfigItem);

        // enchant level limit item
        var enchantLimitItem = enchantLimitItem(new EnchantLimitConfigGui());
        pane.bindItem('2', enchantLimitItem);

        // enchant level limit item
        var enchantMergeLimitItem = enchantMergeLimitItem(new EnchantMergeLimitConfigGui());
        pane.bindItem('3', enchantMergeLimitItem);

        // enchant cost item
        var enchantCostItem = enchantCostItem(new EnchantCostConfigGui());
        pane.bindItem('4', enchantCostItem);

        // Enchantment Conflicts item
        var enchantConflictItem = enchantConflictItem(EnchantConflictGui.getInstance());
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        var groupConfigItem = groupConfigItem(GroupConfigGui.getInstance());
        pane.bindItem('6', groupConfigItem);

        // Unit repair item
        var unitRepairItem = unitRepairItem(UnitRepairConfigGui.getInstance());
        pane.bindItem('7', unitRepairItem);

        // Custom recipe item
        var customRecipeItem = customRecipeItem(CustomRecipeConfigGui.getInstance());
        pane.bindItem('8', customRecipeItem);

        // quit item
        pane.bindItem('Q', quitItem());
    }

    public static GuiItem basicConfigItem(Gui target) {
        var item = new ItemStack(Material.COMMAND_BLOCK);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aBasic Config Menu");
        meta.setLore(Collections.singletonList("§7Click here to open basic config menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantLimitItem(Gui target) {
        var item = new ItemStack(Material.ENCHANTED_BOOK);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aEnchantment Level Limit");
        meta.setLore(Collections.singletonList("§7Click here to open enchantment level limit menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantMergeLimitItem(Gui target) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aEnchantment Merge Limit");
        meta.setLore(Collections.singletonList("§7Click here to open enchantment merge limit menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantCostItem(Gui target) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aEnchantment Cost");
        meta.setLore(Collections.singletonList("§7Click here to open enchantment costs menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantConflictItem(Gui target) {
        var item = new ItemStack(Material.OAK_FENCE);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aEnchantment Conflict");
        meta.setLore(Collections.singletonList("§7Click here to open enchantment conflict menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem groupConfigItem(Gui target) {
        var item = new ItemStack(Material.CHEST);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aItem Groups");
        meta.setLore(Collections.singletonList("§7Click here to open item group menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem unitRepairItem(Gui target) {
        var item = new ItemStack(Material.DIAMOND);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aUnit Repair");
        meta.setLore(Collections.singletonList("§7Click here to open anvil unit repair menu"));
        item.setItemMeta(meta);

        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem customRecipeItem(Gui target) {
        var item = new ItemStack(Material.CRAFTING_TABLE);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§aCustom recipes");
        meta.setLore(Collections.singletonList("§7Click here to open anvil custom recipe menu"));
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem quitItem() {
        var item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§cQuit");
        item.setItemMeta(meta);

        return new GuiItem(item, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }, CustomAnvil.instance);
    }

}
