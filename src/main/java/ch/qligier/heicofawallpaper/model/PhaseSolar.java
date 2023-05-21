package ch.qligier.heicofawallpaper.model;

/**
 * A phase of a solar dynamic wallpaper.
 *
 * @param frameIndex The index of the frame to show.
 * @param elevation  The sun elevation.
 * @param azimuth    The sun azimuth.
 * @author Quentin Ligier
 **/
public record PhaseSolar(short frameIndex,
                         float elevation,
                         float azimuth) {
}
