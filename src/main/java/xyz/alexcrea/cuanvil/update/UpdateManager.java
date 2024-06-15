package xyz.alexcrea.cuanvil.update;

import kotlin.Pair;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class UpdateManager {
    private final static Version MAX_KNOW_MINECRAFT_VERSION = new Version(1, 21, 0);

    public final static String MINECRAFT_VERSION_PATH = "lowMinecraftVersion";
    public final static String PLUGIN_VERSION_PATH = "configVersion";
    private final static String UPDATE_FOLDER = "version";
    private final static String VERSION_LIST_RESSOUCE = UPDATE_FOLDER+"/minecraftVersions.txt";

    private List<Version> minecraftVersionList;
    private Version minecraftVersion;
    private Version usedMinecraftVersion;
    private Version lastUsedMinecraftVersion;
    private Version configVersion;

    public UpdateManager(){
    }
    public void checkUpdate(Plugin plugin){
        readVersions(plugin);

        if(minecraftVersion == null){
            plugin.getLogger().warning("Could not detect minecraft version");
            return;
        }
        if(usedMinecraftVersion == null){
            plugin.getLogger().warning("Can't detect lowest compatible config version");

        }

        if(minecraftVersion.compareTo(MAX_KNOW_MINECRAFT_VERSION) > 0){
            plugin.getLogger().warning("It look Like your minecraft version may not be supported.");
            plugin.getLogger().warning("Last Known supported version is " + MAX_KNOW_MINECRAFT_VERSION + ", and your server version was detected as " + minecraftVersion);
            plugin.getLogger().warning("The plugin may still work if there is no change since last supported version. But no guaranty.");
        }



    }

    public void readVersions(Plugin plugin){
        if(minecraftVersionList == null || minecraftVersionList.isEmpty()){
            readMinecraftVersionList(plugin);
        }
        if(minecraftVersionList == null || minecraftVersionList.isEmpty()){
            return;
        }

        // Should work //TODO test for paper and spigot
        String versionString = Bukkit.getServer().getBukkitVersion().split("-")[0];
        System.out.println(versionString + " ; " + Bukkit.getServer().getBukkitVersion()); //TESTING (for paper & spigot)
        this.minecraftVersion = Version.versionOf(versionString);
        if(this.minecraftVersion == null) return;

        this.usedMinecraftVersion = firstValidVersion(this.minecraftVersion);

        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();
        String lastUsedMinecraftVersionString = config.getString(MINECRAFT_VERSION_PATH);
        String configVersionString = config.getString(PLUGIN_VERSION_PATH);

        this.lastUsedMinecraftVersion = Version.versionOf(lastUsedMinecraftVersionString);
        this.configVersion = Version.versionOf(configVersionString);

        if(this.lastUsedMinecraftVersion == null){
            this.lastUsedMinecraftVersion = new Version(0,0,0);
        }

    }

    public Version firstValidVersion(Version toFind){
        for (Version version : this.minecraftVersionList) { // Assume sorted by readMinecraftVersionList
            if(version.compareTo(toFind) >= 0) return version;
        }
        return null;
    }

    public void readMinecraftVersionList(Plugin plugin){
        this.minecraftVersionList = readVersionList(plugin, VERSION_LIST_RESSOUCE);
        if(this.minecraftVersionList != null){
            this.minecraftVersionList.sort(Version::compareTo);
        }
    }

    public static List<Version> readVersionList(Plugin plugin, InputStream inputStream){
        ArrayList<Version> versionList = new ArrayList<>();
        Scanner scanner = new Scanner(inputStream);

        // Read every version
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.isEmpty()) continue;

            Version version = Version.versionOf(line);
            if(version == null){
                plugin.getLogger().warning("Could not parse version  \"" + line + "\"");
                continue;
            }
            versionList.add(version);
        }

        scanner.close();
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return versionList;
    }
    public static List<Pair<Version, Version>> readUpdateList(Plugin plugin, InputStream inputStream){
        ArrayList<Pair<Version, Version>> versionList = new ArrayList<>();
        Scanner scanner = new Scanner(inputStream);

        // Read every version
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.isEmpty()) continue;

            String[] versions = line.split("/", 2);
            if(versions.length <= 1){
                plugin.getLogger().warning("Could not parse update \"" + line + "\"");
                continue;
            }

            Version minecraftVersion = Version.versionOf(versions[0]);
            Version pluginVersion = Version.versionOf(versions[1]);
            if((minecraftVersion == null) || (pluginVersion == null)){
                plugin.getLogger().warning("Could not parse update \"" + line + "\"");
                continue;
            }

            versionList.add(new Pair<>(minecraftVersion, pluginVersion));
        }

        scanner.close();
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return versionList;
    }

    public static @Nullable List<Version> readVersionList(Plugin plugin, String ressouceName){
        InputStream inputStream = plugin.getResource(ressouceName);
        if(inputStream == null){
            plugin.getLogger().severe("Could not find version list from resource "+ressouceName);
            return null;
        }

        return readVersionList(plugin, inputStream);
    }

}
