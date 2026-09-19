package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_21_9;

@NotNullByDefault
public class PUpdate_1_15_6 extends PluginUpdate {

    public PUpdate_1_15_6() {
        super(new Version(1, 15, 6));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        // fix only needed for 1.21.9 and above
        Version current = UpdateUtils.currentMinecraftVersion();
        if(new Version(1, 21, 9).greaterThan(current)) return;

        FileConfiguration unitConfig = toSave.use(ConfigHolder.UNIT_REPAIR).getConfig();

        // Add unit repair
        Update_1_21_9.addCopperUnitRepair(unitConfig);
    }

}
