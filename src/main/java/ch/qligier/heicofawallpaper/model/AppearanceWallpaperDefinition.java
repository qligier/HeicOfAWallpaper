package ch.qligier.heicofawallpaper.model;

import java.util.Objects;

/**
 * The definition of an appearance dynamic wallpaper.
 * <p>
 * An appearance wallpaper is a dynamic wallpaper that changes frame depending on whether the light or dark theme is
 * enabled.
 *
 * @author Quentin Ligier
 */
public class AppearanceWallpaperDefinition extends DynamicWallpaperDefinition {

    /**
     * The frame index of the light theme.
     */
    short lightFrameIndex;

    /**
     * The frame index of the dark theme.
     */
    short darkFrameIndex;

    public AppearanceWallpaperDefinition(final short lightFrameIndex, final short darkFrameIndex) {
        this.lightFrameIndex = lightFrameIndex;
        this.darkFrameIndex = darkFrameIndex;
    }

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    @Override
    public short numberOfPhases() {
        return 2;
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public short currentFrame(final CurrentEnvironment currentEnvironment) {
        return currentEnvironment.isLightThemeEnabled() ? this.lightFrameIndex : this.darkFrameIndex;
    }

    /**
     * Returns the type of the dynamic wallpaper.
     */
    @Override
    public DynamicWallpaperType type() {
        return DynamicWallpaperType.APPEARANCE;
    }

    public short getLightFrameIndex() {
        return this.lightFrameIndex;
    }

    public void setLightFrameIndex(final short lightFrameIndex) {
        this.lightFrameIndex = lightFrameIndex;
    }

    public short getDarkFrameIndex() {
        return this.darkFrameIndex;
    }

    public void setDarkFrameIndex(final short darkFrameIndex) {
        this.darkFrameIndex = darkFrameIndex;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final AppearanceWallpaperDefinition that)) return false;
        return lightFrameIndex == that.lightFrameIndex
            && darkFrameIndex == that.darkFrameIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lightFrameIndex, darkFrameIndex);
    }

    @Override
    public String toString() {
        return "AppearanceWallpaperDefinition{" +
            "lightFrameIndex=" + lightFrameIndex +
            ", darkFrameIndex=" + darkFrameIndex +
            ", height=" + height +
            ", width=" + width +
            ", hash='" + fileHash + '\'' +
            ", filename='" + filename + '\'' +
            ", numberOfFrames=" + numberOfFrames +
            '}';
    }
}
