package ch.qligier.heicofawallpaper.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The interface of a dynamic wallpaper definition.
 *
 * @param height          The wallpaper image height.
 * @param width           The wallpaper image width.
 * @param fileHash        The wallpaper file hash.
 * @param filename        The wallpaper filename.
 * @param bplist          The wallpaper bplist metadata, base64-encoded.
 * @param numberOfFrames  The number of frames in the wallpaper (not to be mistaken with the number of phases).
 * @param appearancePhase The appearance phase, or {@code null}.
 * @param solarPhases     The list of solar phases, or {@code null}.
 * @param timePhases      The list of time phases, or {@code null}.
 * @author Quentin Ligier
 */
public record DynWallDefinition(short height,
                                short width,
                                String fileHash,
                                String filename,
                                String bplist,
                                short numberOfFrames,
                                @Nullable PhaseAppearance appearancePhase,
                                @Nullable List<PhaseSolar> solarPhases,
                                @Nullable List<PhaseTime> timePhases) {

    public DynWallDefinition(final short height,
                             final short width,
                             final String fileHash,
                             final String filename,
                             final String bplist,
                             final short numberOfFrames,
                             @Nullable final PhaseAppearance appearancePhase,
                             @Nullable final List<PhaseSolar> solarPhases,
                             @Nullable final List<PhaseTime> timePhases) {
        this.height = height;
        this.width = width;
        this.fileHash = Objects.requireNonNull(fileHash);
        this.filename = Objects.requireNonNull(filename);
        this.bplist = Objects.requireNonNull(bplist);
        this.numberOfFrames = numberOfFrames;
        this.appearancePhase = appearancePhase;

        if (solarPhases != null) {
            if (solarPhases.isEmpty()) {
                throw new RuntimeException("The solar wallpaper has no phase, at least one expected");
            }
            // The phases are sorted by ???, to facilitate search
            this.solarPhases = Collections.unmodifiableList(solarPhases);
        } else {
            this.solarPhases = null;
        }

        if (timePhases != null) {
            if (timePhases.isEmpty()) {
                throw new RuntimeException("The time wallpaper has no phase, at least one expected");
            }
            // The phases are sorted by increasing time, to facilitate search
            timePhases.sort(Comparator.comparing(PhaseTime::time));
            this.timePhases = Collections.unmodifiableList(timePhases);
        } else {
            this.timePhases = null;
        }
    }

    public boolean isAppearance() {
        return this.appearancePhase != null;
    }

    public boolean isSolar() {
        return this.solarPhases != null;
    }

    public boolean isTime() {
        return this.timePhases != null;
    }
}
