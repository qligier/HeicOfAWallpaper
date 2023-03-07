package ch.qligier.heicofawallpaper.xmp;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class XmpExtractor {

    /**
     * The ExifTool interface.
     */
    private final ExifTool exifTool;

    /**
     * Constructor. It builds the default ExifTool interface, the executable must be in the path.
     */
    public XmpExtractor() {
        this.exifTool = new ExifToolBuilder()
            .enableStayOpen()
            .build();
    }

    public Map<Tag, String> getMetadata(final File imageFile) throws IOException {
        return this.exifTool.getImageMeta(imageFile, List.of(CustomTag.XMP_SOLAR,
                                                             CustomTag.XMP_H24,
                                                             CustomTag.XMP_APR,
                                                             CustomTag.QUICKTIME_METAIMAGESIZE,
                                                             StandardTag.IMAGE_WIDTH,
                                                             StandardTag.IMAGE_HEIGHT));
    }
}
