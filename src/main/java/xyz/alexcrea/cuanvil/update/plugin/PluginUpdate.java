package xyz.alexcrea.cuanvil.update.plugin;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

@NotNullByDefault
public abstract class PluginUpdate {

    public final Version version;

    public PluginUpdate(Version version) {
        this.version = version;
    }

    public abstract void handleUpdate(UpdateHandler.UpdatedConfigList toSave);
}
