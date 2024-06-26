package xyz.alexcrea.cuanvil.update;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateUtils {
    public final static String MINECRAFT_VERSION_PATH = "lowMinecraftVersion";

    static int[] readVersionFromString(String versionString){
        String[] partialVersion = versionString.split("\\.");
        int[] versionParts = new int[]{0, 0, 0};

        for (int i = 0; i < Math.min(3, partialVersion.length); i++) {
            versionParts[i] = Integer.parseInt(partialVersion[i]);
        }
        return versionParts;
    }

    static void addToStringList(FileConfiguration config, String path, String... toAdd){
        List<String> groups = new ArrayList<>(config.getStringList(path));
        groups.addAll(Arrays.asList(toAdd));
        config.set(path, groups);

    }

}
