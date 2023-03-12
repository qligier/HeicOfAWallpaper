package ch.qligier.heicofawallpaper.service;

import ch.qligier.heicofawallpaper.win32.Shell32Manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Every thing related to the file system.
 * <p>
 * The only folders of the file system that the application accesses are:
 * <ul>
 *     <li>The <em>username</em>/AppData/Local/qligier/HeicOfAWallpaper folder, to store data needed by the
 *     application (READ and WRITE).</li>
 *     <li>The folder chosen by the user to store the dynamic wallpaper files (READ only). In that folder, only {@code
 *     .heic} files will be read.</li>
 * </ul>
 *
 * @author Quentin Ligier
 **/
public class FileSystemService {
    private static final Logger LOG = Logger.getLogger("FileSystemService");

    private static final String DATA_PATH = "qligier/HeicOfAWallpaper";
    private static final String CONFIGURATION_FILE_NAME = "configuration.json";
    private static final String WALLPAPERS_FOLDER_NAME = "wallpapers";

    /**
     * This class is not instantiable.
     */
    private FileSystemService() {
    }

    public static Path getDataPath() {
        return Shell32Manager.getLocalAppDataPath().resolve(DATA_PATH);
    }

    public static void ensureDataPathExists() {
        final File dataFolder = getDataPath().toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public static Path getUserConfigurationPath() {
        return getDataPath().resolve(CONFIGURATION_FILE_NAME);
    }

    public static List<File> findHeicFilesInPath(final Path folderPath) {
        final File folder = folderPath.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            LOG.info(() -> "findHeicFilesInPath: the path is not a directory: " + folderPath);
            return Collections.emptyList();
        }
        final var files = folder.listFiles((dir, name) -> name.endsWith(".heic"));
        if (files != null) {
            return List.of(files);
        }
        LOG.fine("findHeicFilesInPath: listFiles: has returned null");
        return Collections.emptyList();
    }

    public static String sha256File(final File file) {
        final MessageDigest digest;
        final byte[] bytes;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = Files.readAllBytes(file.toPath());
        } catch (final NoSuchAlgorithmException exception) {
            LOG.severe("SHA-256 is not available");
            throw new RuntimeException(exception);
        } catch (final IOException exception) {
            LOG.severe("Can't read file");
            throw new RuntimeException(exception);
        }
        final byte[] hash = digest.digest(bytes);

        final StringBuilder result = new StringBuilder();
        for (final byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
