package ch.qligier.heicofawallpaper.model;

/**
 * The definition of an appearance dynamic wallpaper.
 * <p>
 * An appearance wallpaper is a dynamic wallpaper that changes frame depending on whether the light or dark theme is
 * enabled.
 *
 * @author Quentin Ligier
 */
public class AppearanceWallpaper implements DynamicWallpaperInterface {

    /**
     * The number of frames in the dynamic wallpaper.
     */
    private final int numberOfFrames;

    /**
     * The frame index of the light theme.
     */
    private final int lightFrameIndex;

    /**
     * The frame index of the dark theme.
     */
    private final int darkFrameIndex;

    /**
     * Constructor.
     *
     * @param numberOfFrames  The number of frames in the dynamic wallpaper.
     * @param lightFrameIndex The frame index of the light theme.
     * @param darkFrameIndex  The frame index of the dark theme.
     */
    public AppearanceWallpaper(final int numberOfFrames, final int lightFrameIndex, final int darkFrameIndex) {
        this.numberOfFrames = numberOfFrames;
        this.lightFrameIndex = lightFrameIndex;
        this.darkFrameIndex = darkFrameIndex;
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
        return 2;
    }

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    @Override
    public int getCurrentFrame(final CurrentEnvironment currentEnvironment) {
        return currentEnvironment.isLightThemeEnabled() ? this.lightFrameIndex : this.darkFrameIndex;
    }
}
