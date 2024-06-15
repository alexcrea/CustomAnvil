package xyz.alexcrea.cuanvil.update;

import io.delilaheve.CustomAnvil;
import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.requirement.UpdateRequirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AtomicUpdate{
    private static final String UPDATE_TYPE = "type";
    private static final String PATH_PATH = "path";
    private static final String REQUIREMENT_PATH = "requirement";
    private static final String VALUE_PATH = "value";
    private static final String MULTIPLES_VALUES_SEPARATOR = ",";

    private final @NotNull UpdatePart parent;
    private final @NotNull AtomicUpdateType type;
    private final @NotNull  String path;
    private final @NotNull List<UpdateRequirement> requirements;
    private final @Nullable String value; // Ignored on unset. not null if not unset

    public AtomicUpdate(
            @NotNull UpdatePart parent,
            @NotNull AtomicUpdateType type,
            @NotNull String path,
            @NotNull List<UpdateRequirement> requirements,
            @Nullable String value) {
        this.parent = parent;
        this.type = type;
        this.path = path;
        this.requirements = requirements;
        this.value = value;
    }

    public static @Nullable AtomicUpdate fromConfig(
            @NotNull UpdatePart parent,
            @NotNull ConfigurationSection section){
        String typeString = section.getString(UPDATE_TYPE);
        String path = parent.absoluteRestrictionPath(section.getString(PATH_PATH));
        List<String> expected = section.getStringList(REQUIREMENT_PATH);
        String value = section.getString(VALUE_PATH);

        if((path == null)){
            return null;
        }

        AtomicUpdateType type;
        if(typeString == null){
            type = parent.getDefaultType();
        }else{
            try {
                type = AtomicUpdateType.valueOf(typeString.toUpperCase());
            } catch (Exception e) { return null; }
        }

        if(type != AtomicUpdateType.UNSET && value == null){
            return null;
        }

        List<UpdateRequirement> requirements = new ArrayList<>();
        for (String expectedPart : expected) {
            UpdateRequirement requirement = UpdateRequirement.fromString(expectedPart, parent);
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

        return new AtomicUpdate(parent, type, path, requirements, value);
    }

    public boolean isRequirementValidated(){
        for (UpdateRequirement requirement : requirements) {
            if(!requirement.isRequirementFulfilled(getConfigHolder())){
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
        if(!isRequirementValidated()) return false;
        if(ignoreIfOperationIsDone) return true;

        String value;
        List<String> list;
        String[] values;
        switch (this.type){
            case SET:
                value = getConfigHolder().getConfig().getString(this.path);
                return !isStringEqual(value, this.value);
            case UNSET:
                return getConfigHolder().getConfig().contains(this.path);

            case LIST_ADD:
                list = getConfigHolder().getConfig().getStringList(this.path);
                return !list.contains(this.value);
            case LIST_ADD_MULTIPLES:
                list = getConfigHolder().getConfig().getStringList(this.path);
                assert this.value != null;
                values = this.value.split(MULTIPLES_VALUES_SEPARATOR);
                for (String value2 : values) {
                    if(list.contains(value2)) return false;
                }
                return true;

            case LIST_REMOVE:
                list = getConfigHolder().getConfig().getStringList(this.path);
                return list.contains(this.value);
            case LIST_REMOVE_MULTIPLES:
                list = getConfigHolder().getConfig().getStringList(this.path);
                assert this.value != null;
                values = this.value.split(MULTIPLES_VALUES_SEPARATOR);
                for (String value2 : values) {
                    if(!list.contains(value2)) return false;
                }
                return true;

        }

        return false;
    }

    public boolean tryApplyUpdate(boolean forceUnexpected, boolean ignoreIfResultEqual){
        if(!forceUnexpected && !isExpected(ignoreIfResultEqual)){
            return false;
        }

        FileConfiguration config = getConfigHolder().getConfig();

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
            case LIST_ADD_MULTIPLES:
                values = config.getStringList(this.path);
                assert this.value != null;
                values.addAll(Arrays.asList(this.value.split(MULTIPLES_VALUES_SEPARATOR)));
                config.set(this.path, this.value);
                break;

            case LIST_REMOVE:
                values = config.getStringList(this.path);
                values.remove(this.value);
                config.set(this.path, this.value);
                break;
            case LIST_REMOVE_MULTIPLES:
                values = config.getStringList(this.path);
                assert this.value != null;
                values.removeAll(Arrays.asList(this.value.split(MULTIPLES_VALUES_SEPARATOR)));
                config.set(this.path, this.value);
                break;
        }

        return true;
    }

    public ConfigHolder getConfigHolder(){
        return this.parent.getUsedConfig().getConfigHolder();
    }

}
