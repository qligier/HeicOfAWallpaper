package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.configuration.RuntimeConfiguration;
import ch.qligier.heicofawallpaper.configuration.StaticConfiguration;
import ch.qligier.heicofawallpaper.configuration.UserConfiguration;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import ch.qligier.heicofawallpaper.gui.TrayIconManager;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.model.CurrentEnvironment;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperInterface;
import ch.qligier.heicofawallpaper.service.DynamicWallpaperService;
import ch.qligier.heicofawallpaper.service.FileSystemService;
import ch.qligier.heicofawallpaper.win32.DesktopWallpaperManager;
import ch.qligier.heicofawallpaper.win32.RegistryManager;
import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinNT;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

/**
 * The main Heic Of a Wallpaper application.
 *
 * @author Quentin Ligier
 */
public class HoawApplication extends Application {
    private static final Logger LOG = Logger.getLogger("HoawApplication");

    private static final long REFRESH_DELAY_MS = 1000 * 60;

    /**
     *
     */
    private final MetadataExtractor metadataExtractor = new MetadataExtractor();

    /**
     *
     */
    private final DynamicWallpaperService dynamicWallpaperService = new DynamicWallpaperService(this.metadataExtractor);

    /**
     * The main JavaFX stage. The field is assigned in {@link #start(Stage)}.
     */
    @MonotonicNonNull
    private Stage mainStage;

    /**
     * The runtime configuration. The field is assigned in {@link #start(Stage)}.
     */
    @MonotonicNonNull
    private RuntimeConfiguration runtimeConfiguration;

    /**
     * The user configuration. The field is assigned in {@link #start(Stage)}.
     */
    @MonotonicNonNull
    private UserConfiguration userConfiguration;

    /**
     * The field is assigned in {@link #start(Stage)}.
     */
    @MonotonicNonNull
    private DesktopWallpaperManager desktopWallpaperManager;

    @MonotonicNonNull
    private Map<String, DynamicWallpaperInterface> wallpapersInFolder;

    @MonotonicNonNull
    private Timer timer;

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
     */
    @Override
    public void start(final Stage stage) {
        System.out.println("start");
        this.mainStage = stage;
        new TrayIconManager(this.mainStage, this);

        this.desktopWallpaperManager = DesktopWallpaperManager.create();
        this.runtimeConfiguration = this.loadRuntimeConfiguration();
        FileSystemService.ensureDataPathExists();
        this.userConfiguration = this.loadUserConfiguration();
        this.wallpapersInFolder = this.loadWallpapersFromFolder();

        this.timer = new Timer();
        final Runnable refresh = this::refreshWallpaper;
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refresh.run();
            }
        }, 500, REFRESH_DELAY_MS);

        this.showWindow();
    }

    public void hideWindow() {
    }

    public void showWindow() {
        System.out.println("showWindow");
        final Stage newWindow = new Stage();
        newWindow.setTitle(StaticConfiguration.APP_NAME + " v" + StaticConfiguration.APP_VERSION);
        newWindow.setScene(new Scene(new MainWindow(this), 600, 600));
        newWindow.initStyle(StageStyle.DECORATED);
        newWindow.initModality(Modality.NONE);
        newWindow.initOwner(this.mainStage);
        newWindow.setResizable(false);
        newWindow.getIcons().add(Utils.getLogo());
        newWindow.show();

    }

    /**
     * Loads and returns the runtime configuration.
     */
    private RuntimeConfiguration loadRuntimeConfiguration() {
        final GraphicsDevice[] monitors = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final List<RuntimeConfiguration.Monitor> monitorDetails = new ArrayList<>(monitors.length);
        int i = 0;
        for (final GraphicsDevice monitor : monitors) {
            final DisplayMode displayMode = monitor.getDisplayMode();
            monitorDetails.add(new RuntimeConfiguration.Monitor(
                monitor.getIDstring(),
                "",
                i++, // TODO
                displayMode.getWidth(),
                displayMode.getHeight()
            ));
        }
        return new RuntimeConfiguration(true, monitorDetails);
    }

    /**
     * Loads the user configuration from disk and returns it.
     */
    private UserConfiguration loadUserConfiguration() {
        final Gson gson = new Gson();
        try {
            final String serialized = Files.readString(FileSystemService.getUserConfigurationPath(),
                                                       StandardCharsets.UTF_8);
            return gson.fromJson(serialized, UserConfiguration.class);
        } catch (final IOException exception) {
            return new UserConfiguration("D:\\Programmation\\Java\\HeicOfAWallpaper\\src\\main\\resources\\heic",
                                         //Shell32Manager.getUserPicturesPath().toString(),
                                         new HashMap<>());
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

    private void refreshWallpaper() {
        System.out.println("refreshWallpaper");
        this.runtimeConfiguration = this.loadRuntimeConfiguration();

        final CurrentEnvironment currentEnv = new CurrentEnvironment(
            Instant.now(),
            RegistryManager.isLightThemeEnabled(),
            null,
            null
        );

        // Create an instance to bind the COM channel to the current thread
        final WinNT.HRESULT result = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        COMUtils.checkRC(result);
        DesktopWallpaperManager manager = null;
        try {
            manager = DesktopWallpaperManager.create();
            for (final RuntimeConfiguration.Monitor monitor : this.runtimeConfiguration.monitors()) {
                final String wallpaperFilename = this.userConfiguration.getWallpaperChoices().get(monitor.devicePath());
                if (wallpaperFilename == null) {
                    // The user has not set a wallpaper for that screen
                    continue;
                }

                // Find the definition of the given wallpaper
                final var wallpaper = this.wallpapersInFolder.get(wallpaperFilename);
                if (wallpaper == null) {
                    // Can't find, must delete from choice
                    continue;
                }

                final int requestedFrame = wallpaper.currentFrame(currentEnv);
                final String frameFilename = String.format("%s-%d.jpg",
                                                           wallpaperFilename.substring(0,
                                                                                       wallpaperFilename.length() - 5),
                                                           requestedFrame);
                final File wallpaperFile = Path.of(this.userConfiguration.getWallpaperFolderPath())
                    .resolve(wallpaperFilename)
                    .toFile();
                final String hash = FileSystemService.sha256File(wallpaperFile);
                final Path framePath = FileSystemService.getDataPath()
                    .resolve(hash)
                    .resolve(frameFilename);
                if (!framePath.toFile().exists()) {
                    LOG.warning("The frame path does not exist");
                    continue;
                }

                final String monitorPath = manager.getMonitorDevicePathAt(monitor.index());
                final Path currentWallpaper = Path.of(manager.getJpgWallpaper(monitorPath));
                if (framePath.equals(currentWallpaper)) {
                    LOG.finest("The requested frame is already used");
                    continue;
                }
                System.out.println("Setting wallpaper: " + framePath);
                manager.setJpgWallpaper(monitorPath, framePath);
            }
        } finally {
            if (manager != null) {
                manager.Release();
            }
            Ole32.INSTANCE.CoUninitialize();
        }
    }

    private Map<String, DynamicWallpaperInterface> loadWallpapersFromFolder() {
        final List<File> heicFiles;
        try {
            heicFiles =
                FileSystemService.findHeicFilesInPath(Path.of(this.userConfiguration.getWallpaperFolderPath()));
        } catch (final Exception exception) {
            return Collections.emptyMap();
        }

        final Map<String, DynamicWallpaperInterface> wallpapers = new HashMap<>(heicFiles.size());
        this.metadataExtractor.start();
        heicFiles.parallelStream()
            .forEach(heicFile -> {
                try {
                    wallpapers.put(heicFile.getName(), this.dynamicWallpaperService.loadDefinition(heicFile));
                } catch (final Exception exception) {

                }
            });
        try {
            this.metadataExtractor.close();
        } catch (final Exception exception) {
            // Let's ignore it for now
        }
        return wallpapers;
    }

    private void ensureWallpaperIsExtracted(final String wallpaperFilename) {
        System.out.println("ensureWallpaperIsExtracted");
        final File dynamicWallpaperFile =
            Path.of(this.userConfiguration.getWallpaperFolderPath()).resolve(wallpaperFilename).toFile();
        if (!dynamicWallpaperFile.isFile() || !dynamicWallpaperFile.canRead()) {
            LOG.info(() -> "The wallpaper at ' " + dynamicWallpaperFile.getAbsolutePath() + " ' is not readable");
            return;
        }
        final String hash = FileSystemService.sha256File(dynamicWallpaperFile);
        final File extractionFolder = FileSystemService.getDataPath().resolve(hash).toFile();
        if (extractionFolder.exists()) {
            return;
        }
        try {
            this.dynamicWallpaperService.extract(dynamicWallpaperFile, hash);
        } catch (final Exception exception) {
            Utils.showException(exception);
        }
    }

    public MetadataExtractor getMetadataExtractor() {
        return this.metadataExtractor;
    }

    public DynamicWallpaperService getDynamicWallpaperService() {
        return this.dynamicWallpaperService;
    }

    public Stage getMainStage() {
        return this.mainStage;
    }

    public RuntimeConfiguration getRuntimeConfiguration() {
        return this.runtimeConfiguration;
    }

    public UserConfiguration getUserConfiguration() {
        return this.userConfiguration;
    }

    public DesktopWallpaperManager getDesktopWallpaperManager() {
        return this.desktopWallpaperManager;
    }

    public Map<String, DynamicWallpaperInterface> getWallpapersInFolder() {
        return this.wallpapersInFolder;
    }

    public void setWallpaperFolderPath(final String wallpaperFolderPath) {
        this.userConfiguration.setWallpaperFolderPath(wallpaperFolderPath);
        this.wallpapersInFolder = this.loadWallpapersFromFolder();
    }

    public void setWallpaperChoice(final String monitorDevicePath,
                                   final String wallpaperFilename) {
        System.out.println("setWallpaperChoice");
        if (!this.wallpapersInFolder.containsKey(wallpaperFilename)) {
            LOG.info(() -> "The wallpaper filename '" + wallpaperFilename + "' is not present in the folder");
            return;
        }
        this.ensureWallpaperIsExtracted(wallpaperFilename);
        this.userConfiguration.getWallpaperChoices().put(monitorDevicePath, wallpaperFilename);
    }
}
