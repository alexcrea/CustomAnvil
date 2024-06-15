package xyz.alexcrea.cuanvil.update;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.ConfigHolderEnum;

import java.util.List;

public class AtomicUpdate{
    private static final String UPDATE_TYPE = "type";
    private static final String CONFIG_TYPE_PATH = "config_type";
    private static final String PATH_PATH = "path";
    private static final String EXPECTED_PATH = "expected";
    private static final String VALUE_PATH = "value";

    private final @NotNull AtomicUpdateType type;
    private final @NotNull ConfigHolderEnum configType;
    private final @NotNull  String path;
    private final @Nullable String expected; // Ignored on list
    private final @Nullable String value; // Ignored on unset. not null if not unset

    public AtomicUpdate(
            @NotNull AtomicUpdateType type,
            @NotNull ConfigHolderEnum configType,
            @NotNull  String path,
            @Nullable String expected, // Ignored on list
            @Nullable String value) {
        this.type = type;
        this.configType = configType;
        this.path = path;
        this.expected = expected;
        this.value = value;
    }

    public static @Nullable AtomicUpdate fromConfig(@NotNull ConfigurationSection section){
        String typeString = section.getString(UPDATE_TYPE);
        String configTypeString = section.getString(CONFIG_TYPE_PATH);
        String path = section.getString(PATH_PATH);
        String expected = section.getString(EXPECTED_PATH);
        String value = section.getString(VALUE_PATH);

        if((configTypeString == null) ||
                (typeString == null) ||
                (path == null)){
            return null;
        }

        AtomicUpdateType type;
        try {
            type = AtomicUpdateType.valueOf(typeString.toUpperCase());
        } catch (Exception e) { return null; }

        if(type != AtomicUpdateType.UNSET && value == null){
            return null;
        }

        ConfigHolderEnum configType;
        try {
            configType = ConfigHolderEnum.valueOf(configTypeString.toUpperCase());
        } catch (Exception e) { return null; }


        return new AtomicUpdate(type, configType, path, expected, value);
    }

    // Is string equal can take null, it also checks if the content is equal but ignore case
    private boolean isStringEqual(@Nullable String val1, @Nullable String val2){
        if(val1 == null){
            return val2 == null;
        }
        return val1.equalsIgnoreCase(val2);
    }

    public boolean isExpected(boolean ignoreIfOperationIsDone){
        String value;
        List<String> values;
        switch (this.type){
            case SET:
                value = this.configType.getConfigHolder().getConfig().getString(this.path);
                return (isStringEqual(value, this.expected)) || (ignoreIfOperationIsDone && isStringEqual(value, this.value));
            case UNSET:
                value = this.configType.getConfigHolder().getConfig().getString(this.path);
                return (isStringEqual(value, this.expected)) || (ignoreIfOperationIsDone && (value == null));
            case LIST_ADD:
                values = this.configType.getConfigHolder().getConfig().getStringList(this.path);
                return ignoreIfOperationIsDone || !values.contains(this.value);

            case LIST_REMOVE:
                values = this.configType.getConfigHolder().getConfig().getStringList(this.path);
                return ignoreIfOperationIsDone || values.contains(this.value);
        }

        return false;
    }

    public boolean tryApplyUpdate(boolean forceUnexpected, boolean ignoreIfResultEqual){
        if(!forceUnexpected && !isExpected(ignoreIfResultEqual)){
            return false;
        }

        ConfigHolder configHolder = this.configType.getConfigHolder();
        FileConfiguration config = configHolder.getConfig();

        List<String> values;
        switch (this.type){
            case SET:
                config.set(this.path, this.value);
                break;
            case UNSET:
                config.set(this.path, null);
                break;
            case LIST_ADD:
                values = config.getStringList(this.path);
                values.add(this.value);
                config.set(this.path, this.value);
                break;
            case LIST_REMOVE:
                values = config.getStringList(this.path);
                values.remove(this.value);
                config.set(this.path, this.value);
            break;
        }

        return true;
    }

}
