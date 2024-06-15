package xyz.alexcrea.cuanvil.update.requirement;

import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdatePart;

public interface UpdateRequirement {

    boolean isRequirementFulfilled(@NotNull ConfigHolder holder);

    static UpdateRequirement fromString(String value, UpdatePart parent){
        // Format: path requirementType [arguments]
        String[] args = value.split(" ");
        if(args.length < 2) return null;
        String path = parent.absoluteRestrictionPath(args[0]);
        String type = args[1];

        String[] newArgs = new String[args.length-2];
        System.arraycopy(args, 2, newArgs, 0, args.length - 2);

        switch (type.toLowerCase()){
            case "exist":
                return ExistRequirement.createNew(path);
            case "is_list":
                return IsListRequirement.createNew(path);
            case "list_contain":
                return ListContainRequirement.createNew(path, newArgs);
            case "list_dont_contain":
                return ListDontContainRequirement.createNew(path, newArgs);
            case "null":
                return NullRequirement.createNew(path);
            case "equal":
                return StringEqualRequirement.createNew(path, newArgs, true);
            case "equal_case":
                return StringEqualRequirement.createNew(path, newArgs, false);

            default:
                CustomAnvil.Companion.log("Can't find requirement type \"" + type + "\"");
                return null;
        }

    }
}
