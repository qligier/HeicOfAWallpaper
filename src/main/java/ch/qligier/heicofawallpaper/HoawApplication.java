package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.configuration.RuntimeConfiguration;
import ch.qligier.heicofawallpaper.configuration.StaticConfiguration;
import ch.qligier.heicofawallpaper.configuration.UserConfiguration;
import ch.qligier.heicofawallpaper.gui.TrayIconManager;
import ch.qligier.heicofawallpaper.win32.DesktopWallpaperManager;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The main Heic Of a Wallpaper application.
 *
 * @author Quentin Ligier
 */
public class HoawApplication extends Application {

    /**
     * The tray icon manager.
     */
    private TrayIconManager trayIcon;

    /**
     * The main JavaFX stage.
     */
    private Stage mainStage;

    /**
     * The runtime configuration.
     */
    private RuntimeConfiguration runtimeConfiguration;

    /**
     * The user configuration.
     */
    private UserConfiguration userConfiguration;

    /**
     *
     */
    private DesktopWallpaperManager desktopWallpaperManager;

    /**
     * Entry point for the CLI.
     *
     * @param args The CLI arguments.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * The main entry point for all JavaFX applications. The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running. NOTE: This method is called on the JavaFX
     * Application Thread.
     *
     * @param stage the primary stage for this application, onto which the application scene can be set. Applications
     *              may create other stages, if needed, but they will not be primary stages.
     * @throws IOException
     */
    @Override
    public void start(final Stage stage) throws IOException {
        this.trayIcon = new TrayIconManager(stage, this);
        this.mainStage = stage;

        this.desktopWallpaperManager = DesktopWallpaperManager.create();

        this.runtimeConfiguration = this.loadRuntimeConfiguration();

        FileSystemService.ensureDataPathExists();
        this.userConfiguration = this.loadUserConfiguration();
    }

    public void openSettingWindow() {
    }

    public void openAboutWindow() throws IOException {
        final Stage newWindow = new Stage();
        final FXMLLoader fxmlLoader = new FXMLLoader(HoawApplication.class.getResource("/view/about.fxml"));
        final Scene scene = new Scene(fxmlLoader.load(), 450, 600);
        newWindow.setTitle("About " + StaticConfiguration.APP_NAME + " v" + StaticConfiguration.APP_VERSION);
        newWindow.setScene(scene);
        newWindow.initStyle(StageStyle.UTILITY);
        newWindow.initModality(Modality.WINDOW_MODAL);
        newWindow.initOwner(this.mainStage);
        newWindow.setResizable(false);
        newWindow.getIcons().add(Utils.getLogo());
        newWindow.show();
    }

    /**
     * Loads and returns the runtime configuration.
     */
    private RuntimeConfiguration loadRuntimeConfiguration() {
        final int numberOfConnectedMonitors = this.desktopWallpaperManager.getMonitorDevicePathCount();
        final GraphicsDevice[] monitors = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final List<RuntimeConfiguration.Monitor> monitorDetails = new ArrayList<>(monitors.length);
        for (final GraphicsDevice monitor : monitors) {
            final DisplayMode displayMode = monitor.getDisplayMode();
            monitorDetails.add(new RuntimeConfiguration.Monitor(
                monitor.getIDstring(),
                "",
                displayMode.getWidth(),
                displayMode.getHeight()
            ));
        }
        return null;
    }

    /**
     * Loads the user configuration from disk and returns it.
     */
    private UserConfiguration loadUserConfiguration() {
        final Gson gson = new Gson();
        try {
            final String serialized = Files.readString(FileSystemService.getUserConfigurationPath(), StandardCharsets.UTF_8);
            return gson.fromJson(serialized, UserConfiguration.class);
        } catch (final IOException exception) {
            return new UserConfiguration();
        }
    }

    /**
     * Saves the user configuration to disk.
     */
    private void saveUserConfiguration(final UserConfiguration userConfiguration) throws IOException {
        final Gson gson = new Gson();
        final String serialized = gson.toJson(userConfiguration);
        Files.writeString(
            FileSystemService.getUserConfigurationPath(),
            serialized,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
