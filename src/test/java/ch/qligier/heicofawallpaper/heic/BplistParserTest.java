package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.model.PhaseAppearance;
import ch.qligier.heicofawallpaper.model.PhaseSolar;
import ch.qligier.heicofawallpaper.model.PhaseTime;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the {@link BplistParser} class.
 *
 * @author Quentin Ligier
 **/
class BplistParserTest {

    @Test
    void parseAppearanceBplist() {
        final PhaseAppearance appearance = BplistParser.parseAppearanceBplist(
            "YnBsaXN0MDDSAQIDBFFkUWwQARAACA0PERMAAAAAAAABAQAAAAAAAAAFAAAAAAAAAAAAAAAAAAAAFQ==",
            (short) 2);

        assertNotNull(appearance);
        assertEquals(0, appearance.lightFrameIndex());
        assertEquals(1, appearance.darkFrameIndex());
    }

    @Test
    void parseTimeBplist() {
        final List<PhaseTime> time = BplistParser.parseTimeBplist(
            "YnBsaXN0MDDSAQIDD1J0aVJhcKMECQzSBQYHCFF0UWkjP9AAAAAAAAAQANIFBgoLIwAAAAAAAAAAEAHSBQYNDiM/6AAAAAAAABAC0hARDghRZFFsCA0QExccHiApKzA5O0BJS1BSAAAAAAAAAQEAAAAAAAAAEgAAAAAAAAAAAAAAAAAAAFQ=",
            (short) 3);

        assertNotNull(time);
        assertEquals(3, time.size());
        assertEquals(1, time.get(0).frameIndex());
        assertEquals(LocalTime.of(0, 0), time.get(0).time());
        assertEquals(0, time.get(1).frameIndex());
        assertEquals(LocalTime.of(6, 0), time.get(1).time());
        assertEquals(2, time.get(2).frameIndex());
        assertEquals(LocalTime.of(18, 0), time.get(2).time());
    }

    @Test
    void parseSolarMetadata() {
        final List<PhaseSolar> solar = BplistParser.parseSolarBplist(
            "YnBsaXN0MDDSAQIDCFJhcFJzadIEBQYHUWxRZBAAEAGqCQ8SFhkdISMmKdMKCwwNDgZRYVF6UWkjAAAAAAAAAAAjQHDgAAAAAADTCgsMEBEHI8A5AAAAAAAAI0BRgAAAAAAA0woLDBMUFSPAIgAAAAAAACNAVAAAAAAAABAC0woLDA0XGCNAVoAAAAAAABAD0woLDBobHCNAJAAAAAAAACNAWQAAAAAAABAE0woLDB4fICNAOQAAAAAAACNAW4AAAAAAABAF0woLDB4iICNAb0AAAAAAANMKCwwaJCUjQHBAAAAAAAAQBtMKCwwTJygjQHGAAAAAAAAQB9MKCwwQKgcjQHIgAAAAAAAACAANABAAEwAYABoAHAAeACAAKwAyADQANgA4AEEASgBRAFoAYwBqAHMAfAB+AIUAjgCQAJcAoACpAKsAsgC7AMQAxgDNANYA3QDmAOgA7wD4APoBAQAAAAAAAAIBAAAAAAAAACsAAAAAAAAAAAAAAAAAAAEK",
            (short) 8);

        assertNotNull(solar);
        assertEquals(10, solar.size());
        assertEquals(0, solar.get(0).frameIndex());
        assertEquals(0.0, solar.get(0).elevation());
        assertEquals(270.0, solar.get(0).azimuth());
        assertEquals(1, solar.get(9).frameIndex());
        assertEquals(-25.0, solar.get(9).elevation());
        assertEquals(290.0, solar.get(9).azimuth());
    }

    @Test
    void parseTimePercent() throws InvalidDynamicWallpaperException {
        assertEquals(LocalTime.of(0, 0), BplistParser.parseTimePercent(0));
        assertEquals(LocalTime.of(0, 0), BplistParser.parseTimePercent(1));
        assertEquals(LocalTime.of(12, 0), BplistParser.parseTimePercent(0.5));
        assertEquals(LocalTime.of(6, 0), BplistParser.parseTimePercent(0.25));
        assertEquals(LocalTime.of(18, 0), BplistParser.parseTimePercent(0.75));
        assertEquals(LocalTime.of(8, 0), BplistParser.parseTimePercent(0.33333));
        assertEquals(LocalTime.of(0, 30), BplistParser.parseTimePercent(0.02083));
        assertThrows(InvalidDynamicWallpaperException.class, () -> BplistParser.parseTimePercent(-0.2));
        assertThrows(InvalidDynamicWallpaperException.class, () -> BplistParser.parseTimePercent(1.1));
    }
}
