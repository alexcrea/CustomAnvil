package xyz.alexcrea.cuanvil.update.requirement;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import java.util.List;

public class IsListRequirement implements UpdateRequirement{

    private final @NotNull String path;
    protected IsListRequirement(@NotNull String path){
        this.path = path;
    }

    @Override
    public boolean isRequirementFulfilled(@NotNull ConfigHolder holder) {
        List<?> list = holder.getConfig().getList(this.path);
        return list != null;
    }

    public static IsListRequirement createNew(@NotNull String path) {
        return new IsListRequirement(path);
    }

}
