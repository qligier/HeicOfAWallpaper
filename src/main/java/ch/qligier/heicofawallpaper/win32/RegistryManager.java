package ch.qligier.heicofawallpaper.win32;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.Arrays;

/**
 * The manager for the Windows Registry.
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

    public static void listEdid() {
        final var keys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Enum" +
            "\\DISPLAY");
        System.out.println(Arrays.toString(keys));

        System.out.println("-------");

        var bytes = Advapi32Util.registryGetBinaryValue(WinReg.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet" +
            "\\Enum\\DISPLAY" +
            "\\DEL422F\\1&8713bca&0&UID0\\Device Parameters", "EDID");
        System.out.println(EdidUtil.toString(bytes));

        System.out.println("-------");

        bytes = Advapi32Util.registryGetBinaryValue(WinReg.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet" +
            "\\Enum\\DISPLAY" +
            "\\DEL422F\\5&2ff1101f&4&UID4353\\Device Parameters", "EDID");
        System.out.println(EdidUtil.toString(bytes));

        System.out.println("-------");

        bytes = Advapi32Util.registryGetBinaryValue(WinReg.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet" +
            "\\Enum\\DISPLAY" +
            "\\DEL422F\\5&2ff1101f&4&UID4356\\Device Parameters", "EDID");
        System.out.println(EdidUtil.toString(bytes));

        System.out.println("-------");
    }

    /**
     * Checks whether the light or dark theme is currently enabled by checking the Registry key. If it does not exist,
     * it defaults to light theme.
     *
     * @return {@code true} if the light theme is enabled, {@code false} if the dark theme is enabled.
     * @see <a href="https://github.com/Dansoftowner/jSystemThemeDetector">Dansoftowner/jSystemThemeDetector</a>
     */
    public boolean isLightThemeEnabled() {
        return !Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, LIGHT_THEME_PATH, LIGHT_THEME_VALUE)
            || Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, LIGHT_THEME_PATH, LIGHT_THEME_VALUE) == 1;
    }
}
