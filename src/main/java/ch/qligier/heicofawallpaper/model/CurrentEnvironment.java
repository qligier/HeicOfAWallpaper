package ch.qligier.heicofawallpaper.model;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record CurrentEnvironment(Instant time,
                                 boolean isLightThemeEnabled,
                                 @Nullable long latitude,
                                 @Nullable long longitude) {
}
