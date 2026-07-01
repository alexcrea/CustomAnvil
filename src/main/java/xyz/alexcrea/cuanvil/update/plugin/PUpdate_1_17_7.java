package xyz.alexcrea.cuanvil.update.plugin;

import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_20_5;

import javax.annotation.Nonnull;
import java.util.Set;

public class PUpdate_1_17_7 {

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        // fix only needed for 1.20.5 and above
        Version current = UpdateUtils.currentMinecraftVersion();
        if (new Version(1, 20, 5).greaterThan(current)) return;

        Update_1_20_5.updateName(toSave);
    }

}
