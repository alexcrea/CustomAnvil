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
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.tests.DefaultCustomAnvilTest;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;

import java.util.List;

public class EnchantmentUtilTests extends DefaultCustomAnvilTest {

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

        // Create item
        ItemStack normalStick = new ItemStackMock(Material.STICK);
        ItemStack sharpnessBook = AnvilFuseTestUtil.prepareItem(
            Material.ENCHANTED_BOOK,
            List.of("sharpness"), 1);

        ItemStack sharpnessStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK,
                List.of("sharpness"), 1);

        ItemStack sharpnessResultStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK, 1,
                List.of("sharpness"), 1);
        ItemStack sharpness2ResultStick = AnvilFuseTestUtil.prepareItem(
                Material.STICK, 1,
                List.of("sharpness"), 2);

        // Create anvil fuse data
        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                normalStick, sharpnessBook,
                null
        );
        AnvilFuseTestData nullResultData2 = new AnvilFuseTestData(
                sharpnessStick, sharpnessStick,
                null
        );

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


}
