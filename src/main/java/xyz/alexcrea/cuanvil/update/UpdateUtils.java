package xyz.alexcrea.cuanvil.update;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateUtils {
    public static final String MINECRAFT_VERSION_PATH = "lowMinecraftVersion";

    public static Version currentMinecraftVersion(){
        String versionString = Bukkit.getServer().getBukkitVersion().split("-")[0];
        return Version.fromString(versionString);
    }

    @Deprecated
    public static int[] currentMinecraftVersionArray(){
        String versionString = Bukkit.getServer().getBukkitVersion().split("-")[0];
        return UpdateUtils.readVersionFromString(versionString);
    }

    public static int[] readVersionFromString(String versionString){
        String[] partialVersion = versionString.split("\\.");
        int[] versionParts = new int[]{0, 0, 0};

        for (int i = 0; i < Math.min(3, partialVersion.length); i++) {
            versionParts[i] = Integer.parseInt(partialVersion[i]);
        }
        return versionParts;
    }

    public static void addToStringList(FileConfiguration config, String path, String... toAdd){
        List<String> groups = new ArrayList<>(config.getStringList(path));
        groups.addAll(Arrays.asList(toAdd));
        config.set(path, groups);

    }

}
