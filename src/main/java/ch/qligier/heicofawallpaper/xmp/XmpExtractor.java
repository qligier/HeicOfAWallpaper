package ch.qligier.heicofawallpaper.xmp;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.heic.SolarWallpaperMetadata;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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
            .build();
    }

    public Map<Tag, String> getMetadata(final File imageFile) throws IOException {
        return this.exifTool.getImageMeta(imageFile, List.of(CustomTag.XMP_SOLAR,
                                                             CustomTag.XMP_H24,
                                                             StandardTag.IMAGE_WIDTH,
                                                             StandardTag.IMAGE_HEIGHT));
    }

    public void parseSolarMetadata(final String solar)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException, InvalidDynamicWallpaperException {
        final byte[] bplist = Base64.getDecoder().decode(solar);
        final HashMap<String, Object> root =
            (HashMap<String, Object>) PropertyListParser.parse(bplist).toJavaObject(HashMap.class);

        if (!root.containsKey("ap") || !root.containsKey("si")) {
            throw new InvalidDynamicWallpaperException("The solar metadata is missing the 'ap' or 'si' keys");
        }
        final List<HashMap<String, Object>> si = getDictionaryArrayForKey(root, "si");
        final List<SolarWallpaperMetadata.SolarFrameMetadata> frames = new ArrayList<>();
        int expectedIndex = 0;

        for (final HashMap<String, Object> entry : si) {
            /*if (entryDict.containsKey("i") && entryDict.get("i")) {
                throw new InvalidDynamicWallpaperException("The index");
            }*/
            frames.add(new SolarWallpaperMetadata.SolarFrameMetadata());
        }
    }

    private HashMap<String, Object> getDictionaryForKey(final HashMap<String, Object> dictionary,
                                                        final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException("");
        }
        final Object entry = dictionary.get(key);
        if (entry instanceof HashMap) {
            return (HashMap<String, Object>) entry;
        }
        throw new InvalidDynamicWallpaperException("");
    }

    private List<HashMap<String, Object>> getDictionaryArrayForKey(final HashMap<String, Object> dictionary,
                                                                   final String key)
        throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException("");
        }
        final Object entry = dictionary.get(key);
        if (!(entry instanceof List)) {
            throw new InvalidDynamicWallpaperException("");
        }
        final List<Object> list = (List<Object>) entry;
        final List<HashMap<String, Object>> result = new ArrayList<>(list.size());
        for (final Object element : list) {
            if (element instanceof final HashMap hashMap) {
                result.add(hashMap);
            }
            throw new InvalidDynamicWallpaperException("");
        }
        return result;
    }

    private int getIntForKey(final HashMap<String, Object> dictionary,
                             final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException("");
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Integer integer) {
            return integer;
        }
        throw new InvalidDynamicWallpaperException("");
    }

    private float getFloatForKey(final HashMap<String, Object> dictionary,
                                 final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException("");
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Float floatt) {
            return floatt;
        }
        throw new InvalidDynamicWallpaperException("");
    }
}
