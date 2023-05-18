package ch.qligier.heicofawallpaper.service;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.heic.BplistReader;
import ch.qligier.heicofawallpaper.heic.CustomTag;
import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperDefinition;
import com.dd.plist.PropertyListFormatException;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
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
                        final String hash) throws IOException, InterruptedException {
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
    public List<DynamicWallpaperDefinition> loadDefinitionsFromCache(final Path wallpaperCachePath) throws IOException {

        /*return Files.list(wallpaperCachePath)
            .filter(path -> path.toFile().isDirectory())
            .map(path -> path.resolve("cache.json"))
            .filter(path -> path.toFile().isFile())
            .;*/
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
     * @implSpec The caller must ensure the {@code metadataExtractor} has been started and will be closed.
     */
    public List<DynamicWallpaperDefinition> loadDefinitionsFromFile(final File dynamicWallpaperFile) throws Exception {
        assert this.metadataExtractor.isStarted();
        final Map<Tag, String> metadata = this.metadataExtractor.getMetadata(dynamicWallpaperFile);

        final short width = Short.parseShort(metadata.get(StandardTag.IMAGE_WIDTH));
        final short height = Short.parseShort(metadata.get(StandardTag.IMAGE_HEIGHT));

        final short numberOfFrames;
        if (metadata.containsKey(CustomTag.QUICKTIME_METAIMAGESIZE)) {
            final String[] metaImageSizes = metadata.get(CustomTag.QUICKTIME_METAIMAGESIZE).split(" ");
            if (metaImageSizes.length % 4 != 0) {
                throw new InvalidDynamicWallpaperException("The MetaImageSize has a number of values not divisible by 4");
            }
            numberOfFrames = (short) (metaImageSizes.length / 4);
            for (int i = 0; i < numberOfFrames; ) {
                i = i + 2;
                if (Short.parseShort(metaImageSizes[i]) != width) {
                    throw new InvalidDynamicWallpaperException("One of the frame has a different width");
                }
                ++i;
                if (Short.parseShort(metaImageSizes[i]) != height) {
                    throw new InvalidDynamicWallpaperException("One of the frame has a different height");
                }
                ++i;
            }
        } else {
            numberOfFrames = 0;
        }

        final List<DynamicWallpaperDefinition> definitions;
        final String bplist;
        if (metadata.containsKey(CustomTag.XMP_SOLAR)) {
            bplist = metadata.get(CustomTag.XMP_SOLAR);
            definitions = BplistReader.parseSolarBplist(bplist, numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_H24)) {
            bplist = metadata.get(CustomTag.XMP_H24);
            definitions = BplistReader.parseTimeBplist(bplist, numberOfFrames);
        } else if (metadata.containsKey(CustomTag.XMP_APR)) {
            bplist = metadata.get(CustomTag.XMP_APR);
            definitions = List.of(BplistReader.parseAppearanceBplist(bplist, numberOfFrames));
        } else {
            throw new InvalidDynamicWallpaperException("The dynamic wallpaper has no Solar, H24 or Apr metadata");
        }

        for (final var definition : definitions) {
            definition.setWidth(width);
            definition.setHeight(height);
            definition.setFilename(dynamicWallpaperFile.getName());
            definition.setHash(FileSystemService.sha256File(dynamicWallpaperFile));
            definition.setNumberOfFrames(numberOfFrames);
            definition.setBplist(bplist);
        }
        return definitions;
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
