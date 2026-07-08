package xyz.alexcrea.cuanvil.update.minecraft;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.Version;

import java.util.HashSet;
import java.util.Set;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

public class Update_1_19 extends MCUpdate {

    public Update_1_19() {
        super(new Version(1, 19));
    }

    @Override
    protected void doUpdate() {
        var tosave = new HashSet<ConfigHolder>();
        updateName(tosave);

        for (ConfigHolder holder : tosave) {
            holder.saveToDisk(true);
        }
    }

    public static void updateName(@NotNull Set<ConfigHolder> tosave) {
        var conflict = ConfigHolder.CONFLICT_HOLDER.getConfig();

        addAbsentToList(conflict, "restriction_swift_sneak.enchantments", "minecraft:swift_sneak");
        addAbsentToList(conflict, "restriction_swift_sneak.notAffectedGroups", "leggings", "enchanted_book");

        ConfigHolder.ITEM_GROUP_HOLDER.reload();

        tosave.add(ConfigHolder.DEFAULT_CONFIG);
        tosave.add(ConfigHolder.CONFLICT_HOLDER);
    }

}
