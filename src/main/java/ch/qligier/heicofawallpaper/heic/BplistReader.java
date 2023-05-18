package ch.qligier.heicofawallpaper.heic;

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
 * A reader of bplists (binary property lists) for dynamic wallpapers.
 *
 * @author Quentin Ligier
 **/
public class BplistReader {
    private static final String THEME_LIGHT_FRAME_INDEX = "l";
    private static final String THEME_DARK_FRAME_INDEX = "d";
    private static final String TIME_PHASES = "ti";
    private static final String SOLAR_PHASES = "si";
    private static final String APPEARANCE_PHASES = "ap";
    private static final String FRAME_INDEX = "i";
    private static final String TIME = "t";
    private static final String ELEVATION = "a";
    private static final String AZIMUTH = "z";
    private static final int SECONDS_IN_DAY = 86_400;

    public AppearanceWallpaperDefinition parseAppearanceBplist(final String base64AppearanceBplist,
                                                               final short numberOfFrames)
        throws PropertyListFormatException, IOException, ParseException, ParserConfigurationException, SAXException, InvalidDynamicWallpaperException {
        final HashMap<String, Object> root = this.parseBase64Bplist(base64AppearanceBplist);

        return this.parseAppearanceDictionary(root);
    }

    public List<DynamicWallpaperDefinition> parseTimeBplist(final String base64TimeBplist,
                                                            final short numberOfFrames) throws Exception {
        final List<DynamicWallpaperDefinition> definitions = new ArrayList<>(2);
        final HashMap<String, Object> root = this.parseBase64Bplist(base64TimeBplist);

        // Extract time phases
        final List<HashMap<String, Object>> phases = this.getDictionaryArrayValue(root, TIME_PHASES);
        final List<PhaseTime> parsedPhases = new ArrayList<>(phases.size());
        for (final HashMap<String, Object> phase : phases) {
            final short frameIndex = this.getShortValue(phase, FRAME_INDEX);
            this.checkFrameIndex(numberOfFrames, frameIndex);

            parsedPhases.add(new PhaseTime(frameIndex,
                                           this.parseTimePercent(this.getFloatValue(phase, TIME))));
        }
        definitions.add(new TimeWallpaperDefinition(parsedPhases));

        // Extract appearance phases
        if (root.containsKey(APPEARANCE_PHASES)) {
            final HashMap<String, Object> appearancePhases = this.getDictionaryValue(root, APPEARANCE_PHASES);
            definitions.add(this.parseAppearanceDictionary(appearancePhases));
        }

        return definitions;
    }

    public List<DynamicWallpaperDefinition> parseSolarBplist(final String base64SolarBplist,
                                                             final short numberOfFrames) throws Exception {
        final List<DynamicWallpaperDefinition> definitions = new ArrayList<>(2);
        final HashMap<String, Object> root = this.parseBase64Bplist(base64SolarBplist);

        // Extract solar phases
        final List<HashMap<String, Object>> phases = getDictionaryArrayValue(root, SOLAR_PHASES);
        final List<PhaseSolar> parsedPhases = new ArrayList<>(phases.size());
        for (final HashMap<String, Object> phase : phases) {
            final short frameIndex = this.getShortValue(phase, FRAME_INDEX);
            this.checkFrameIndex(numberOfFrames, frameIndex);
            final float elevation = this.getFloatValue(phase, ELEVATION);
            final float azimuth = this.getFloatValue(phase, AZIMUTH);
            parsedPhases.add(new PhaseSolar(frameIndex, elevation, azimuth));
        }
        definitions.add(new SolarWallpaperDefinition(parsedPhases));

        // Extract appearance phases
        if (root.containsKey(APPEARANCE_PHASES)) {
            final HashMap<String, Object> appearancePhases = this.getDictionaryValue(root, APPEARANCE_PHASES);
            definitions.add(this.parseAppearanceDictionary(appearancePhases));
        }

        return definitions;
    }

    protected AppearanceWallpaperDefinition parseAppearanceDictionary(final HashMap<String, Object> dictionary)
        throws InvalidDynamicWallpaperException {
        final short lightFrameIndex = this.getShortValue(dictionary, THEME_LIGHT_FRAME_INDEX);
        //this.checkFrameIndex(numberOfFrames, lightFrameIndex);
        final short darkFrameIndex = this.getShortValue(dictionary, THEME_DARK_FRAME_INDEX);
        //this.checkFrameIndex(numberOfFrames, darkFrameIndex);
        return new AppearanceWallpaperDefinition(lightFrameIndex, darkFrameIndex);
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

    private short getShortValue(final HashMap<String, Object> dictionary,
                                final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Integer integer) {
            return integer.shortValue();
        }
        throw new InvalidDynamicWallpaperException(String.format("The key '%s' is not an integer", key));
    }

    private float getFloatValue(final HashMap<String, Object> dictionary,
                                final String key) throws InvalidDynamicWallpaperException {
        if (!dictionary.containsKey(key)) {
            throw new InvalidDynamicWallpaperException(String.format("The key '%s' is missing", key));
        }
        final Object value = dictionary.get(key);
        if (value instanceof final Double doublee) {
            return doublee.floatValue();
        } else if (value instanceof final Integer integer) {
            return integer.floatValue();
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
