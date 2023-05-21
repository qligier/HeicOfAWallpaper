package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.configuration.RuntimeConfiguration;
import ch.qligier.heicofawallpaper.configuration.StaticConfiguration;
import ch.qligier.heicofawallpaper.configuration.UserConfiguration;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import ch.qligier.heicofawallpaper.gui.TrayIconManager;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.heic.PreviewGenerator;
import ch.qligier.heicofawallpaper.model.CachedDynWallDefinition;
import ch.qligier.heicofawallpaper.model.CurrentEnvironment;
import ch.qligier.heicofawallpaper.model.DynWallDefinition;
import ch.qligier.heicofawallpaper.model.DynWallSelection;
import ch.qligier.heicofawallpaper.service.DynamicWallpaperService;
import ch.qligier.heicofawallpaper.service.FileSystemService;
import ch.qligier.heicofawallpaper.service.PhaseEvaluator;
import ch.qligier.heicofawallpaper.win32.DesktopWallpaperManager;
import ch.qligier.heicofawallpaper.win32.RegistryManager;
import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinNT;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @MonotonicNonNull
    private final ObservableList<DynWallDefinition> wallpaperDefinitions =
        FXCollections.observableList(new ArrayList<>(8));
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

        new Thread(this::loadWallpapersFromFolder).start();

        final Timer timer = new Timer();
        final Runnable refresh = this::refreshWallpaper;
        timer.schedule(new TimerTask() {
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
        this.appWindow.setScene(new Scene(new MainWindow(this), 840, 960));
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
            LocalTime.from(Instant.now().atZone(ZoneId.systemDefault())),
            RegistryManager.isLightThemeEnabled(),
            null,
            null
        );
        final PhaseEvaluator evaluator = new PhaseEvaluator(currentEnv);

        // Create an instance to bind the COM channel to the current thread
        final WinNT.HRESULT result = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        COMUtils.checkRC(result);
        DesktopWallpaperManager manager = null;
        try {
            manager = DesktopWallpaperManager.create();
            for (final RuntimeConfiguration.Monitor monitor : this.runtimeConfiguration.monitors()) {
                final DynWallSelection selection =
                    this.userConfiguration.getWallpaperChoices().get(monitor.devicePath());
                if (selection == null) {
                    // The user has not set a wallpaper for that screen
                    continue;
                }

                // Find the definition of the given wallpaper
                final DynWallDefinition definition = this.wallpaperDefinitions.stream()
                    .filter(otherDefinition -> selection.filename().equals(otherDefinition.filename()))
                    .findAny().orElse(null);
                if (definition == null) {
                    // Can't find, must delete from choice
                    continue;
                }
                final String fileHash = definition.fileHash();

                final int requestedFrame = switch (selection.type()) {
                    case APPEARANCE -> {
                        assert definition.appearancePhase() != null;
                        yield evaluator.evaluateAppearanceFrame(definition.appearancePhase());
                    }
                    case SOLAR -> {
                        assert definition.solarPhases() != null;
                        yield evaluator.evaluateSolarFrame(definition.solarPhases());
                    }
                    case TIME -> {
                        assert definition.timePhases() != null;
                        yield evaluator.evaluateTimeFrame(definition.timePhases());
                    }
                };
                final String frameFilename = String.format("%s-%d.jpg",
                                                           fileHash.substring(0, fileHash.length() - 5),
                                                           requestedFrame);
                final File wallpaperFile = Path.of(this.userConfiguration.getWallpaperFolderPath())
                    .resolve(fileHash)
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

    private void loadWallpapersFromFolder() {
        // First, we load wallpaper definitions from the cache
        final List<DynWallDefinition> allDefinitions = new ArrayList<>(8);
        try (final Stream<Path> stream = Files.list(FileSystemService.getDataPath())) {
            stream.parallel()
                .filter(path -> path.toFile().isDirectory())
                .filter(path -> path.resolve(FileSystemService.CACHE_DEFINITION_FILE_NAME).toFile().isFile())
                .map(path -> {
                    try {
                        final String hash = path.toFile().getName();
                        final String content = Files.readString(path.resolve(FileSystemService.CACHE_DEFINITION_FILE_NAME),
                                                                StandardCharsets.UTF_8);
                        final CachedDynWallDefinition cached = this.gson.fromJson(content,
                                                                                  CachedDynWallDefinition.class);
                        return cached.toDynamicWallpaperDefinition(hash);
                    } catch (final Exception exception) {
                        LOG.warning(exception.toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(definition -> {
                    allDefinitions.add(definition);
                    this.addWallpaperDefinition(definition);
                });
        } catch (final IOException exception) {
            // Log
        }
        final Set<String> cachedFilenames = allDefinitions.parallelStream()
            .map(DynWallDefinition::filename)
            .collect(Collectors.toSet());

        // Then, we list the file in the source folder
        final List<File> wallpaperToExtract = new ArrayList<>(8);
        try (final var stream = FileSystemService.findHeicFilesInPath(Path.of(this.getUserConfiguration().getWallpaperFolderPath()))) {
            stream.parallel()
                .map(Path::toFile)
                .filter(file -> !cachedFilenames.contains(file.getName()))
                .forEach(wallpaperToExtract::add);
        } catch (final IOException exception) {
            // Log
        }

        // We compare and find wallpapers that have not been extracted
        try {
            this.metadataExtractor.start();
            // TODO: parallel
            for (final File heicFile : wallpaperToExtract) {
                final var definition = this.dynamicWallpaperService.loadDefinitionFromFile(heicFile);

                final Path cacheDirectory = FileSystemService.getDataPath().resolve(definition.fileHash());
                cacheDirectory.toFile().mkdirs();

                // Extract the wallpaper
                this.dynamicWallpaperService.extract(heicFile, definition.fileHash());

                // Create the thumbnail
                final BufferedImage previewImage = PreviewGenerator.generate(definition);
                final File previewFile = cacheDirectory.resolve("preview.png").toFile();
                ImageIO.write(previewImage, "png", previewFile);

                // Put the definition in a cache
                final var cacheWallpaperDefinition =
                    CachedDynWallDefinition.fromDynamicWallpaperDefinition(definition);
                final Path cacheDefinitionFile = cacheDirectory.resolve("definition.json");
                Files.writeString(cacheDefinitionFile,
                                  this.gson.toJson(cacheWallpaperDefinition),
                                  StandardOpenOption.WRITE,
                                  StandardOpenOption.TRUNCATE_EXISTING,
                                  StandardOpenOption.CREATE);

                this.addWallpaperDefinition(definition);
            }
        } catch (final Exception exception) {
            LOG.warning(exception.toString());
        } finally {
            this.metadataExtractor.close();
        }
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

    public ObservableList<DynWallDefinition> getWallpaperDefinitions() {
        return this.wallpaperDefinitions;
    }

    public void setWallpaperFolderPath(final String wallpaperFolderPath) {
        this.userConfiguration.setWallpaperFolderPath(wallpaperFolderPath);
        this.loadWallpapersFromFolder();
    }

    public void setWallpaperChoice(final String monitorDevicePath,
                                   final DynWallSelection selection) {
        System.out.println("setWallpaperChoice");
        /*if (!this.wallpaperDefinitions.containsKey(wallpaperFilename)) {
            LOG.info(() -> "The wallpaper filename '" + wallpaperFilename + "' is not present in the folder");
            return;
        }*/
        this.ensureWallpaperIsExtracted(selection.filename());
        this.userConfiguration.getWallpaperChoices().put(monitorDevicePath, selection);
    }

    public synchronized void addWallpaperDefinition(final DynWallDefinition definition) {
        this.wallpaperDefinitions.add(definition);
    }
}
