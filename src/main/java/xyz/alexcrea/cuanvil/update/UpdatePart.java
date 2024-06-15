package xyz.alexcrea.cuanvil.update;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolderEnum;

import java.util.ArrayList;
import java.util.List;

public class UpdatePart {
    private static final String DEFAULT_TYPE_PATH = "default_type";
    private static final String CONFIG_TYPE_PATH = "config_file";
    private static final String BASE_PATH = "base_path";
    private static final String RESTRICTION_BASE_PATH = "restriction_base_path";

    private final @NotNull String name;
    private final @NotNull String message;
    private final @NotNull AtomicUpdateType defaultType;
    private final @NotNull ConfigHolderEnum usedConfig;
    private final @NotNull String configRelativePath;
    private final @NotNull String restrictionRelativePath;
    private final @NotNull List<AtomicUpdate> updates;

    private UpdatePart(
            @NotNull String name,
            @NotNull String message,
            @NotNull AtomicUpdateType defaultType,
            @NotNull ConfigHolderEnum usedConfig,
            @NotNull String configRelativePath,
            @NotNull String restrictionRelativePath
            ){
        this.name = name;
        this.message = message;
        this.defaultType = defaultType;

        this.usedConfig = usedConfig;
        if(configRelativePath.isEmpty()) this.configRelativePath = "";
        else this.configRelativePath = configRelativePath+".";

        if(restrictionRelativePath.isEmpty()) this.restrictionRelativePath = "";
        else this.restrictionRelativePath = restrictionRelativePath+".";

        this.updates = new ArrayList<>();
    }

    @NotNull
    public ConfigHolderEnum getUsedConfig() {
        return this.usedConfig;
    }

    @NotNull
    public AtomicUpdateType getDefaultType() {
        return defaultType;
    }

    public String absoluteConfigPath(String path){
        return this.configRelativePath + path;
    }

    public String absoluteRestrictionPath(String path){
        return this.restrictionRelativePath + path;
    }

}
