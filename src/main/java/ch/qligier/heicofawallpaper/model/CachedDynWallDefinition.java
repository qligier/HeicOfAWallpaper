package ch.qligier.heicofawallpaper.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * @author Quentin Ligier
 */
public record CachedDynWallDefinition(short height,
                                      short width,
                                      String filename,
                                      String bplist,
                                      short numberOfFrames,
                                      @Nullable PhaseAppearance appearancePhase,
                                      @Nullable List<PhaseSolar> solarPhases,
                                      @Nullable List<PhaseTime> timePhases) {

    public static CachedDynWallDefinition fromDynamicWallpaperDefinition(final DynWallDefinition definition) {
        return new CachedDynWallDefinition(definition.height(),
                                           definition.width(),
                                           definition.filename(),
                                           definition.fileHash(),
                                           definition.numberOfFrames(),
                                           definition.appearancePhase(),
                                           definition.solarPhases(),
                                           definition.timePhases());
    }

    public DynWallDefinition toDynamicWallpaperDefinition(final String hash) {
        return new DynWallDefinition(this.height,
                                     this.width,
                                     hash,
                                     this.filename,
                                     this.bplist,
                                     this.numberOfFrames,
                                     this.appearancePhase,
                                     this.solarPhases,
                                     this.timePhases);
    }
}
