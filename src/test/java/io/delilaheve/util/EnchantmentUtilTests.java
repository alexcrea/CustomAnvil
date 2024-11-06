package io.delilaheve.util;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.ItemStackMock;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.api.EnchantmentApi;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;

import java.util.List;

public class EnchantmentUtilTests extends ConfigResetCustomAnvilTest {

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
    public void testBypassFuse(){
        // Test permission did not changed (if it do then server owner should be warned)
        String permission = CustomAnvil.bypassFusePermission;
        Assertions.assertEquals("ca.bypass.fuse", permission, "bypass fuse permission changed. " +
                "Caution with that as it will break some server CustomAnvil setup.");

        // Create ingredient item
        ItemStack normalStick = new ItemStackMock(Material.STICK);
        ItemStack sharpnessBook = AnvilFuseTestUtil.prepareItem(
            Material.ENCHANTED_BOOK,
            List.of("sharpness"), 1);

        ItemStack sharpnessStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK,
                List.of("sharpness"), 1);

        // Create result item
        ItemStack sharpnessResultStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK, 1,
                List.of("sharpness"), 1);
        ItemStack sharpness2ResultStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK, 1,
                List.of("sharpness"), 2);

        // Create failing anvil fuse data
        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                normalStick, sharpnessBook,
                null
        );
        AnvilFuseTestData nullResultData2 = new AnvilFuseTestData(
                sharpnessStick, sharpnessStick,
                null
        );

        // Create successful anvil fuse data
        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                normalStick, sharpnessBook,
                sharpnessResultStick
                // TODO add expected price
        );
        AnvilFuseTestData legalResultData2 = new AnvilFuseTestData(
                sharpnessStick, sharpnessStick,
                sharpness2ResultStick
                // TODO add expected price
        );

        // Test with no permission
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData2);

        // Add permission
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, true);

        // Test with new permission
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData2);
    }

    @Test
    public void testLeveLimitFuse(){
        String permission = CustomAnvil.bypassLevelPermission;
        Assertions.assertEquals("ca.bypass.level", permission, "level fuse permission changed. " +
                "Caution with that as it will break some server CustomAnvil setup.");

        // Create ingredient item
        ItemStack sharpness5Sword = AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD,
                List.of("sharpness"), 5);

        ItemStack sharpnessBook = AnvilFuseTestUtil.prepareItem(
                Material.ENCHANTED_BOOK,
                List.of("sharpness"), 1);
        ItemStack sharpness5Book = AnvilFuseTestUtil.prepareItem(
                Material.ENCHANTED_BOOK,
                List.of("sharpness"), 5);
        ItemStack sharpness6Book = AnvilFuseTestUtil.prepareItem(
                Material.ENCHANTED_BOOK,
                List.of("sharpness"), 6);

        // Create result item
        ItemStack sharpness2BookResult = AnvilFuseTestUtil.prepareItem(
                Material.ENCHANTED_BOOK, 1,
                List.of("sharpness"), 2);
        ItemStack sharpness6SwordResult = AnvilFuseTestUtil.prepareItem(
                Material.DIAMOND_SWORD, 1,
                List.of("sharpness"), 6);

        // Create failing anvil fuse data
        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                sharpnessBook, sharpnessBook,
                null
        );
        AnvilFuseTestData nullResultData2 = new AnvilFuseTestData(
                sharpness5Sword, sharpness6Book,
                null
        );
        AnvilFuseTestData nullResultData3 = new AnvilFuseTestData(
                sharpness5Sword, sharpness5Book,
                null
        );

        // Create successful anvil fuse data
        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                sharpnessBook, sharpnessBook,
                sharpness2BookResult
                // TODO add expected price
        );
        AnvilFuseTestData legalResultData2 = new AnvilFuseTestData(
                sharpness5Sword, sharpness6Book,
                sharpness6SwordResult
                // TODO add expected price
        );
        AnvilFuseTestData legalResultData3 = new AnvilFuseTestData(
                sharpness5Sword, sharpness5Book,
                sharpness6SwordResult
                // TODO add expected price
        );

        // Test failing result first
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData2);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData3);

        // Test working sharpness 2
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);

        // Set merge limit to 2 & test
        ConfigHolder.DEFAULT_CONFIG.getConfig().set("disable-merge-over.minecraft:sharpness", 1);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);

        // Add permission
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, true);

        // Test working sharpness 2
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData2);
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData3);
    }

}
