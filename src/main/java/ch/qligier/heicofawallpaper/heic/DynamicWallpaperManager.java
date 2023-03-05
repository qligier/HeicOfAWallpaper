package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.FileSystemService;
import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 *
 *
 * @author Quentin Ligier
 */
public class DynamicWallpaperManager {

    public static void uncompress(final File dynamicWallpaperFile) {
        final String hash = FileSystemService.sha256File(dynamicWallpaperFile);

        // imagemagick
    }

    @Nullable
    public static String changeWallpaperTo() {
        return null;
    }

    public static String current
}
