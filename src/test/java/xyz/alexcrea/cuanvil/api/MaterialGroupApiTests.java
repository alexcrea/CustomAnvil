package xyz.alexcrea.cuanvil.api;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialGroupApiTests extends ConfigResetCustomAnvilTest {

    @Test
    void groupAddAndRemove() {
        String groupName = "group";
        IncludeGroup group = new IncludeGroup(groupName);
        group.addToPolicy(Material.DIAMOND_PICKAXE); // We do not want it to be empty

        // Group not being set should not exist
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Add group
        assertTrue(MaterialGroupApi.addMaterialGroup(group));
        assertFalse(MaterialGroupApi.addMaterialGroup(group, true));

        assertTrue(doGroupExist(groupName));
        assertTrue(doGroupCanBeFound(groupName));

        // Remove group
        assertTrue(MaterialGroupApi.removeGroup(group));
        assertFalse(MaterialGroupApi.removeGroup(group));

        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Re add
        assertFalse(MaterialGroupApi.addMaterialGroup(group, false));
        assertTrue(MaterialGroupApi.addMaterialGroup(group, true));

        assertTrue(doGroupExist(groupName));
        assertTrue(doGroupCanBeFound(groupName));

    }

    @Test
    void writeGroup_Reload() {
        String groupName = "group";
        IncludeGroup group = new IncludeGroup(groupName);
        group.addToPolicy(Material.DIAMOND_PICKAXE); // We do not want it to be empty

        // Group not being set should not exist
        assertFalse(doGroupExist(groupName));
        assertFalse(doGroupCanBeFound(groupName));

        // Add group and reload
        assertTrue(MaterialGroupApi.writeMaterialGroup(group));
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
        IncludeGroup group = new IncludeGroup(groupName);

        // Add group and reload
        assertFalse(MaterialGroupApi.writeMaterialGroup(group));
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
        IncludeGroup group = new IncludeGroup(groupName);

        // Try write group
        assertFalse(MaterialGroupApi.writeMaterialGroup(group));
    }

    boolean doGroupExist(String groupName) {
        return MaterialGroupApi.getGroup(groupName) != null;
    }

    boolean doGroupCanBeFound(String groupName) {
        ConflictBuilder builder = new ConflictBuilder(groupName);
        builder.addExcludedGroup(groupName);

        EnchantConflictGroup group = builder.build();
        IncludeGroup materialGroup = (IncludeGroup) group.getCantConflictGroup();
        return materialGroup.getGroups().size() == 1;
    }

}
