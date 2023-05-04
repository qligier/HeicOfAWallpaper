package ch.qligier.heicofawallpaper.win32;

import ch.qligier.heicofawallpaper.Utils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.Arrays;

/**
 * The Java manager for the Windows Registry.
 *
 * @author Quentin Ligier
 **/
public class RegistryManager {

    /**
     * The Registry path for the light theme setting.
     */
    private static final String LIGHT_THEME_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    /**
     * The Registry value name for the light theme setting. There is also 'SystemUsesLightTheme'.
     */
    private static final String LIGHT_THEME_VALUE = "AppsUseLightTheme";

    /**
     * Checks whether the light or dark theme is currently enabled by checking the Registry key. If it does not exist,
     * it defaults to light theme.
     *
     * @return {@code true} if the light theme is enabled, {@code false} if the dark theme is enabled.
     * @see <a href="https://github.com/Dansoftowner/jSystemThemeDetector">Dansoftowner/jSystemThemeDetector</a>
     */
    public static boolean isLightThemeEnabled() {
        return !Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, LIGHT_THEME_PATH, LIGHT_THEME_VALUE)
            || Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, LIGHT_THEME_PATH, LIGHT_THEME_VALUE) == 1;
    }
}
