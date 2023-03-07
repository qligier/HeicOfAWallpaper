package ch.qligier.heicofawallpaper.model;

import java.util.Objects;

/**
 * The definition of an appearance dynamic wallpaper.
 * <p>
 * An appearance wallpaper is a dynamic wallpaper that changes frame depending on whether the light or dark theme is
 * enabled.
 *
 * @param numberOfFrames  The number of frames in the dynamic wallpaper.
 * @param lightFrameIndex The frame index of the light theme.
 * @param darkFrameIndex  The frame index of the dark theme.
 * @author Quentin Ligier
 */
public record AppearanceDynamicWallpaper(int numberOfFrames,
                                         int lightFrameIndex,
                                         int darkFrameIndex) implements DynamicWallpaperInterface {

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    @Override
    public int numberOfPhases() {
        return 2;
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public int currentFrame(final CurrentEnvironment currentEnvironment) {
        return currentEnvironment.isLightThemeEnabled() ? this.lightFrameIndex : this.darkFrameIndex;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final AppearanceDynamicWallpaper that)) return false;
        return numberOfFrames == that.numberOfFrames
            && lightFrameIndex == that.lightFrameIndex
            && darkFrameIndex == that.darkFrameIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfFrames, lightFrameIndex, darkFrameIndex);
    }

    @Override
    public String toString() {
        return "AppearanceDynamicWallpaper{" +
            "numberOfFrames=" + this.numberOfFrames +
            ", lightFrameIndex=" + this.lightFrameIndex +
            ", darkFrameIndex=" + this.darkFrameIndex +
            '}';
    }
}
