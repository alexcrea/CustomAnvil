package xyz.alexcrea.cuanvil.update.requirement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;

import java.util.List;

public class ListDontContainRequirement implements UpdateRequirement{

    private final @NotNull String path;
    private final @NotNull String[] values;
    protected ListDontContainRequirement(@NotNull String path, @NotNull String... values){
        this.path = path;
        this.values = values;
    }

    @Override
    public boolean isRequirementFulfilled(@NotNull ConfigHolder holder) {
        List<String> list = holder.getConfig().getStringList(this.path);
        for (String value : this.values) {
            if(list.contains(value)){
                return false;
            }
        }

        return true;
    }

    @Nullable
    public static ListDontContainRequirement createNew(@NotNull String path, @NotNull String... args) {
        if(args.length == 0) return null;
        return new ListDontContainRequirement(path, args);
    }

}
