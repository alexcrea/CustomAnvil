package xyz.alexcrea.cuanvil.api;

import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeItemTypeGroup;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnstableApiUsage")
public class MaterialGroupApiTests extends ConfigResetCustomAnvilTest {

    @Test
    void groupAddAndRemove() {
        String groupName = "group";
        IncludeItemTypeGroup group = new IncludeItemTypeGroup(groupName);
        group.addToPolicy(ItemType.DIAMOND_PICKAXE); // We do not want it to be empty

        // Group not being set should not exist
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Add group
        assertTrue(ItemGroupApi.addItemGroup(group));
        assertFalse(ItemGroupApi.addItemGroup(group, true));

        assertTrue(doGroupExist(groupName));
        assertTrue(doGroupCanBeFound(groupName));

        // Remove group
        assertTrue(ItemGroupApi.removeGroup(group));
        assertFalse(ItemGroupApi.removeGroup(group));

        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Re add
        assertFalse(ItemGroupApi.addItemGroup(group, false));
        assertTrue(ItemGroupApi.addItemGroup(group, true));

        assertTrue(doGroupExist(groupName));
        assertTrue(doGroupCanBeFound(groupName));

    }

    @Test
    void writeGroup_Reload() {
        String groupName = "group";
        IncludeItemTypeGroup group = new IncludeItemTypeGroup(groupName);
        group.addToPolicy(ItemType.DIAMOND_PICKAXE); // We do not want it to be empty

        // Group not being set should not exist
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Add group and reload
        assertTrue(ItemGroupApi.writeItemGroup(group));
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Tick so write get reloaded
        server.getScheduler().performOneTick();

        assertTrue(doGroupExist(groupName));
        assertTrue(doGroupCanBeFound(groupName));
    }

    @Test
    void writeGroup_Empty() {
        String groupName = "group";
        IncludeItemTypeGroup group = new IncludeItemTypeGroup(groupName);

        // Add group and reload
        assertFalse(ItemGroupApi.writeItemGroup(group));
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Tick so write get reloaded
        server.getScheduler().performOneTick();

        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));
    }

    @Test
    void writeGroup_InvalidDot() {
        String groupName = "group.group";
        IncludeItemTypeGroup group = new IncludeItemTypeGroup(groupName);

        // Try write group
        assertFalse(ItemGroupApi.writeItemGroup(group));
    }

    boolean doGroupExist(String groupName) {
        return ItemGroupApi.getGroup(groupName) != null;
    }

    boolean doGroupCanBeFound(String groupName) {
        ConflictBuilder builder = new ConflictBuilder(groupName);
        builder.addExcludedGroup(groupName);

        EnchantConflictGroup group = builder.build();
        IncludeItemTypeGroup materialGroup = (IncludeItemTypeGroup) group.getCantConflictGroup();
        return materialGroup.getGroups().size() == 1;
    }

}
