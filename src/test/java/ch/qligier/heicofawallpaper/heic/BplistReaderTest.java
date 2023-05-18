package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.model.AppearanceWallpaperDefinition;
import ch.qligier.heicofawallpaper.model.SolarWallpaperDefinition;
import ch.qligier.heicofawallpaper.model.TimeWallpaperDefinition;
import com.dd.plist.PropertyListFormatException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests of the {@link BplistReader} class.
 *
 * @author Quentin Ligier
 **/
class BplistReaderTest {

    private final BplistReader reader = new BplistReader();

    @Test
    void parseAppearanceBplist()
        throws PropertyListFormatException, InvalidDynamicWallpaperException, IOException, ParseException, ParserConfigurationException, SAXException {
        final AppearanceWallpaperDefinition appearance = this.reader.parseAppearanceBplist(
            "YnBsaXN0MDDSAQIDBFFkUWwQARAACA0PERMAAAAAAAABAQAAAAAAAAAFAAAAAAAAAAAAAAAAAAAAFQ==",
            (short) 2);

        assertEquals(2, appearance.numberOfPhases());
        assertEquals(0, appearance.getLightFrameIndex());
        assertEquals(1, appearance.getDarkFrameIndex());
        assertEquals(2, appearance.numberOfFrames());
    }

    @Test
    void parseTimeBplist()
        throws Exception {
        final var definitions = this.reader.parseTimeBplist(
            "YnBsaXN0MDDSAQIDD1J0aVJhcKMECQzSBQYHCFF0UWkjP9AAAAAAAAAQANIFBgoLIwAAAAAAAAAAEAHSBQYNDiM/6AAAAAAAABAC0hARDghRZFFsCA0QExccHiApKzA5O0BJS1BSAAAAAAAAAQEAAAAAAAAAEgAAAAAAAAAAAAAAAAAAAFQ=",
            (short) 3);

        final TimeWallpaperDefinition time = (TimeWallpaperDefinition) definitions.get(0);
        assertEquals(3, time.numberOfPhases());
        assertEquals(1, time.getPhases().get(0).frameIndex());
        assertEquals(LocalTime.of(0, 0), time.getPhases().get(0).time());
        assertEquals(0, time.getPhases().get(1).frameIndex());
        assertEquals(LocalTime.of(6, 0), time.getPhases().get(1).time());
        assertEquals(2, time.getPhases().get(2).frameIndex());
        assertEquals(LocalTime.of(18, 0), time.getPhases().get(2).time());

        final AppearanceWallpaperDefinition appearance = (AppearanceWallpaperDefinition) definitions.get(1);
        assertEquals(2, appearance.numberOfPhases());
        assertEquals(0, appearance.getLightFrameIndex());
        assertEquals(2, appearance.getDarkFrameIndex());
    }

    @Test
    void parseSolarMetadata()
        throws Exception {
        final var definitions = this.reader.parseSolarBplist(
            "YnBsaXN0MDDSAQIDCFJhcFJzadIEBQYHUWxRZBAAEAGqCQ8SFhkdISMmKdMKCwwNDgZRYVF6UWkjAAAAAAAAAAAjQHDgAAAAAADTCgsMEBEHI8A5AAAAAAAAI0BRgAAAAAAA0woLDBMUFSPAIgAAAAAAACNAVAAAAAAAABAC0woLDA0XGCNAVoAAAAAAABAD0woLDBobHCNAJAAAAAAAACNAWQAAAAAAABAE0woLDB4fICNAOQAAAAAAACNAW4AAAAAAABAF0woLDB4iICNAb0AAAAAAANMKCwwaJCUjQHBAAAAAAAAQBtMKCwwTJygjQHGAAAAAAAAQB9MKCwwQKgcjQHIgAAAAAAAACAANABAAEwAYABoAHAAeACAAKwAyADQANgA4AEEASgBRAFoAYwBqAHMAfAB+AIUAjgCQAJcAoACpAKsAsgC7AMQAxgDNANYA3QDmAOgA7wD4APoBAQAAAAAAAAIBAAAAAAAAACsAAAAAAAAAAAAAAAAAAAEK",
            (short) 8);

        final SolarWallpaperDefinition solar = (SolarWallpaperDefinition) definitions.get(0);
        assertEquals(10, solar.numberOfPhases());
        assertEquals(0, solar.getPhases().get(0).frameIndex());
        assertEquals(0.0, solar.getPhases().get(0).elevation());
        assertEquals(270.0, solar.getPhases().get(0).azimuth());
        assertEquals(1, solar.getPhases().get(9).frameIndex());
        assertEquals(-25.0, solar.getPhases().get(9).elevation());
        assertEquals(290.0, solar.getPhases().get(9).azimuth());

        final AppearanceWallpaperDefinition appearance = (AppearanceWallpaperDefinition) definitions.get(1);
        assertEquals(2, appearance.numberOfPhases());
        assertEquals(0, appearance.getLightFrameIndex());
        assertEquals(1, appearance.getDarkFrameIndex());
    }

    @Test
    void parseTimePercent() throws InvalidDynamicWallpaperException {
        assertEquals(LocalTime.of(0, 0), this.reader.parseTimePercent(0));
        assertEquals(LocalTime.of(0, 0), this.reader.parseTimePercent(1));
        assertEquals(LocalTime.of(12, 0), this.reader.parseTimePercent(0.5));
        assertEquals(LocalTime.of(6, 0), this.reader.parseTimePercent(0.25));
        assertEquals(LocalTime.of(18, 0), this.reader.parseTimePercent(0.75));
        assertEquals(LocalTime.of(8, 0), this.reader.parseTimePercent(0.33333));
        assertEquals(LocalTime.of(0, 30), this.reader.parseTimePercent(0.02083));
        assertThrows(InvalidDynamicWallpaperException.class, () -> this.reader.parseTimePercent(-0.2));
        assertThrows(InvalidDynamicWallpaperException.class, () -> this.reader.parseTimePercent(1.1));
    }
}
