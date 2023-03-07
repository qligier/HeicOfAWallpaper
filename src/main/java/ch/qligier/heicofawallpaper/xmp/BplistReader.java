package ch.qligier.heicofawallpaper.xmp;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.model.*;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class BplistReader {
    private static final String THEME_LIGHT_FRAME_INDEX = "l";
    private static final String THEME_DARK_FRAME_INDEX = "d";
    private static final String TIME_PHASES = "ti";
    private static final String SOLAR_PHASES = "si";
    private static final String FRAME_INDEX = "i";
    private static final String TIME = "t";
    private static final String ELEVATION = "a";
    private static final String AZIMUTH = "z";
    private static final int SECONDS_IN_DAY = 86_400;

    public AppearanceDynamicWallpaper parseAppearanceBplist(final String base64AppearanceBplist,
                                                            final int numberOfFrames)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException, InvalidDynamicWallpaperException {
        final HashMap<String, Object> root = this.parseBase64Bplist(base64AppearanceBplist);

        final int lightFrameIndex = this.getIntValue(root, THEME_LIGHT_FRAME_INDEX);
        this.checkFrameIndex(numberOfFrames, lightFrameIndex);
        final int darkFrameIndex = this.getIntValue(root, THEME_DARK_FRAME_INDEX);
        this.checkFrameIndex(numberOfFrames, darkFrameIndex);
        return new AppearanceDynamicWallpaper(numberOfFrames, lightFrameIndex, darkFrameIndex);
    }

    public TimeDynamicWallpaper parseTimeBplist(final String base64TimeBplist,
                                                final int numberOfFrames)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException, InvalidDynamicWallpaperException {
        final HashMap<String, Object> root = this.parseBase64Bplist(base64TimeBplist);

        final List<HashMap<String, Object>> phases = this.getDictionaryArrayValue(root, TIME_PHASES);
        final List<TimeDynamicWallpaperPhase> parsedPhases = new ArrayList<>(phases.size());
        for (final HashMap<String, Object> phase : phases) {
            final int frameIndex = this.getIntValue(phase, FRAME_INDEX);
            this.checkFrameIndex(numberOfFrames, frameIndex);

            parsedPhases.add(new TimeDynamicWallpaperPhase(frameIndex,
                                                           this.parseTimePercent(this.getDoubleValue(phase, TIME))));
        }

        return new TimeDynamicWallpaper(numberOfFrames, parsedPhases);
    }

    public SolarDynamicWallpaper parseSolarMetadata(final String base64SolarBplist,
                                                    final int numberOfFrames)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException, InvalidDynamicWallpaperException {
        final HashMap<String, Object> root = this.parseBase64Bplist(base64SolarBplist);

        final List<HashMap<String, Object>> phases = getDictionaryArrayValue(root, SOLAR_PHASES);
        final List<SolarDynamicWallpaperPhase> parsedPhases = new ArrayList<>(phases.size());

        for (final HashMap<String, Object> phase : phases) {
            final int frameIndex = this.getIntValue(phase, FRAME_INDEX);
            this.checkFrameIndex(numberOfFrames, frameIndex);
            final double elevation = this.getDoubleValue(phase, ELEVATION);
            final double azimuth = this.getDoubleValue(phase, AZIMUTH);
            parsedPhases.add(new SolarDynamicWallpaperPhase(frameIndex, elevation, azimuth));
        }

        return new SolarDynamicWallpaper(numberOfFrames, parsedPhases);
    }

    private HashMap<String, Object> parseBase64Bplist(final String base64Bplist)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException {
        final byte[] bplist = Base64.getDecoder().decode(base64Bplist);
        return (HashMap<String, Object>) PropertyListParser.parse(bplist).toJavaObject();
    }

    private HashMap<String, Object> getDictionaryValue(final HashMap<String, Object> dictionary,
                                                       final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object entry = dictionary.get(key);
        if (entry instanceof HashMap) {
            return (HashMap<String, Object>) entry;
        }
        throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not a dictionary", key));
    }

    private List<HashMap<String, Object>> getDictionaryArrayValue(final HashMap<String, Object> dictionary,
                                                                  final String key)
        throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object entry = dictionary.get(key);
        if (!(entry instanceof final Object[] values)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not an array", key));
        }
        final List<HashMap<String, Object>> result = new ArrayList<>(values.length);
        for (final Object element : values) {
            if (element instanceof final HashMap hashMap) {
                result.add(hashMap);
            } else {
                throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not an array of dictionary",
                                                                         key));
            }
        }
        return result;
    }

    protected LocalTime parseTimePercent(final double timePercent) throws InvalidDynamicWallpaperException {
        if (timePercent < 0 || timePercent > 1) {
            throw new InvalidDynamicWallpaperException(String.format("The timePercent '%f' is invalid", timePercent));
        }
        if (timePercent == 1) {
            return LocalTime.of(0, 0);
        }
        return LocalTime.ofSecondOfDay(Math.round(SECONDS_IN_DAY * timePercent));
    }

    private int getIntValue(final HashMap<String, Object> dictionary,
                            final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Integer integer) {
            return integer;
        }
        throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not an integer", key));
    }

    private double getDoubleValue(final HashMap<String, Object> dictionary,
                                  final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Double doublee) {
            return doublee;
        } else if (value instanceof final Integer integer) {
            return (double) integer;
        }
        throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not a float", key));
    }

    private void checkFrameIndex(final int numberOfFrames,
                                 final int frameIndex) throws InvalidDynamicWallpaperException {
        if (frameIndex >= numberOfFrames) {
            throw new InvalidDynamicWallpaperException(String.format("The frame index '%d' is invalid, %d frames " +
                                                                         "found", frameIndex, numberOfFrames));
        }
    }
}
