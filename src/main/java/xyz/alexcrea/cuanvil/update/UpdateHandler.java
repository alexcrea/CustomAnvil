package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.minecraft.*;
import xyz.alexcrea.cuanvil.update.plugin.*;
import xyz.alexcrea.cuanvil.util.LockedObjectProvider;
import xyz.alexcrea.cuanvil.util.LockedObjectProvider.LockedWrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NotNullByDefault
public class UpdateHandler {

    private static final String CONFIG_VERSION_PATH = "configVersion";

    // Handle mc version update then plugin version update
    public static void handleUpdates() {
        var toSave = new UpdatedConfigList();
        boolean updatedMC = handleMCVersionUpdate(toSave);
        Version latest = handlePluginUpdate(toSave);

        // Finally, re add default we may be missing
        updatedMC |= PluginSetDefault.reAddMissingDefault(toSave);

        if(updatedMC || latest != null)
            toSave.cleanup(latest);
    }

    private static final List<PluginUpdate> pluginUpdateList = List.of(
            new PUpdate_1_6_2(),
            new PUpdate_1_6_7(),
            new PUpdate_1_8_0(),
            new PUpdate_1_11_0(),
            new PUpdate_1_15_5(),
            new PUpdate_1_15_6(),
            new PUpdate_1_18()
    );

    private static final List<MCUpdate> mcUpdateList = List.of(
            new Update_1_19(),
            new Update_1_20_5(),
            new Update_1_21(),
            new Update_1_21_9(),
            new Update_1_21_11()
    );

    // Handle only plugin update
    @Nullable
    private static Version handlePluginUpdate(UpdatedConfigList toSave) {
        String versionString;
        try(var lock = ConfigHolder.DEFAULT.read) {
            versionString = lock.get().getConfig().getString(CONFIG_VERSION_PATH);
        }

        Version current = versionString == null ? new Version(0) : Version.fromString(versionString);

        @Nullable Version latest = null;

        // Hopefully, should iterate in the "insertion" order
        for(PluginUpdate update : pluginUpdateList) {
            var version = update.version;
            if(version.greaterThan(current)) {
                CustomAnvil.log("handling plugin update to " + version);
                update.handleUpdate(toSave);

                latest = version;
            }
        }

        return latest;
    }

    // Handle minecraft version update (not plugin version update)
    private static boolean handleMCVersionUpdate(UpdatedConfigList toSave) {
        Version current = UpdateUtils.currentMinecraftVersion();

        boolean hadUpdate = false;
        for(MCUpdate mcUpdate : mcUpdateList) {
            hadUpdate |= mcUpdate.handleUpdate(toSave, current, hadUpdate);
        }

        if(hadUpdate) {
            CustomAnvil.instance.getLogger().info("Minecraft updating Done !");
        }
        return hadUpdate;
    }

    public static class UpdatedConfigList {
        private final List<LockedWrite<? extends ConfigHolder>> used = new ArrayList<>();
        private final Set<ConfigHolder> toSave = new HashSet<>();

        public <T extends ConfigHolder> T use(LockedObjectProvider<T> config) {
            var lock = config.write;
            used.add(lock);
            T holder = lock.get();
            toSave.add(holder);

            return holder;
        }

        public void cleanup(@Nullable Version newVersion) {
            CustomAnvil.instance.getLogger().info("Configuration file updated !");

            if(newVersion != null) {
                var def = use(ConfigHolder.DEFAULT);
                def.getConfig().set(CONFIG_VERSION_PATH, newVersion.toString());
            }

            // save
            for(ConfigHolder configHolder : toSave) {
                configHolder.saveToDisk(true);
            }

            // then reload
            for(ConfigHolder configHolder : toSave) {
                configHolder.reload();
            }

            // then unlock
            for(LockedWrite<? extends ConfigHolder> lock : used) {
                lock.close();
            }
        }
    }

}
