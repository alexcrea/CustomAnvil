package xyz.alexcrea.cuanvil.update;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Version implements Comparable<Version>{
    int major;
    int minor;
    int build;

    public Version(int major, int minor, int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public static @Nullable Version versionOf(@Nullable String version){
        if(version == null) return null;
        String[] numbers = version.split("\\.", 3);

        Integer major;
        Integer minor;
        Integer patch;

        major = Ints.tryParse(numbers[0]);
        // Minor number
        if(numbers.length >= 2){
            minor = Ints.tryParse(numbers[1]);
        }else{
            minor = 0;
        }
        // Patch number
        if(numbers.length >= 3){
            patch = Ints.tryParse(numbers[2]);
        }else{
            patch = 0;
        }

        if((major == null) || (minor == null) || (patch == null)){
            return null;
        }

        return new Version(major, minor, patch);
    }


    @Override
    public int compareTo(@NotNull Version other) {
        if(this.major > other.major) return 1;
        else if (this.major < other.major) return -1;

        if(this.minor > other.minor) return 1;
        else if (this.minor < other.minor) return -1;

        if(this.build > other.build) return 1;
        else if (this.build < other.build) return -1;

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Version){
            Version other = (Version) obj;
            return (other.major == this.major) && (other.minor == this.minor) && (other.build == this.build);
        }
        return false;
    }

    @Override
    public String toString() {
        if(build > 0){
            return major + "." + minor + "." + build;
        }
        if(minor > 0){
            return major + "." + minor;
        }
        return String.valueOf(major);
    }
}
