package ch.qligier.heicofawallpaper.configuration;

import java.util.Map;

/**
 * The user configuration of the application. It shall be saved and read each time the application quits and launches.
 *
 * @author Quentin Ligier
 **/
public class UserConfiguration {

    /**
     * The choices of dynamic wallpapers made by the user. The key is ??, the value is the dynamic wallpaper filename.
     */
    private final Map<String, String> wallpaperChoices;
}
