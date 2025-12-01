package xyz.alexcrea.cuanvil.update.minecraft;

import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateUtils;
import xyz.alexcrea.cuanvil.update.Version;

public abstract class MCUpdate {

    public final Version version;

    public MCUpdate(Version version){
        this.version = version;
    }

    public boolean handleUpdate(Version current){
        // Test if we are running in this update version or better
        if(version.greaterThan(current))
            return false;

        // if version path is not null then check if its it's before this update version
        String oldVersion = ConfigHolder.DEFAULT_CONFIG.getConfig().getString(UpdateUtils.MINECRAFT_VERSION_PATH);
        if(oldVersion != null){
            var version = Version.fromString(oldVersion);
            if(this.version.lesserEqual(version)) return false;
        }

        CustomAnvil.instance.getLogger().info("Updating config to support " + version +" ...");
        doUpdate();
        return true;
    }

    protected abstract void doUpdate();


}
