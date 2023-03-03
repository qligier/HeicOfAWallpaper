package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.win32.Shell32Manager;

import java.io.File;
import java.nio.file.Path;

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
public class FileSystem {

    private final static String DATA_PATH = "qligier/HeicOfAWallpaper";
    private final static String CONFIGURATION_FILE_NAME = "configuration.json";
    private final static String WALLPAPERS_FOLDER_NAME = "wallpapers";

    /**
     * This class is not instantiable.
     */
    private FileSystem() {
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

    public static File getUserConfigurationFile() {
        return getDataPath().resolve(CONFIGURATION_FILE_NAME).toFile();
    }
}
