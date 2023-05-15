package ch.qligier.heicofawallpaper.model;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The definition of a solar dynamic wallpaper.
 * <p>
 * A solar dynamic wallpaper is a dynamic wallpaper that changes frame depending on the Sun position.
 *
 * @param numberOfFrames The number of frames in the dynamic wallpaper.
 * @param phases         The list of phases defined in the dynamic wallpaper.
 * @author Quentin Ligier
 * @see <a href="https://gist.github.com/ole/6b6b5ef20fbec12e9227075e20c6e6ef">Reverse-engineering the dynamic wallpaper
 * file format in macOS Mojave.</a>
 * @see <a href="https://github.com/mczachurski/wallpapper">This is simple console application for macOS to create
 * dynamic wallpapers introduced in macOS Mojave.</a>
 **/
public record SolarDynamicWallpaper(short numberOfFrames,
                                    List<SolarDynamicWallpaperPhase> phases) implements DynamicWallpaperInterface {

    /**
     * Constructs a new solar dynamic wallpaper.
     *
     * @param numberOfFrames The number of frames in the dynamic wallpaper.
     * @param phases         The list of phases defined in the dynamic wallpaper.
     */
    public SolarDynamicWallpaper(short numberOfFrames,
                                 final List<SolarDynamicWallpaperPhase> phases) {
        if (numberOfFrames < 1) {
            throw new RuntimeException("The solar wallpaper has 0 frame, at least one expected");
        }
        if (phases.size() == 0) {
            throw new RuntimeException("The solar wallpaper has 0 phase, at least one expected");
        }
        this.numberOfFrames = numberOfFrames;
        this.phases = phases;
        // The phases are sorted by ???, to facilitate search
        //this.phases.sort(Comparator.comparing(TimeDynamicWallpaperPhase::time));
    }

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    @Override
    public short numberOfPhases() {
        return (short) this.phases.size();
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public short currentFrame(final CurrentEnvironment currentEnvironment) {
        return (short) new Random().nextInt(0, this.numberOfFrames);
    }

    /**
     * Returns the type of the dynamic wallpaper.
     */
    @Override
    public DynamicWallpaperType type() {
        return DynamicWallpaperType.SOLAR;
    }

    /**
     * Returns the wallpaper height.
     */
    @Override
    public short height() {
        return 6016;
    }

    /**
     * Returns the wallpaper width.
     */
    @Override
    public short width() {
        return 6016;
    }

    /**
     * Returns the wallpaper file hash.
     */
    @Override
    public String hash() {
        return "977324529fbf532ca3791e406d92acbecf535a5316a2633e892aa929a4a85af0";
    }

    /**
     * Returns the wallpaper file name.
     */
    @Override
    public String filename() {
        return "Catalina.heic";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final SolarDynamicWallpaper that)) return false;
        return numberOfFrames == that.numberOfFrames
            && phases.equals(that.phases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfFrames, phases);
    }

    @Override
    public String toString() {
        return "SolarDynamicWallpaper{" +
            "numberOfFrames=" + this.numberOfFrames +
            ", phases=" + this.phases +
            '}';
    }
}
