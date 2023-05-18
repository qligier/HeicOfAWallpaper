package ch.qligier.heicofawallpaper.model;

/**
 * HeicOfAWallpaper
 *
 * @param frameIndex The index of the frame to show.
 * @author Quentin Ligier
 **/
public record PhaseSolar(int frameIndex,
                         float elevation,
                         float azimuth) {
}
