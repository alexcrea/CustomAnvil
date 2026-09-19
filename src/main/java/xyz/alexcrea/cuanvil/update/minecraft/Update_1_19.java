package xyz.alexcrea.cuanvil.update.minecraft;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@NotNullByDefault
public class Update_1_19 extends MCUpdate {

    public Update_1_19() {
        super(new Version(1, 19));
    }

    @Override
    protected void doUpdate(UpdateHandler.UpdatedConfigList toSave) {
        updateName(toSave);
    }

    public static void updateName(UpdateHandler.UpdatedConfigList toSave) {
        var conflict = toSave.use(ConfigHolder.CONFLICT).getConfig();

        addAbsentToList(conflict, "restriction_swift_sneak.enchantments", "minecraft:swift_sneak");
        addAbsentToList(conflict, "restriction_swift_sneak.notAffectedGroups", "leggings", "enchanted_book");
    }

}
