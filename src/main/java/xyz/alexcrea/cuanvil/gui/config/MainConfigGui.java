package xyz.alexcrea.cuanvil.gui.config;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.dependency.DependencyManager;
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager;
import xyz.alexcrea.cuanvil.gui.config.global.BasicConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.CustomRecipeConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConflictGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantCostConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantLimitConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantMergeLimitConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;
import xyz.alexcrea.cuanvil.gui.config.global.UnitRepairConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.function.Supplier;

@NotNullByDefault
public class MainConfigGui extends ChestGui {

    public MainConfigGui() {
        super(3, MsgUI.MAIN_TITLE.textHolder(), CustomAnvil.instance);
        init(DependencyManager.packetManager);
    }

    public void init(PacketManager packetManager) {
        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "012345678",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(9, 3, pattern);
        addPane(Slot.fromXY(0, 0), pane);

        GuiGlobalItems.addBackgroundItem(pane);

        // Basic config item
        var basicConfigItem = basicConfigItem(() -> new BasicConfigGui(() -> this, packetManager));
        pane.bindItem('1', basicConfigItem);

        // enchant level limit item
        var enchantLimitItem = enchantLimitItem(() -> {
            var value = new EnchantLimitConfigGui(() -> this);
            value.init();

            return value;
        });
        pane.bindItem('2', enchantLimitItem);

        // enchant level limit item
        var enchantMergeLimitItem = enchantMergeLimitItem(() -> new EnchantMergeLimitConfigGui(() -> this));
        pane.bindItem('3', enchantMergeLimitItem);

        // enchant cost item
        var enchantCostItem = enchantCostItem(() -> {
            var value = new EnchantCostConfigGui(() -> this);
            value.init();

            return value;
        });
        pane.bindItem('4', enchantCostItem);

        // Enchantment Conflicts item
        var enchantConflictItem = enchantConflictItem(() -> {
            var value = new EnchantConflictGui(() -> this);
            value.init();

            return value;
        });
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        var groupConfigItem = groupConfigItem(() -> {
            var gui = new GroupConfigGui(() -> this);
            gui.init();

            return gui;
        });
        pane.bindItem('6', groupConfigItem);

        // Unit repair item
        var unitRepairItem = unitRepairItem(() -> {
            var gui = new UnitRepairConfigGui(() -> this);
            gui.init();

            return gui;
        });
        pane.bindItem('7', unitRepairItem);

        // Custom recipe item
        var customRecipeItem = customRecipeItem(() -> {
            var gui = new CustomRecipeConfigGui(() -> this);
            gui.init();

            return gui;
        });
        pane.bindItem('8', customRecipeItem);

        // quit item
        pane.bindItem('Q', quitItem());
    }

    public static GuiItem basicConfigItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.COMMAND_BLOCK);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_BASIC_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_BASIC_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantLimitItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.ENCHANTED_BOOK);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_ENCHANT_LEVEL_LIMIT_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_ENCHANT_LEVEL_LIMIT_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantMergeLimitItem(Supplier<? extends Gui> target) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_ENCHANT_MERGE_LIMIT_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_ENCHANT_MERGE_LIMIT_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantCostItem(Supplier<? extends Gui> target) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_ENCHANT_COST_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_ENCHANT_COST_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem enchantConflictItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.OAK_FENCE);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_ENCHANT_CONFLICT_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_ENCHANT_CONFLICT_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem groupConfigItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.CHEST);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_ITEM_GROUP_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_ITEM_GROUP_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem unitRepairItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.DIAMOND);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_UNIT_REPAIR_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_UNIT_REPAIR_LORE);
        item.setItemMeta(meta);

        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem customRecipeItem(Supplier<? extends Gui> target) {
        var item = new ItemStack(Material.CRAFTING_TABLE);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.MAIN_CUSTOM_RECIPE_TITLE);
        ComponentUtil.applyLore(meta, MsgUI.MAIN_CUSTOM_RECIPE_LORE);
        item.setItemMeta(meta);
        return GuiGlobalItems.goToGuiItem(item, target);
    }

    public static GuiItem quitItem() {
        var item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.SHARED_QUIT);
        item.setItemMeta(meta);

        return new GuiItem(item, event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }, CustomAnvil.instance);
    }

}
