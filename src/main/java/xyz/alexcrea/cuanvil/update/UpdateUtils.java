package xyz.alexcrea.cuanvil.update;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateUtils {
    public static final String MINECRAFT_VERSION_PATH = "lowMinecraftVersion";

    public static Version currentMinecraftVersion() {
        String versionString = Bukkit.getServer().getBukkitVersion().split("-")[0];
        return Version.fromString(versionString);
    }

    public static void addToStringList(FileConfiguration config, String path, String... toAdd) {
        List<String> groups = new ArrayList<>(config.getStringList(path));
        groups.addAll(Arrays.asList(toAdd));
        config.set(path, groups);

    }

    public static void addAbsentToList(FileConfiguration config, String path, String... toAdd) {
        List<String> groups = new ArrayList<>(config.getStringList(path));
        for (String val : toAdd) {
            if (groups.contains(val)) continue;

            groups.add(val);
        }
        config.set(path, groups);

    }

}
