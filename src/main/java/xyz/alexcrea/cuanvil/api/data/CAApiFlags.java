package xyz.alexcrea.cuanvil.api.data;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum CAApiFlags {

    ENCHANTMENT_REGISTER_V1,
    ENCHANTMENT_CONFLICT_V1,
    CUSTOM_RECIPE_V1,
    UNIT_REPAIR_V1,

    MATERIAL_GROUP_V1,

    CONFIG_EVENTS_V1,
    LISTENER_EVENTS_V1,
    ;

    private static final Set<CAApiFlags> CURRENT_FLAGS = EnumSet.of(
            ENCHANTMENT_REGISTER_V1,
            ENCHANTMENT_CONFLICT_V1,
            CUSTOM_RECIPE_V1,
            UNIT_REPAIR_V1,

            MATERIAL_GROUP_V1,

            CONFIG_EVENTS_V1,
            LISTENER_EVENTS_V1
    );

    public static Set<CAApiFlags> getCurrentFlags() {
        return Collections.unmodifiableSet(CURRENT_FLAGS);
    }

    public boolean hasFlags(CAApiFlags... flag) {
        return CURRENT_FLAGS.containsAll(List.of(flag));
    }


}
