package ch.qligier.heicofawallpaper.heic;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The extractor of metadata for dynamic wallpaper files. It only extracts useful metadata for this application.
 *
 * @author Quentin Ligier
 **/
public class MetadataExtractor {

    /**
     * The list of useful tags to extract.
     */
    private static final List<Tag> TAGS_OF_INTEREST = List.of(CustomTag.XMP_SOLAR,
        CustomTag.XMP_H24,
        CustomTag.XMP_APR,
        CustomTag.QUICKTIME_METAIMAGESIZE,
        StandardTag.IMAGE_WIDTH,
        StandardTag.IMAGE_HEIGHT
    );

    /**
     * The ExifTool interface, {@code null} when it has not been started.
     */
    @Nullable
    private ExifTool exifTool;

    /**
     * Creates a pool of ExifTool services.
     */
    public void start() {
        this.exifTool = new ExifToolBuilder()
            .enableStayOpen()
            .withPoolSize(2)
            .build();
    }

    /**
     * Clears a pool of ExifTool services.
     *
     * @throws Exception if an error occurred while closing exiftool client.
     */
    public void close() throws Exception {
        if (this.exifTool == null) {
            return;
        }
        this.exifTool.close();
    }

    /**
     * Gets a list of useful metadata of a HEIF file.
     *
     * @param imageFile The HEIF file.
     * @return a map of tag name and value.
     * @throws IOException if something bad happen during I/O operations.
     */
    public Map<Tag, String> getMetadata(final File imageFile) throws IOException {
        if (this.exifTool == null) {
            throw new RuntimeException("Exiftool is not running");
        }
        return this.exifTool.getImageMeta(imageFile, TAGS_OF_INTEREST);
    }
}
