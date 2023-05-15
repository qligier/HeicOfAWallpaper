package ch.qligier.heicofawallpaper.service;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.heic.BplistReader;
import ch.qligier.heicofawallpaper.heic.CustomTag;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperInterface;
import com.dd.plist.PropertyListFormatException;
import com.thebuzzmedia.exiftool.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Quentin Ligier
 */
public class DynamicWallpaperService {

    private final MetadataExtractor metadataExtractor;

    private final BplistReader bplistReader = new BplistReader();

    public DynamicWallpaperService(final MetadataExtractor metadataExtractor) {
        this.metadataExtractor = Objects.requireNonNull(metadataExtractor);
    }

    public void extract(final File dynamicWallpaperFile,
                        @Nullable String hash) throws IOException, InterruptedException {
        System.out.println("extract");
        if (hash == null) {
            hash = FileSystemService.sha256File(dynamicWallpaperFile);
        }

        final File destinationFolder = FileSystemService.getDataPath().resolve(hash).toFile();
        destinationFolder.mkdirs();

        final String[] commands = new String[6];
        commands[0] = "magick";
        commands[1] = "convert";
        commands[2] = '"' + dynamicWallpaperFile.getAbsolutePath() + '"';
        commands[3] = "-quality";
        commands[4] = "85";
        final String filenameHeic = dynamicWallpaperFile.getName();
        final String filenameJpg = filenameHeic.substring(0, filenameHeic.length() - 5) + ".jpg";
        commands[5] = '"' + destinationFolder.toPath().resolve(filenameJpg).toString() + '"';

        System.out.println(Arrays.toString(commands));

        final ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);
        final Process process = builder.start();
        final StreamGobbler streamGobbler =
            new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        System.out.println("Process exit code: " + exitCode);
    }

    public DynamicWallpaperInterface loadDefinition(final File dynamicWallpaperFile)
        throws IOException, PropertyListFormatException, InvalidDynamicWallpaperException, ParseException, ParserConfigurationException, SAXException {
        final Map<Tag, String> metadata = this.metadataExtractor.getMetadata(dynamicWallpaperFile);

        final short numberOfFrames = this.getNumberOfFrames(metadata);

        if (metadata.containsKey(CustomTag.XMP_SOLAR)) {
            return this.bplistReader.parseSolarBplist(metadata.get(CustomTag.XMP_SOLAR), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_H24)) {
            return this.bplistReader.parseTimeBplist(metadata.get(CustomTag.XMP_H24), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_APR)) {
            return this.bplistReader.parseAppearanceBplist(metadata.get(CustomTag.XMP_APR), numberOfFrames);
        }
        throw new InvalidDynamicWallpaperException("The dynamic wallpaper has no Solar, H24 or Apr metadata");
    }

    private short getNumberOfFrames(final Map<Tag, String> metadata) throws InvalidDynamicWallpaperException {
        if (metadata.containsKey(CustomTag.QUICKTIME_METAIMAGESIZE)) {
            final String[] metaImageSizes = metadata.get(CustomTag.QUICKTIME_METAIMAGESIZE).split(" ");
            if (metaImageSizes.length % 4 != 0) {
                throw new InvalidDynamicWallpaperException("The MetaImageSize has a number of values not divisible by 4");
            }
            return (short) (metaImageSizes.length / 4 + 1); // TODO: Main image not here?
        }

        throw new InvalidDynamicWallpaperException("The MetaImageSize is missing");
    }

    private record StreamGobbler(InputStream inputStream, Consumer<String> consumer) implements Runnable {

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .forEach(consumer);
        }
    }
}
