package xyz.alexcrea.cuanvil.dependency.packet;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketManagerSelector {

    private PacketManagerSelector(){}

    public static @NotNull PacketManager selectPacketManager(boolean forceProtocolib){
        // Try to find version
        if(forceProtocolib){
            PacketManager protocolibPacketManager = getProtocolibIfPresent();
            if(protocolibPacketManager != null) return protocolibPacketManager;
        }

        PacketManager versionSpecificManager = getVersionSpecificManager();
        if(versionSpecificManager != null) return versionSpecificManager;

        if(!forceProtocolib){
            PacketManager protocolibPacketManager = getProtocolibIfPresent();
            if(protocolibPacketManager != null) return protocolibPacketManager;
        }
        return new NoPacketManager();
    }

    private static @Nullable PacketManager getProtocolibIfPresent(){
        if(Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) return new ProtocoLibWrapper();
        return null;
    }

    private static @Nullable PacketManager getVersionSpecificManager() {


        //TODO depending on version. find the manager !

        return null;
    }

}
