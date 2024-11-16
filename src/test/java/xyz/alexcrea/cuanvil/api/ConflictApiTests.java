package xyz.alexcrea.cuanvil.api;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;
import xyz.alexcrea.cuanvil.util.CommonItemUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConflictApiTests extends ConfigResetCustomAnvilTest {

    private AnvilInventory anvil;
    private PlayerMock player;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Mock used player & open anvil
        player = server.addPlayer();

        Inventory anvil = server.createInventory(player, InventoryType.ANVIL);

        this.anvil = (AnvilInventory) anvil;
        player.openInventory(anvil);

        ConfigHolder.DEFAULT_CONFIG.getConfig().set("debug_log", true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set("debug_log_verbose", true);
    }

    @Test
    public void testConflict() {
        ItemStack sharpness1 = CommonItemUtil.sharpness(1);
        ItemStack arthropods1 = CommonItemUtil.bane_of_arthropods(1);
        ItemStack illegalResult = AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("bane_of_arthropods", "sharpness"),
                1, 1
        );

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                sharpness1, arthropods1,
                null
        );

        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                sharpness1, arthropods1,
                illegalResult,
                // TODO add expected price
                null
        );

        CAEnchantment sharpness = EnchantmentApi.getByKey(Enchantment.SHARPNESS.getKey());
        Assertions.assertNotNull(sharpness);

        // Testing default conflict (illegal item should not be produced)
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);

        // Try to find & remove conflict
        EnchantConflictGroup conflict = findGroup("sword_enchant_conflict");
        Assertions.assertNotNull(conflict, "Could not find conflict.");

        // Test what happen when we remove the conflict (illegal item should be allowed)
        ConflictAPI.removeConflict(conflict);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);

        // We create and add a new conflict
        ConflictBuilder builder = new ConflictBuilder("sword_enchant_conflict");
        builder.addEnchantment("bane_of_arthropods").addEnchantment(sharpness); //TODO maybe add "add by bukkit enchantment"
        builder.setMaxBeforeConflict(1);

        // Nothing should change as it is not new: it was previously deleted
        Assertions.assertFalse(builder.registerIfNew());
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);

        // Now the conflict should be registered and conflict should exist
        Assertions.assertTrue(builder.registerIfAbsent());
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);
    }

    @Test
    void writeGroup_Reload() {
        String conflictName = "conflict";
        ConflictBuilder builder = new ConflictBuilder(conflictName);

        // Group not being set should not exist
        assertFalse(doGroupExist(conflictName));

        // Add group and reload
        assertTrue(ConflictAPI.writeConflict(builder));
        assertFalse(doGroupExist(conflictName));

        // Tick so write get reloaded
        server.getScheduler().performOneTick();

        assertTrue(doGroupExist(conflictName));
    }

    @Test
    void writeGroup_InvalidDot() {
        String conflictName = "conflict.conflict";
        ConflictBuilder builder = new ConflictBuilder(conflictName);

        // Try write group
        assertFalse(ConflictAPI.writeConflict(builder));
    }

    // Maybe move to ConflictApi class ?
   private static boolean doGroupExist(@NotNull String groupName) {
        return findGroup(groupName) != null;
    }

    // Maybe move to ConflictApi class ?
    @Nullable
    private static EnchantConflictGroup findGroup(@NotNull String groupName){
        for (EnchantConflictGroup enchantConflictGroup : ConflictAPI.getRegisteredConflict()) {
            if (groupName.equalsIgnoreCase(enchantConflictGroup.getName())) {
                return enchantConflictGroup;
            }
        }
        return null;
    }

}
