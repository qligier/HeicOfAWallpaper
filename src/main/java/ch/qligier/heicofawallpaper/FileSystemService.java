package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.win32.Shell32Manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
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
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException exception) {
            LOG.severe("SHA-256 is not available");
            throw new RuntimeException(exception);
        }

        try (final DigestInputStream dis = new DigestInputStream(new FileInputStream(file), digest)) {
            while (dis.read() != -1) {
                // Read the file till the end
            }
            digest = dis.getMessageDigest();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
        final StringBuilder result = new StringBuilder();
        for (final byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
