package xyz.alexcrea.cuanvil.api;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class UnitRepairApiTest extends ConfigResetCustomAnvilTest {

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
    void vanillaUnitRepair(){
        ItemStack damagedPickaxe = new ItemStackMock(Material.DIAMOND_PICKAXE);
        damagedPickaxe.setDurability((short) (Material.DIAMOND_PICKAXE.getMaxDurability() -1));

        ItemStack resultPickaxe = new ItemStackMock(Material.DIAMOND_PICKAXE);
        resultPickaxe.setDurability((short) (Material.DIAMOND_PICKAXE.getMaxDurability()/2));
        ItemMeta meta = resultPickaxe.getItemMeta();
        ((Repairable) meta).setRepairCost(1);

        ItemStack diamond2 = new ItemStackMock(Material.DIAMOND, 2);

        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                damagedPickaxe, diamond2,
                resultPickaxe,
                // TODO add expected price
                null
        );

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);
    }

    @Test
    void removeUnitRepair(){
        ItemStack damagedPickaxe = new ItemStackMock(Material.DIAMOND_PICKAXE);
        damagedPickaxe.setDurability((short) (Material.DIAMOND_PICKAXE.getMaxDurability() -1));

        ItemStack diamond2 = new ItemStackMock(Material.DIAMOND, 2);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                damagedPickaxe, diamond2,
                null
        );

        // Remove unit repair
        assertTrue(UnitRepairApi.removeUnitRepair(Material.DIAMOND, Material.DIAMOND_PICKAXE));

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);

        // see override
        assertFalse(UnitRepairApi.addUnitRepair(Material.DIAMOND, Material.DIAMOND_PICKAXE, 0.25));
        assertTrue(UnitRepairApi.addUnitRepair(Material.DIAMOND, Material.DIAMOND_PICKAXE, 0.25, true));
    }


    @Test
    void addUnitRepair(){
        ItemStack damagedPickaxe = new ItemStackMock(Material.DIAMOND_PICKAXE);
        damagedPickaxe.setDurability((short) (Material.DIAMOND_PICKAXE.getMaxDurability() -1));

        ItemStack resultPickaxe = new ItemStackMock(Material.DIAMOND_PICKAXE);
        resultPickaxe.setDurability((short) (Material.DIAMOND_PICKAXE.getMaxDurability()/2));
        ItemMeta meta = resultPickaxe.getItemMeta();
        ((Repairable) meta).setRepairCost(1);

        ItemStack stick2 = new ItemStackMock(Material.STICK, 2);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                damagedPickaxe, stick2,
                null
        );
        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                damagedPickaxe, stick2,
                resultPickaxe,
                // TODO add expected price
                null
        );

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, nullResultData);

        // Add unit repair
        assertTrue(UnitRepairApi.addUnitRepair(Material.STICK, Material.DIAMOND_PICKAXE));
        assertFalse(UnitRepairApi.addUnitRepair(Material.STICK, Material.DIAMOND_PICKAXE));
        AnvilFuseTestUtil.executeAnvilTest(anvil, player, legalResultData);
    }

}
