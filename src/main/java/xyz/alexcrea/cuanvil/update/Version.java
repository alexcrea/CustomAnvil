package xyz.alexcrea.cuanvil.update;

import javax.annotation.Nonnull;

public record Version(int major, int minor, int patch) {

    public Version(int major, int minor){
        this(major, minor, 0);
    }
    public Version(int major){
        this(major, 0, 0);
    }

    public static Version fromString(@Nonnull String versionString){
        String[] partialVersion = versionString.split("\\.");
        int[] versionParts = new int[]{0, 0, 0};

        for (int i = 0; i < Math.min(3, partialVersion.length); i++) {
            versionParts[i] = Integer.parseInt(partialVersion[i]);
        }
        return new Version(versionParts[0], versionParts[1], versionParts[2]);
    }

    public boolean greaterThan(@Nonnull Version other){
        return this.major > other.major || (this.major == other.major &&
                (this.minor > other.minor || (this.minor == other.minor &&
                        this.patch > other.patch)));
    }

    public boolean greaterEqual(@Nonnull Version other){
        return this.major > other.major || (this.major == other.major &&
                (this.minor > other.minor || (this.minor == other.minor &&
                        this.patch >= other.patch)));
    }

    public boolean lesserThan(@Nonnull Version other){
        return this.major < other.major || (this.major == other.major &&
                (this.minor < other.minor || (this.minor == other.minor &&
                        this.patch < other.patch)));
    }

    public boolean lesserEqual(@Nonnull Version other){
        return this.major < other.major || (this.major == other.major &&
                (this.minor < other.minor || (this.minor == other.minor &&
                        this.patch <= other.patch)));
    }

}
