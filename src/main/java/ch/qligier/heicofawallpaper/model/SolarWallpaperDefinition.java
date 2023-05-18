package ch.qligier.heicofawallpaper.model;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The definition of a solar dynamic wallpaper.
 * <p>
 * A solar dynamic wallpaper is a dynamic wallpaper that changes frame depending on the Sun position.
 *
 * @author Quentin Ligier
 * @see <a href="https://gist.github.com/ole/6b6b5ef20fbec12e9227075e20c6e6ef">Reverse-engineering the dynamic wallpaper
 * file format in macOS Mojave.</a>
 * @see <a href="https://github.com/mczachurski/wallpapper">This is simple console application for macOS to create
 * dynamic wallpapers introduced in macOS Mojave.</a>
 **/
public class SolarWallpaperDefinition extends DynamicWallpaperDefinition {

    /**
     * The list of phases defined in the dynamic wallpaper.
     */
    private final List<PhaseSolar> phases;

    /**
     * Constructs a new solar dynamic wallpaper.
     *
     * @param phases The list of phases defined in the dynamic wallpaper.
     */
    public SolarWallpaperDefinition(final List<PhaseSolar> phases) {
        if (phases.isEmpty()) {
            throw new RuntimeException("The solar wallpaper has 0 phase, at least one expected");
        }
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

    public List<PhaseSolar> getPhases() {
        return this.phases;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final SolarWallpaperDefinition that)) return false;
        return phases.equals(that.phases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phases);
    }

    @Override
    public String toString() {
        return "SolarWallpaperDefinition{" +
            "phases=" + phases +
            ", height=" + height +
            ", width=" + width +
            ", hash='" + fileHash + '\'' +
            ", filename='" + filename + '\'' +
            ", numberOfFrames=" + numberOfFrames +
            '}';
    }
}
