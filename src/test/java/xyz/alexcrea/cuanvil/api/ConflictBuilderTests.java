package xyz.alexcrea.cuanvil.api;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.group.IncludeGroup;
import xyz.alexcrea.cuanvil.tests.SharedOnlyMockBukkit;

import static org.junit.jupiter.api.Assertions.*;

public class ConflictBuilderTests extends SharedOnlyMockBukkit {

    private ConflictBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new ConflictBuilder("test");
    }

    @Test
    void testDefaults() {
        assertNull(builder.getSource());
        assertEquals("an unknown source", builder.getSourceName());
        assertEquals("test", builder.getName());

        assertTrue(builder.getEnchantmentNames().isEmpty());
        assertTrue(builder.getEnchantmentKeys().isEmpty());
        assertTrue(builder.getExcludedGroupNames().isEmpty());

        assertEquals(0, builder.getMaxBeforeConflict());
    }

    @Test
    void setName() {
        assertEquals("test", builder.getName());
        assertEquals(builder, builder.setName("another"));
        assertEquals("another", builder.getName());
    }

    @Test
    void setMaxBeforeConflict() {
        assertEquals(0, builder.getMaxBeforeConflict());
        assertEquals(builder, builder.setMaxBeforeConflict(1));
        assertEquals(1, builder.getMaxBeforeConflict());
    }

    @Test
    void enchantmentString() {
        assertTrue(builder.getEnchantmentNames().isEmpty());
        assertEquals(builder, builder.addEnchantment("bane_of_arthropods"));
        assertEquals(1, builder.getEnchantmentNames().size());

        assertEquals(builder, builder.removeEnchantment("bane_of_arthropods"));
        assertTrue(builder.getEnchantmentNames().isEmpty());
    }

    @Test
    void enchantmentKey() {
        NamespacedKey key = NamespacedKey.fromString("bane_of_arthropods");
        assertNotNull(key);

        assertTrue(builder.getEnchantmentKeys().isEmpty());
        assertEquals(builder, builder.addEnchantment(key));
        assertEquals(1, builder.getEnchantmentKeys().size());

        assertEquals(builder, builder.removeEnchantment(key));
        assertTrue(builder.getEnchantmentKeys().isEmpty());
    }

    @Test
    void excludedGroup_String() {
        assertTrue(builder.getExcludedGroupNames().isEmpty());
        assertEquals(builder, builder.addExcludedGroup("group"));
        assertEquals(1, builder.getExcludedGroupNames().size());

        assertEquals(builder, builder.removeExcludedGroup("group"));
        assertTrue(builder.getExcludedGroupNames().isEmpty());
    }

    @Test
    void excludedGroup_Group() {
        IncludeGroup group = new IncludeGroup("group");

        assertTrue(builder.getExcludedGroupNames().isEmpty());
        assertEquals(builder, builder.addExcludedGroup(group));
        assertEquals(1, builder.getExcludedGroupNames().size());

        assertEquals(builder, builder.removeExcludedGroup(group));
        assertTrue(builder.getExcludedGroupNames().isEmpty());
    }

    @Test
    void copy(){
        builder.setName("other");
        builder.setMaxBeforeConflict(1);

        builder.addEnchantment("bane_of_arthropods");
        builder.addEnchantment(NamespacedKey.fromString("bane_of_arthropods"));
        builder.addExcludedGroup("group");
        builder.addExcludedGroup(new IncludeGroup("group2"));

        ConflictBuilder copy = builder.copy();
        assertEquals("other", copy.getName());
        assertEquals(1, copy.getMaxBeforeConflict());

        assertEquals(1, copy.getEnchantmentNames().size());
        assertEquals("bane_of_arthropods", copy.getEnchantmentNames().stream().findFirst().orElse(null));

        assertEquals(1, copy.getEnchantmentKeys().size());
        assertEquals(NamespacedKey.fromString("bane_of_arthropods"), copy.getEnchantmentKeys().stream().findFirst().orElse(null));

        assertEquals(2, copy.getExcludedGroupNames().size());
    }

}
