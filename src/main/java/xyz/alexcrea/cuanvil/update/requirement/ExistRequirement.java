package xyz.alexcrea.cuanvil.update.requirement;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

public class ExistRequirement implements UpdateRequirement{

    private final @NotNull String path;
    protected ExistRequirement(@NotNull String path){
        this.path = path;
    }

    @Override
    public boolean isRequirementFulfilled(@NotNull ConfigHolder holder) {
        return holder.getConfig().get(this.path) != null;
    }

    public static ExistRequirement createNew(@NotNull String path) {
        return new ExistRequirement(path);
    }

}
