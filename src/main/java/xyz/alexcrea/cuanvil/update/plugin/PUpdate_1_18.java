package xyz.alexcrea.cuanvil.update.plugin;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_19;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_20_5;

@NotNullByDefault
public class PUpdate_1_18 extends PluginUpdate {

    public PUpdate_1_18() {
        super(new Version(1, 18, 0));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        Version current = UpdateUtils.currentMinecraftVersion();
        if(new Version(1, 19, 0).greaterThan(current)) return;
        Update_1_19.updateName(toSave);

        if(new Version(1, 20, 5).greaterThan(current)) return;
        Update_1_20_5.updateName(toSave);
    }
}
