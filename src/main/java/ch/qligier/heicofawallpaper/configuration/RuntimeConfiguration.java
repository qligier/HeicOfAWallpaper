package ch.qligier.heicofawallpaper.configuration;

import java.util.List;

/**
 * The runtime configuration of the application. It shall not be saved but shall be regenerated when the application
 * launches.
 *
 * @param isLightThemeEnabled Whether the light or dark theme is enabled.
 * @param monitors            The details about connected monitors.
 * @author Quentin Ligier
 **/
public record RuntimeConfiguration(boolean isLightThemeEnabled,
                                   List<Monitor> monitors) {

    public int getNumberOfMonitors() {
        return this.monitors.size();
    }

    public record Monitor(String devicePath,
                          String deviceName,
                          int index,
                          int width,
                          int height) {
    }
}
