package xyz.alexcrea.cuanvil.update.requirement;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

public class NullRequirement implements UpdateRequirement{

    private final @NotNull String path;
    protected NullRequirement(@NotNull String path){
        this.path = path;
    }

    @Override
    public boolean isRequirementFulfilled(@NotNull ConfigHolder holder) {
        return holder.getConfig().get(this.path) == null;
    }

    public static NullRequirement createNew(@NotNull String path) {
        return new NullRequirement(path);
    }

}
