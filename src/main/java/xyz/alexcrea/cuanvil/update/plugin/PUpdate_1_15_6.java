package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_21_9;

import javax.annotation.Nonnull;
import java.util.Set;

public class PUpdate_1_15_6 {

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        // fix only needed for 1.21.9 and above
        Version current = UpdateUtils.currentMinecraftVersion();
        if (new Version(1, 21, 9).greaterThan(current)) return;

        FileConfiguration unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        // Add unit repair
        Update_1_21_9.addCopperUnitRepair(unitConfig);

        toSave.add(ConfigHolder.UNIT_REPAIR_HOLDER);
    }

}
