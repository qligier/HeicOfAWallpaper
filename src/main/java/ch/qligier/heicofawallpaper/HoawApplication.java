package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.configuration.RuntimeConfiguration;
import ch.qligier.heicofawallpaper.configuration.StaticConfiguration;
import ch.qligier.heicofawallpaper.configuration.UserConfiguration;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import ch.qligier.heicofawallpaper.gui.TrayIconManager;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.heic.PreviewGenerator;
import ch.qligier.heicofawallpaper.model.CachedWallpaperDefinition;
import ch.qligier.heicofawallpaper.model.CurrentEnvironment;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperDefinition;
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
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    private static final int REFRESH_DELAY_MS = 1000 * 60;

    private final Gson gson = new Gson();

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
     * The application window, if it is opened.
     */
    @Nullable
    private Stage appWindow;

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
    private List<DynamicWallpaperDefinition> wallpaperDefinitions;

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
        this.mainStage = stage;
        new TrayIconManager(this.mainStage, this);

        this.desktopWallpaperManager = DesktopWallpaperManager.create();
        this.runtimeConfiguration = this.loadRuntimeConfiguration();
        FileSystemService.ensureDataPathExists();
        this.userConfiguration = this.loadUserConfiguration();
        this.wallpaperDefinitions = this.loadWallpapersFromFolder();

        this.timer = new Timer();
        final Runnable refresh = this::refreshWallpaper;
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refresh.run();
            }
        }, 500, REFRESH_DELAY_MS);

        this.openAppWindow();
    }

    public void closeAppWindow() {
        if (this.appWindow == null) {
            return;
        }
        this.appWindow.close();
        this.appWindow = null;
    }

    /**
     * Opens the application window.
     */
    public void openAppWindow() {
        if (this.appWindow != null) {
            return;
        }
        this.appWindow = new Stage();
        this.appWindow.setTitle(StaticConfiguration.APP_NAME + " v" + StaticConfiguration.APP_VERSION);
        this.appWindow.setScene(new Scene(new MainWindow(this), 600, 600));
        this.appWindow.initStyle(StageStyle.DECORATED);
        this.appWindow.initModality(Modality.NONE);
        this.appWindow.initOwner(this.mainStage);
        this.appWindow.setResizable(false);
        this.appWindow.getIcons().add(Utils.getLogo());
        this.appWindow.show();
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
                final String wallpaperIdentifier = this.userConfiguration.getWallpaperChoices().get(monitor.devicePath());
                if (wallpaperIdentifier == null) {
                    // The user has not set a wallpaper for that screen
                    continue;
                }

                // Find the definition of the given wallpaper
                final DynamicWallpaperDefinition wallpaperDefinition = this.wallpaperDefinitions.stream()
                    .filter(definition -> wallpaperIdentifier.equals(definition.identifier()))
                    .findAny().orElse(null);
                if (wallpaperDefinition == null) {
                    // Can't find, must delete from choice
                    continue;
                }

                final int requestedFrame = wallpaperDefinition.currentFrame(currentEnv);
                final String frameFilename = String.format("%s-%d.jpg",
                                                           wallpaperIdentifier.substring(0,
                                                                                         wallpaperIdentifier.length() - 5),
                                                           requestedFrame);
                final File wallpaperFile = Path.of(this.userConfiguration.getWallpaperFolderPath())
                    .resolve(wallpaperIdentifier)
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

    private List<DynamicWallpaperDefinition> loadWallpapersFromFolder() {
        // First, we load wallpaper definitions from the cache

        // Then, we list the file in the source folder
        final List<File> wallpaperToExtract =
            FileSystemService.findHeicFilesInPath(Path.of(this.getUserConfiguration().getWallpaperFolderPath()));

        // We compare and find wallpapers that have not been extracted
        final List<DynamicWallpaperDefinition> allDefinitions = new ArrayList<>(wallpaperToExtract.size());
        try {
            this.metadataExtractor.start();
            for (final File heicFile : wallpaperToExtract) {
                final var definitions = this.dynamicWallpaperService.loadDefinitionsFromFile(heicFile);

                final Path cacheDirectory = FileSystemService.getDataPath().resolve(definitions.get(0).fileHash());
                cacheDirectory.toFile().mkdirs();

                // Extract the wallpaper
                this.dynamicWallpaperService.extract(heicFile, definitions.get(0).fileHash());

                // Create the thumbnail
                final BufferedImage previewImage = PreviewGenerator.generate(definitions.get(0));
                final File previewFile = cacheDirectory.resolve("preview.png").toFile();
                ImageIO.write(previewImage, "png", previewFile);

                // Put the definition in a cache
                final var cacheWallpaperDefinition =
                    CachedWallpaperDefinition.fromDynamicWallpaperDefinition(definitions.get(0));
                final Path cacheDefinitionFile = cacheDirectory.resolve("definition.json");
                Files.writeString(cacheDefinitionFile,
                                  this.gson.toJson(cacheWallpaperDefinition),
                                  StandardOpenOption.WRITE,
                                  StandardOpenOption.TRUNCATE_EXISTING,
                                  StandardOpenOption.CREATE);

                allDefinitions.addAll(definitions);
            }
        } catch (final Exception exception) {
            // Log exception, ignore file :(
        } finally {
            this.metadataExtractor.close();
        }
        return allDefinitions;

        //

        /*final List<File> heicFiles;
        try {
            heicFiles =
                FileSystemService.findHeicFilesInPath(Path.of(this.userConfiguration.getWallpaperFolderPath()));
        } catch (final Exception exception) {
            return Collections.emptyMap();
        }

        final Map<String, DynamicWallpaperDefinition> wallpapers = new HashMap<>(heicFiles.size());
        this.metadataExtractor.start();
        heicFiles.parallelStream()
            .forEach(heicFile -> {
                try {
                    wallpapers.put(heicFile.getName(),
                                   this.dynamicWallpaperService.loadDefinitionsFromFile(heicFile).get(0));
                } catch (final Exception exception) {

                }
            });
        try {
            this.metadataExtractor.close();
        } catch (final Exception exception) {
            // Let's ignore it for now
        }
        return wallpapers;*/
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

    public List<DynamicWallpaperDefinition> getWallpapersInFolder() {
        return this.wallpaperDefinitions;
    }

    public void setWallpaperFolderPath(final String wallpaperFolderPath) {
        this.userConfiguration.setWallpaperFolderPath(wallpaperFolderPath);
        this.wallpaperDefinitions = this.loadWallpapersFromFolder();
    }

    public void setWallpaperChoice(final String monitorDevicePath,
                                   final String wallpaperFilename) {
        System.out.println("setWallpaperChoice");
        /*if (!this.wallpaperDefinitions.containsKey(wallpaperFilename)) {
            LOG.info(() -> "The wallpaper filename '" + wallpaperFilename + "' is not present in the folder");
            return;
        }*/
        this.ensureWallpaperIsExtracted(wallpaperFilename);
        this.userConfiguration.getWallpaperChoices().put(monitorDevicePath, wallpaperFilename);
    }
}
