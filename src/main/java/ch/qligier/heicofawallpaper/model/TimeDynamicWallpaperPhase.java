package ch.qligier.heicofawallpaper.model;

import java.time.LocalTime;

/**
 * The definition of a phase in a time dynamic wallpaper.
 *
 * @param frameIndex The index of the frame to show.
 * @param time       The time to change the frame.
 * @author Quentin Ligier
 */
public record TimeDynamicWallpaperPhase(int frameIndex,
                                        LocalTime time) {
}
