package ch.qligier.heicofawallpaper.exception;

/**
 * Exception thrown, when processing a macOS dynamic wallpaper (HEIF file) that is invalid.
 *
 * @author Quentin Ligier
 **/
public class InvalidDynamicWallpaperException extends Exception {

    public InvalidDynamicWallpaperException(final String message) {
        super(message);
    }

    public InvalidDynamicWallpaperException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
