package ch.qligier.heicofawallpaper.service;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.heic.BplistReader;
import ch.qligier.heicofawallpaper.heic.CustomTag;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperDefinition;
import com.dd.plist.PropertyListFormatException;
import com.thebuzzmedia.exiftool.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
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
        commands[5] = '"' + destinationFolder.toPath().resolve("frame.jpg").toString() + '"';

        System.out.println(Arrays.toString(commands));

        final ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);
        final Process process = builder.start();
        final StreamConsumer streamConsumer =
            new StreamConsumer(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamConsumer);
        int exitCode = process.waitFor();
        System.out.println("Process exit code: " + exitCode);
    }

    /**
     * This method loads the definition(s) of a dynamic wallpaper from its cached content.
     *
     * @param wallpaperCachePath
     * @return
     */
    public List<DynamicWallpaperDefinition> loadDefinitionsFromCache(final Path wallpaperCachePath) {
        return null;
    }

    /**
     * This method loads the definition(s) of a dynamic wallpaper from its original HEIC file. It is slow and should be
     * done the first time only. Afterwards, definitions should be loaded from the created cache (see
     * {@link #loadDefinitionsFromCache(Path)}.
     *
     * @param dynamicWallpaperFile
     * @return
     * @throws IOException
     * @throws PropertyListFormatException
     * @throws InvalidDynamicWallpaperException
     * @throws ParseException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public List<DynamicWallpaperDefinition> loadDefinitionsFromFile(final File dynamicWallpaperFile) throws Exception {
        final Map<Tag, String> metadata = this.metadataExtractor.getMetadata(dynamicWallpaperFile);

        final short numberOfFrames = this.getNumberOfFrames(metadata);

        if (metadata.containsKey(CustomTag.XMP_SOLAR)) {
            return this.bplistReader.parseSolarBplist(metadata.get(CustomTag.XMP_SOLAR), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_H24)) {
            return this.bplistReader.parseTimeBplist(metadata.get(CustomTag.XMP_H24), numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_APR)) {
            return List.of(this.bplistReader.parseAppearanceBplist(metadata.get(CustomTag.XMP_APR), numberOfFrames));
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

    /**
     * A runnable that streams an {@link InputStream} into a consumer.
     *
     * @param inputStream
     * @param consumer
     */
    private record StreamConsumer(InputStream inputStream, Consumer<String> consumer) implements Runnable {

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(this.inputStream))
                .lines()
                .forEach(this.consumer);
        }
    }
}
