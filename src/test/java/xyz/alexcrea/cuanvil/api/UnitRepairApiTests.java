package xyz.alexcrea.cuanvil.api;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.data.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnstableApiUsage")
public class UnitRepairApiTests extends ConfigResetCustomAnvilTest {

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
    void vanillaUnitRepair() {
        ItemStack damagedPickaxe = ItemType.DIAMOND_PICKAXE.createItemStack();
        damagedPickaxe.setDurability((short) (ItemType.DIAMOND_PICKAXE.getMaxDurability() - 1));

        ItemStack resultPickaxe = ItemType.DIAMOND_PICKAXE.createItemStack();
        resultPickaxe.setDurability((short) (ItemType.DIAMOND_PICKAXE.getMaxDurability() / 2));
        ItemMeta meta = resultPickaxe.getItemMeta();
        ((Repairable) meta).setRepairCost(1);
        resultPickaxe.setItemMeta(meta);

        ItemStack diamond2 = ItemType.DIAMOND.createItemStack(2);

        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                damagedPickaxe, diamond2,
                resultPickaxe,
                2
        );

        legalResultData.executeTest(anvil, player);
    }

    @Test
    void removeUnitRepair() {
        ItemStack damagedPickaxe = ItemType.DIAMOND_PICKAXE.createItemStack();
        damagedPickaxe.setDurability((short) (ItemType.DIAMOND_PICKAXE.getMaxDurability() - 1));

        ItemStack diamond2 = ItemType.DIAMOND.createItemStack(2);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                damagedPickaxe, diamond2,
                null
        );

        // Remove unit repair
        assertTrue(UnitRepairApi.removeUnitRepair(ItemType.DIAMOND, ItemType.DIAMOND_PICKAXE));

        nullResultData.executeTest(anvil, player);

        // see override
        assertFalse(UnitRepairApi.addUnitRepair(ItemType.DIAMOND, ItemType.DIAMOND_PICKAXE, 0.25));
        assertTrue(UnitRepairApi.addUnitRepair(ItemType.DIAMOND, ItemType.DIAMOND_PICKAXE, 0.25, true));
    }


    @Test
    void addUnitRepair() {
        ItemStack damagedPickaxe = ItemType.DIAMOND_PICKAXE.createItemStack();
        damagedPickaxe.setDurability((short) (ItemType.DIAMOND_PICKAXE.getMaxDurability() - 1));

        ItemStack resultPickaxe = ItemType.DIAMOND_PICKAXE.createItemStack();
        resultPickaxe.setDurability((short) (ItemType.DIAMOND_PICKAXE.getMaxDurability() / 2));
        ItemMeta meta = resultPickaxe.getItemMeta();
        ((Repairable) meta).setRepairCost(1);
        resultPickaxe.setItemMeta(meta);

        ItemStack stick2 = ItemType.STICK.createItemStack(2);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                damagedPickaxe, stick2,
                null
        );
        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                damagedPickaxe, stick2,
                resultPickaxe,
                2
        );

        nullResultData.executeTest(anvil, player);

        // Add unit repair
        assertTrue(UnitRepairApi.addUnitRepair(ItemType.STICK, ItemType.DIAMOND_PICKAXE));
        assertFalse(UnitRepairApi.addUnitRepair(ItemType.STICK, ItemType.DIAMOND_PICKAXE));
        legalResultData.executeTest(anvil, player);
    }

}
