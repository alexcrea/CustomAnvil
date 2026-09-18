package xyz.alexcrea.cuanvil.update;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

@NotNullByDefault
public record Version(int major, int minor, int patch) {

    public Version(int major, int minor){
        this(major, minor, 0);
    }
    public Version(int major){
        this(major, 0, 0);
    }

    public static Version fromString(@Nullable String versionString){
        if(versionString == null) return new Version(0, 0, 0);

        String[] partialVersion = versionString.split("\\.");
        int[] versionParts = new int[]{0, 0, 0};

        for (int i = 0; i < Math.min(3, partialVersion.length); i++) {
            try {
                versionParts[i] = Integer.parseInt(partialVersion[i]);
            } catch (NumberFormatException e) {
                break;
            }
        }
        return new Version(versionParts[0], versionParts[1], versionParts[2]);
    }

    public boolean greaterThan(Version other){
        return this.major > other.major || (this.major == other.major &&
                (this.minor > other.minor || (this.minor == other.minor &&
                        this.patch > other.patch)));
    }

    public boolean greaterEqual(Version other){
        return this.major > other.major || (this.major == other.major &&
                (this.minor > other.minor || (this.minor == other.minor &&
                        this.patch >= other.patch)));
    }

    public boolean lesserThan(Version other){
        return this.major < other.major || (this.major == other.major &&
                (this.minor < other.minor || (this.minor == other.minor &&
                        this.patch < other.patch)));
    }

    public boolean lesserEqual(Version other){
        return this.major < other.major || (this.major == other.major &&
                (this.minor < other.minor || (this.minor == other.minor &&
                        this.patch <= other.patch)));
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
