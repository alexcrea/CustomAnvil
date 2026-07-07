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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;
import xyz.alexcrea.cuanvil.util.UnitRepairUtil;

import java.lang.ref.PhantomReference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EnchantConfigGui extends ChestGui {

    public EnchantLimitConfigGui enchantLimitConfigGui;
    public EnchantMergeLimitConfigGui enchantMergeLimitConfigGui;
    public EnchantCostConfigGui enchantCostConfigGui;
    public EnchantConflictGui enchantConflictGui;
    public GroupConfigGui groupConfigGui;

    public EnchantConfigGui(@NotNull Set<CAEnchantment> enchantments) {
        super(3,
                "Configuring Enchantments",
                CustomAnvil.instance);

        Pattern pattern = new Pattern(
                "0000D0000",
                "023405600",
                "Q00000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(pane);

        GuiGlobalItems.addBackgroundItem(pane);

        ItemStack displayItemstack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta displayMeta = displayItemstack.getItemMeta();
        assert displayMeta != null;

        displayMeta.setDisplayName("§aConfiguring Enchantments:");
        displayItemstack.setItemMeta(displayMeta);

        // Set enchantments
        HashMap<CAEnchantment, Integer> enchantmentMap = new HashMap<>();
        for (CAEnchantment enchantment : enchantments) {
            enchantmentMap.put(enchantment, 1);
        }
        EnchantmentApi.setEnchantments(displayItemstack, enchantmentMap);

        pane.bindItem('D', new GuiItem(displayItemstack, GuiGlobalActions.stayInPlace, CustomAnvil.instance));

        // enchant level limit item
        var enchantLimitItem = MainConfigGui.enchantLimitItem(new EnchantLimitConfigGui()); //TODO
        pane.bindItem('2', enchantLimitItem);

        // enchant level limit item
        var enchantMergeLimitItem = MainConfigGui.enchantMergeLimitItem(new EnchantMergeLimitConfigGui()); //TODO
        pane.bindItem('3', enchantMergeLimitItem);

        // enchant cost item
        var enchantCostItem = MainConfigGui.enchantCostItem(new EnchantCostConfigGui()); //TODO
        pane.bindItem('4', enchantCostItem);

        // Enchantment Conflicts item
        var enchantConflictItem = MainConfigGui.enchantConflictItem(getEnchantConflictGui(enchantments));
        pane.bindItem('5', enchantConflictItem);

        // Group config items
        var groupConfigItem = MainConfigGui.groupConfigItem(getGroupConfigGui(enchantments));
        pane.bindItem('6', groupConfigItem);

        // quit item
        pane.bindItem('Q', MainConfigGui.quitItem());
    }

    @NotNull
    @Contract(pure = true)
    private Predicate<EnchantConflictGroup> getGroupFilter(@NotNull Set<CAEnchantment> enchantments) {
        return group -> group.getEnchants()
                .stream()
                .anyMatch(enchantments::contains);
    }

    private EnchantConflictGui getEnchantConflictGui(@NotNull Set<CAEnchantment> enchantments) {
        if (enchantConflictGui == null) {
            enchantConflictGui = new EnchantConflictGui(this);
            enchantConflictGui.setFilter(getGroupFilter(enchantments));
            enchantConflictGui.init();
        }

        return enchantConflictGui;
    }

    private GroupConfigGui getGroupConfigGui(@NotNull Set<CAEnchantment> enchantments) {
        if (groupConfigGui == null) {
            groupConfigGui = new GroupConfigGui(this);

            // Get all the conflict related to this enchantment
            var groups = ConfigHolder.CONFLICT_HOLDER
                    .getConflictManager()
                    .getConflictList()
                    .stream()
                    .filter(getGroupFilter(enchantments))
                    .map(EnchantConflictGroup::getCantConflictGroup)
                    .collect(Collectors.toSet());

            groupConfigGui.setFilter(group ->
                groups.stream().anyMatch(other -> other.isReferencing(group))
            );
            groupConfigGui.init();
        }

        return groupConfigGui;
    }

}
