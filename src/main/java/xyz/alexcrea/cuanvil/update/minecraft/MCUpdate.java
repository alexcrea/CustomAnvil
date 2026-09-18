package xyz.alexcrea.cuanvil.update.minecraft;

import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

@NotNullByDefault
public abstract class MCUpdate {

    public final Version version;

    public MCUpdate(Version version){
        this.version = version;
    }

    public boolean handleUpdate(UpdateHandler.UpdatedConfigList toSave, Version current, boolean hadUpdate){
        // Test if we are running in this update version or better
        if(version.greaterThan(current))
            return false;

        // if version path is not null then check if its it's before this update version
        String oldVersion;
        try (var lock = ConfigHolder.DEFAULT.read){
            oldVersion = lock.get().getConfig().getString(UpdateUtils.MINECRAFT_VERSION_PATH);
        }

        if(oldVersion != null){
            var version = Version.fromString(oldVersion);
            if(this.version.lesserEqual(version)) return false;
        }

        if(!hadUpdate){
            CustomAnvil.instance.getLogger().info("Updating config to support minecraft " + current +" ...");
        }
        doUpdate(toSave);
        return true;
    }

    protected abstract void doUpdate(UpdateHandler.UpdatedConfigList toSave);


}
