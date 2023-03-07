package ch.qligier.heicofawallpaper.configuration;

import java.util.Map;
import java.util.Objects;

/**
 * The user configuration of the application. It shall be saved and read each time the application quits and launches.
 *
 * @author Quentin Ligier
 **/
public class UserConfiguration {

    /**
     * The choices of dynamic wallpapers made by the user. The key is the monitor devicePath, the value is the wallpaper
     * filename.
     */
    private final Map<String, String> wallpaperChoices;
    
    /**
     *
     */
    private String wallpaperFolderPath;

    /**
     * Constructor.
     *
     * @param wallpaperChoices The choices of dynamic wallpapers made by the user.
     */
    public UserConfiguration(final String wallpaperFolderPath,
                             final Map<String, String> wallpaperChoices) {
        this.wallpaperFolderPath = Objects.requireNonNull(wallpaperFolderPath);
        this.wallpaperChoices = Objects.requireNonNull(wallpaperChoices);
    }

    public String getWallpaperFolderPath() {
        return this.wallpaperFolderPath;
    }

    public void setWallpaperFolderPath(final String wallpaperFolderPath) {
        this.wallpaperFolderPath = wallpaperFolderPath;
    }

    public Map<String, String> getWallpaperChoices() {
        return this.wallpaperChoices;
    }
}
