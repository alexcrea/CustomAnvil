package xyz.alexcrea.cuanvil.update.requirement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

public class StringEqualRequirement implements UpdateRequirement {

    private final @NotNull String path;
    private final @NotNull String equalTo;
    private final boolean ignoreCase;
    protected StringEqualRequirement(
            @NotNull String path,
            @NotNull String equalTo,
            boolean ignoreCase){
        this.path = path;
        this.equalTo = equalTo;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean isRequirementFulfilled(@NotNull ConfigHolder holder) {
        String value = holder.getConfig().getString(this.path);
        if(this.ignoreCase){
            return equalTo.equalsIgnoreCase(value);
        }else{
            return equalTo.equals(value);
        }
    }


    @Nullable
    public static StringEqualRequirement createNew(
            @NotNull String path,
            @NotNull String[] args,
            boolean ignoreCase) {
        if(args.length == 0) return null;

        StringBuilder value = new StringBuilder();
        value.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            value.append(" ").append(args[i]);
        }

        return new StringEqualRequirement(path, value.toString(), ignoreCase);
    }


}
