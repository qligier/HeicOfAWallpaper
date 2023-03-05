package ch.qligier.heicofawallpaper.model;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

/**
 * The definition of a time dynamic wallpaper.
 * <p>
 * A time dynamic wallpaper is a dynamic wallpaper that changes frame on given time.
 *
 * @author Quentin Ligier
 **/
public class TimeWallpaper implements DynamicWallpaperInterface {

    /**
     * The number of frames in the dynamic wallpaper.
     */
    private final int numberOfFrames;

    /**
     * The list of phases defined in the dynamic wallpaper, ordered by time.
     */
    private final List<TimeWallpaperPhase> phases;

    /**
     * Constructor.
     *
     * @param numberOfFrames The number of frames.
     * @param phases         The list of phases.
     */
    public TimeWallpaper(final int numberOfFrames, final List<TimeWallpaperPhase> phases) {
        if (numberOfFrames < 1) {
            throw new RuntimeException("The time wallpaper has 0 frame, at least one expected");
        }
        if (phases.size() == 0) {
            throw new RuntimeException("The time wallpaper has 0 phase, at least one expected");
        }
        this.numberOfFrames = numberOfFrames;
        this.phases = phases.stream()
            .sorted(Comparator.comparing(TimeWallpaperPhase::time))
            .toList();
    }

    /**
     * Returns the number of frames (different images) in the dynamic wallpaper.
     */
    @Override
    public int getNumberOfFrames() {
        return this.numberOfFrames;
    }

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    @Override
    public int getNumberOfPhases() {
        return this.phases.size();
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public int getCurrentFrame(final CurrentEnvironment currentEnvironment) {
        final LocalTime currentTime = LocalTime.from(currentEnvironment.time().atZone(ZoneId.systemDefault()));

        // Try to find the earliest phase whose changing time is now or in the past
        Integer currentFrame = this.phases.stream()
            .filter(phase -> !phase.time().isAfter(currentTime))
            .findFirst()
            .map(TimeWallpaperPhase::frameIndex)
            .orElse(null);
        if (currentFrame != null) {
            return currentFrame;
        }

        // If there are no phase before the current time, return the last phase in the day
        return this.phases.get(this.phases.size() - 1).frameIndex();
    }
}
