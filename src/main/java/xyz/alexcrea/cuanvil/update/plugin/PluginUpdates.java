package xyz.alexcrea.cuanvil.update.plugin;

import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.Version;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class PluginUpdates {

    private static final String CONFIG_VERSION_PATH = "configVersion";

    public static void handlePluginUpdate() {
        String versionString = ConfigHolder.DEFAULT_CONFIG.getConfig().getString(CONFIG_VERSION_PATH);
        Version current = versionString == null ? new Version(0) : Version.fromString(versionString);

        Set<ConfigHolder> toSave = new HashSet<>();

        if (new Version(1, 6, 2).greaterThan(current)) {
            PUpdate_1_6_2.handleUpdate(toSave);
            // We assume 1.6.7 will run. TODO a better system instead of that I guess
        }
        if (new Version(1, 6, 7).greaterThan(current)) {
            PUpdate_1_6_7.handleUpdate(toSave);
            // We assume 1.8.0 will run.
        }
        if (new Version(1, 8, 0).greaterThan(current)) {
            PUpdate_1_8_0.handleUpdate(toSave);
            // We assume 1.11.0 will run.
        }

        if (new Version(1, 11, 0).greaterThan(current)) {
            PUpdate_1_11_0.handleUpdate(toSave);

            finishConfiguration("1.11.0", toSave);
        }

    }

    private static void finishConfiguration(@Nonnull String newVersion, @Nonnull Set<ConfigHolder> toSave) {
        CustomAnvil.instance.getLogger().info("Configuration file updated to " + newVersion);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(CONFIG_VERSION_PATH, newVersion);

        toSave.add(ConfigHolder.DEFAULT_CONFIG);
        for (ConfigHolder configHolder : toSave) {
            configHolder.saveToDisk(true);
        }
    }

}
