package ch.qligier.heicofawallpaper.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalTime;

/**
 * The current environment, for dynamic wallpaper evaluation.
 *
 * @param currentTime         The current time.
 * @param isLightThemeEnabled Whether the light or dark theme is enabled.
 * @param latitude            The location latitude, or {@code null}.
 * @param longitude           The location longitude, or {@code null}.
 * @author Quentin Ligier
 */
public record CurrentEnvironment(LocalTime currentTime,
                                 boolean isLightThemeEnabled,
                                 @Nullable Long latitude,
                                 @Nullable Long longitude) {
}
