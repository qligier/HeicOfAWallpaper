package ch.qligier.heicofawallpaper.configuration;

import java.util.List;

/**
 * The runtime configuration of the application. It shall not be saved but shall be regenerated when the application
 * launches.
 *
 * @author Quentin Ligier
 **/
public class RuntimeConfiguration {

    /**
     * Whether the light or dark theme is enabled.
     */
    private boolean isLightThemeEnabled;

    /**
     * The details about connected monitors.
     */
    private List<Monitor> monitors;

    public int getNumberOfMonitors() {
        return this.monitors.size();
    }

    public record Monitor(String devicePath,
                          String deviceName,
                          int width,
                          int height) {
    }
}
