package ch.qligier.heicofawallpaper.model;

/**
 * HeicOfAWallpaper
 *
 * @param frameIndex The index of the frame to show.
 * @author Quentin Ligier
 **/
public record SolarDynamicWallpaperPhase(int frameIndex,
                                         double elevation,
                                         double azimuth) {
}
