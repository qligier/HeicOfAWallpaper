package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.FileSystemService;
import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperInterface;
import ch.qligier.heicofawallpaper.xmp.BplistReader;
import ch.qligier.heicofawallpaper.xmp.CustomTag;
import ch.qligier.heicofawallpaper.xmp.XmpExtractor;
import com.dd.plist.PropertyListFormatException;
import com.thebuzzmedia.exiftool.Tag;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Map;

/**
 * @author Quentin Ligier
 */
public class DynamicWallpaperService {

    private final XmpExtractor xmpExtractor = new XmpExtractor();

    private final BplistReader bplistReader = new BplistReader();

    public void uncompress(final File dynamicWallpaperFile) {
        final String hash = FileSystemService.sha256File(dynamicWallpaperFile);

        // imagemagick
    }

    public DynamicWallpaperInterface loadDefinition(final File dynamicWallpaperFile)
        throws IOException, PropertyListFormatException, InvalidDynamicWallpaperException, ParseException, ParserConfigurationException, SAXException {
        System.out.println(Instant.now().toEpochMilli() + ": start metadata");
        final Map<Tag, String> metadata = this.xmpExtractor.getMetadata(dynamicWallpaperFile);
        System.out.println(Instant.now().toEpochMilli() + ": end metadata");

        final int numberOfFrames = this.getNumberOfFrames(metadata);

        System.out.println(Instant.now().toEpochMilli() + ": start parsing");
        if (metadata.containsKey(CustomTag.XMP_SOLAR)) {
            return this.bplistReader.parseSolarMetadata(metadata.get(CustomTag.XMP_SOLAR), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_H24)) {
            return this.bplistReader.parseTimeBplist(metadata.get(CustomTag.XMP_H24), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_APR)) {
            return this.bplistReader.parseAppearanceBplist(metadata.get(CustomTag.XMP_APR), numberOfFrames);
        }
        throw new InvalidDynamicWallpaperException("The dynamic wallpaper has no Solar, H24 or Apr metadata");
    }

    private int getNumberOfFrames(final Map<Tag, String> metadata) throws InvalidDynamicWallpaperException {
        if (metadata.containsKey(CustomTag.QUICKTIME_METAIMAGESIZE)) {
            final String[] metaImageSizes = metadata.get(CustomTag.QUICKTIME_METAIMAGESIZE).split(" ");
            if (metaImageSizes.length % 4 != 0) {
                throw new InvalidDynamicWallpaperException("The MetaImageSize has a number of values not divisible by 4");
            }
            return metaImageSizes.length / 4 + 1; // TODO: Main image not here?
        }

        throw new InvalidDynamicWallpaperException("The MetaImageSize is missing");
    }
}
