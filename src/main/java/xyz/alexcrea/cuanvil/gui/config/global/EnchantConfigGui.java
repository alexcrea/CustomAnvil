package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EnchantConfigGui extends ChestGui implements ValueUpdatableGui {

    private final Set<CAEnchantment> enchantments;
    private final PatternPane pane;

    private EnchantLimitConfigGui enchantLimitConfigGui;
    private EnchantMergeLimitConfigGui enchantMergeLimitConfigGui;
    private EnchantCostConfigGui enchantCostConfigGui;

    private EnchantConflictGui enchantConflictGui;
    private GroupConfigGui groupConfigGui;

    private static String selectName(@NotNull Set<CAEnchantment> enchantments) {
        if(enchantments.size() == 1) {
            return enchantments.stream().findFirst().get().getPrettyName();
        }

        return MsgUI.INSTANCE.getENCHANT_CONFIG_MULTIPLES_NAME().unformatted();
    }

    public EnchantConfigGui(@NotNull Set<CAEnchantment> enchantments) {
        super(3,
                MsgUI.INSTANCE.getENCHANT_CONFIG_TITLE().textHolder(selectName(enchantments)),
                CustomAnvil.instance);
        this.enchantments = enchantments;

        Pattern pattern = new Pattern(
                "0000D0000",
                "023405600",
                "Q00000000"
        );
        pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        ItemStack displayItemstack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta displayMeta = displayItemstack.getItemMeta();
        assert displayMeta != null;

        ComponentUtil.INSTANCE.setMessageName(displayMeta, MsgUI.INSTANCE.getENCHANT_CONFIG_NAME(), selectName(enchantments));
        displayItemstack.setItemMeta(displayMeta);

        // Set enchantments
        HashMap<CAEnchantment, Integer> enchantmentMap = new HashMap<>();
        for (CAEnchantment enchantment : enchantments) {
            enchantmentMap.put(enchantment, 1);
        }
        EnchantmentApi.setEnchantments(displayItemstack, enchantmentMap);

        pane.bindItem('D', new GuiItem(displayItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));

        updateGuiValues();

        // Enchantment Conflicts item
        var enchantConflictItem = MainConfigGui.enchantConflictItem(getEnchantConflictGui());
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        var groupConfigItem = MainConfigGui.groupConfigItem(getGroupConfigGui());
        pane.bindItem('6', groupConfigItem);

        // quit item
        pane.bindItem('Q', MainConfigGui.quitItem());
    }

    private GuiItem enchantLimitConfigGui() {
        if (enchantLimitConfigGui == null) {
            enchantLimitConfigGui = new EnchantLimitConfigGui(this);
            enchantLimitConfigGui.setFilter(enchantments::contains);
            enchantLimitConfigGui.init();
        }

        // Bypass
        if(enchantments.size() == 1) {
            var enchant = enchantments.iterator().next();
            var factory = EnchantLimitConfigGui.createFactory(enchant, this);

            return enchantLimitConfigGui.itemFromFactory(enchant, factory);
        }

        return MainConfigGui.enchantLimitItem(enchantLimitConfigGui);
    }

    private GuiItem enchantMergeLimitConfigGui() {
        if (enchantMergeLimitConfigGui == null) {
            enchantMergeLimitConfigGui = new EnchantMergeLimitConfigGui(this);
            enchantMergeLimitConfigGui.setFilter(enchantments::contains);
            enchantMergeLimitConfigGui.init();
        }

        // Bypass
        if(enchantments.size() == 1) {
            var enchant = enchantments.iterator().next();
            var factory = EnchantMergeLimitConfigGui.createFactory(enchant, this);

            return enchantMergeLimitConfigGui.itemFromFactory(enchant, factory);
        }

        return MainConfigGui.enchantMergeLimitItem(enchantMergeLimitConfigGui);
    }

    private GuiItem enchantCostConfigGui() {
        if (enchantCostConfigGui == null) {
            enchantCostConfigGui = new EnchantCostConfigGui(this);
            enchantCostConfigGui.setFilter(enchantments::contains);
            enchantCostConfigGui.init();
        }

        // Bypass
        if(enchantments.size() == 1) {
            var enchant = enchantments.iterator().next();
            var factory = EnchantCostConfigGui.createFactory(enchant, this);

            return enchantCostConfigGui.itemFromFactory(enchant, factory);
        }

        return MainConfigGui.enchantCostItem(enchantCostConfigGui);
    }

    @NotNull
    @Contract(pure = true)
    private Predicate<EnchantConflictGroup> getGroupFilter() {
        return group -> group.getEnchants()
                .stream()
                .anyMatch(enchantments::contains);
    }

    private EnchantConflictGui getEnchantConflictGui() {
        if (enchantConflictGui == null) {
            enchantConflictGui = new EnchantConflictGui(this);
            enchantConflictGui.setFilter(getGroupFilter());
            enchantConflictGui.init();
        }

        return enchantConflictGui;
    }

    private GroupConfigGui getGroupConfigGui() {
        if (groupConfigGui == null) {
            groupConfigGui = new GroupConfigGui(this);

            // Get all the conflict related to this enchantment
            var groups = ConfigHolder.CONFLICT_HOLDER
                    .getConflictManager()
                    .getConflictList()
                    .stream()
                    .filter(getGroupFilter())
                    .map(EnchantConflictGroup::getCantConflictGroup)
                    .collect(Collectors.toSet());

            groupConfigGui.setFilter(group ->
                groups.stream().anyMatch(other -> other.isReferencing(group))
            );
            groupConfigGui.init();
        }

        return groupConfigGui;
    }

    @Override
    public void updateGuiValues() {
        // enchant level limit item
        pane.bindItem('2', enchantLimitConfigGui());

        // enchant level limit item
        pane.bindItem('3', enchantMergeLimitConfigGui());

        // enchant cost item
        pane.bindItem('4', enchantCostConfigGui());
    }

    @Override
    public Gui getConnectedGui() {
        return this;
    }
}
