package ch.qligier.heicofawallpaper.model;

/**
 * The interface of a dynamic wallpaper definition.
 *
 * @author Quentin Ligier
 */
public interface DynamicWallpaperInterface {

    /**
     * Returns the number of frames (different images) in the dynamic wallpaper.
     */
    int numberOfFrames();

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    int numberOfPhases();

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    int currentFrame(final CurrentEnvironment currentEnvironment);

    /**
     * Returns the type of the dynamic wallpaper.
     */
    DynamicWallpaperType type();
}
