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
     * The number of monitors connected.
     */
    private transient int numberOfConnected;

    /**
     * Whether the light or dark theme is enabled.
     */
    private transient boolean isLightThemeEnabled;

    /**
     * The details about connected monitors.
     */
    private transient List<MonitorDetail> monitorDetails;

    public record MonitorDetail(String devicePath,
                                String deviceName,
                                int width,
                                int height) {
    }
}
