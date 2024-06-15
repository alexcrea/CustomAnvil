package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.ConfigHolderEnum;
import xyz.alexcrea.cuanvil.update.requirement.UpdateRequirement;

import java.util.ArrayList;
import java.util.List;

public class AtomicUpdate{
    private static final String UPDATE_TYPE = "type";
    private static final String CONFIG_TYPE_PATH = "config_type";
    private static final String PATH_PATH = "path";
    private static final String REQUIREMENT_PATH = "requirement";
    private static final String VALUE_PATH = "value";

    private final @NotNull AtomicUpdateType type;
    private final @NotNull ConfigHolderEnum configType;
    private final @NotNull  String path;
    private final @NotNull List<UpdateRequirement> requirements;
    private final @Nullable String value; // Ignored on unset. not null if not unset

    public AtomicUpdate(
            @NotNull AtomicUpdateType type,
            @NotNull ConfigHolderEnum configType,
            @NotNull  String path,
            @NotNull List<UpdateRequirement> requirements,
            @Nullable String value) {
        this.type = type;
        this.configType = configType;
        this.path = path;
        this.requirements = requirements;
        this.value = value;
    }

    public static @Nullable AtomicUpdate fromConfig(@NotNull ConfigurationSection section){
        String typeString = section.getString(UPDATE_TYPE);
        String configTypeString = section.getString(CONFIG_TYPE_PATH);
        String path = section.getString(PATH_PATH);
        List<String> expected = section.getStringList(REQUIREMENT_PATH);
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

        List<UpdateRequirement> requirements = new ArrayList<>();
        for (String expectedPart : expected) {
            UpdateRequirement requirement = UpdateRequirement.fromString(expectedPart);
            if(requirement == null){
                if (!ConfigOptions.INSTANCE.getDebugLog()) {
                    CustomAnvil.instance.getLogger().warning("This message should not be displayed on Production. " +
                            "If it do display please check for update or create a new issue.");
                    CustomAnvil.instance.getLogger().warning("Here are the information of what happened: Atomic Update failed to parse requirement");
                }
                CustomAnvil.instance.getLogger().warning("Unsuccessfully parsed requirement: " + expectedPart);

            }else{
                requirements.add(requirement);
            }
        }

        return new AtomicUpdate(type, configType, path, requirements, value);
    }

    public boolean isRequirementValidated(){
        for (UpdateRequirement requirement : requirements) {
            if(!requirement.isRequirementFulfilled(this.configType.getConfigHolder())){
                return false;
            }
        }
        return true;
    }

    // Is string equal can take null, it also checks if the content is equal but ignore case
    private boolean isStringEqual(@Nullable String val1, @Nullable String val2){
        if(val1 == null){
            return val2 == null;
        }
        return val1.equalsIgnoreCase(val2);
    }

    public boolean isExpected(@Deprecated boolean ignoreIfOperationIsDone){
        if(!isRequirementValidated()){
            return false;
        }

        String value;
        List<String> values;
        switch (this.type){
            case SET:
                value = this.configType.getConfigHolder().getConfig().getString(this.path);
                return ignoreIfOperationIsDone && isStringEqual(value, this.value);
            case UNSET:
                value = this.configType.getConfigHolder().getConfig().getString(this.path);
                return ignoreIfOperationIsDone && (value == null);
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
