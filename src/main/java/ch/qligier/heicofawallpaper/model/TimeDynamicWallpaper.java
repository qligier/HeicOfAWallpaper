package ch.qligier.heicofawallpaper.model;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The definition of a time dynamic wallpaper.
 * <p>
 * A time dynamic wallpaper is a dynamic wallpaper that changes frame on given time.
 *
 * @param numberOfFrames The number of frames in the dynamic wallpaper.
 * @param phases         The list of phases defined in the dynamic wallpaper.
 * @author Quentin Ligier
 **/
public record TimeDynamicWallpaper(int numberOfFrames,
                                   List<TimeDynamicWallpaperPhase> phases) implements DynamicWallpaperInterface {

    /**
     * Constructor.
     *
     * @param numberOfFrames The number of frames in the dynamic wallpaper.
     * @param phases         The list of phases.
     */
    public TimeDynamicWallpaper(final int numberOfFrames, final List<TimeDynamicWallpaperPhase> phases) {
        if (numberOfFrames < 1) {
            throw new RuntimeException("The time wallpaper has 0 frame, at least one expected");
        }
        if (phases.size() == 0) {
            throw new RuntimeException("The time wallpaper has 0 phase, at least one expected");
        }
        this.numberOfFrames = numberOfFrames;
        this.phases = phases;
        // The phases are sorted by increasing time, to facilitate search
        this.phases.sort(Comparator.comparing(TimeDynamicWallpaperPhase::time));
    }

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    @Override
    public int numberOfPhases() {
        return this.phases.size();
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public int currentFrame(final CurrentEnvironment currentEnvironment) {
        final LocalTime currentTime = LocalTime.from(currentEnvironment.time().atZone(ZoneId.systemDefault()));

        // Try to find the earliest phase whose changing time is now or in the past
        Integer currentFrame = this.phases.stream()
            .filter(phase -> !phase.time().isAfter(currentTime))
            .findFirst()
            .map(TimeDynamicWallpaperPhase::frameIndex)
            .orElse(null);
        if (currentFrame != null) {
            return currentFrame;
        }

        // If there is no phase before the current time, return the last phase of the day
        return this.phases.get(this.phases.size() - 1).frameIndex();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final TimeDynamicWallpaper that)) return false;
        return numberOfFrames == that.numberOfFrames
            && phases.equals(that.phases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfFrames, phases);
    }

    @Override
    public String toString() {
        return "TimeDynamicWallpaper{" +
            "numberOfFrames=" + this.numberOfFrames +
            ", phases=" + this.phases +
            '}';
    }
}
