package ch.qligier.heicofawallpaper.model;

/**
 * The phase of an appearance dynamic wallpaper.
 *
 * @param lightFrameIndex The frame index of the light theme.
 * @param darkFrameIndex  The frame index of the dark theme.
 * @author Quentin Ligier
 */
public record PhaseAppearance(short lightFrameIndex,
                              short darkFrameIndex) {
}
