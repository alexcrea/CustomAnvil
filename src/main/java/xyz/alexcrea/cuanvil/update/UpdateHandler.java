package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.minecraft.MCUpdate;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_21;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_21_11;
import xyz.alexcrea.cuanvil.update.minecraft.Update_1_21_9;
import xyz.alexcrea.cuanvil.update.plugin.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class UpdateHandler {

    private static final String CONFIG_VERSION_PATH = "configVersion";

    // Handle mc version update then plugin version update
    public static void handleUpdates() {
        handleMCVersionUpdate();
        handlePluginUpdate();
    }

    private static final Map<Version, Consumer<Set<ConfigHolder>>> pUpdateMap = Map.of(
            new Version(1, 6, 2), PUpdate_1_6_2::handleUpdate,
            new Version(1, 6, 7), PUpdate_1_6_7::handleUpdate,
            new Version(1, 8, 0), PUpdate_1_8_0::handleUpdate,
            new Version(1, 11, 0), PUpdate_1_11_0::handleUpdate,
            new Version(1, 15, 5), PUpdate_1_15_5::handleUpdate
    );

    private static final List<MCUpdate> mcUpdateMap = List.of(
            new Update_1_21(),
            new Update_1_21_9(),
            new Update_1_21_11()
    );

    // Handle only plugin update
    private static void handlePluginUpdate() {
        String versionString = ConfigHolder.DEFAULT_CONFIG.getConfig().getString(CONFIG_VERSION_PATH);
        Version current = versionString == null ? new Version(0) : Version.fromString(versionString);

        Set<ConfigHolder> toSave = new HashSet<>();

        AtomicReference<Version> latest = new AtomicReference<>(null);

        // Hopefully, should iterate in the "insertion" order
        pUpdateMap.forEach((ver, consumer) -> {
            if (ver.greaterThan(current)) {
                CustomAnvil.log("handling plugin update to " + ver);
                consumer.accept(toSave);

                latest.set(ver);
            }
        });

        if (latest.get() != null) {
            finishConfiguration(latest.get().toString(), toSave);
        }
    }

    // Handle minecraft version update (not plugin version update)
    public static void handleMCVersionUpdate() {
        Version current = UpdateUtils.currentMinecraftVersion();

        boolean hadUpdate = false;
        for (MCUpdate mcUpdate : mcUpdateMap) {
            hadUpdate |= mcUpdate.handleUpdate(current);
        }

        if (hadUpdate) {
            CustomAnvil.instance.getLogger().info("Updating Done !");
        }
    }

    private static void finishConfiguration(@Nonnull String newVersion, @Nonnull Set<ConfigHolder> toSave) {
        CustomAnvil.instance.getLogger().info("Configuration file updated to " + newVersion);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(CONFIG_VERSION_PATH, newVersion);

        toSave.add(ConfigHolder.DEFAULT_CONFIG);
        // save
        for (ConfigHolder configHolder : toSave) {
            configHolder.saveToDisk(true);
        }

        // then reload
        for (ConfigHolder configHolder : toSave) {
            configHolder.reload();
        }

    }

}
