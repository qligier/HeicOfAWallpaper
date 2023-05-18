package ch.qligier.heicofawallpaper.model;

import ch.qligier.heicofawallpaper.exception.InvalidDynamicWallpaperException;
import ch.qligier.heicofawallpaper.heic.BplistReader;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author Quentin Ligier
 */
public class CachedWallpaperDefinition {

    private String filename;

    private short height;

    private short width;

    private @Nullable String solarBplist;

    private @Nullable String timeBplist;

    private @Nullable String appearanceBplist;

    private short numberOfFrames;

    public static CachedWallpaperDefinition fromDynamicWallpaperDefinition(final DynamicWallpaperDefinition definition) {
        final CachedWallpaperDefinition cached = new CachedWallpaperDefinition();
        cached.setFilename(definition.filename());
        cached.setHeight(definition.height());
        cached.setWidth(definition.width());
        cached.setNumberOfFrames(definition.numberOfFrames());
        switch (definition.type()) {
            case SOLAR -> cached.setSolarBplist(definition.bplist());
            case TIME -> cached.setTimeBplist(definition.bplist());
            case APPEARANCE -> cached.setAppearanceBplist(definition.bplist());
        }
        return cached;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public short getHeight() {
        return this.height;
    }

    public void setHeight(final short height) {
        this.height = height;
    }

    public short getWidth() {
        return this.width;
    }

    public void setWidth(final short width) {
        this.width = width;
    }

    public @Nullable String getSolarBplist() {
        return this.solarBplist;
    }

    public void setSolarBplist(final @Nullable String solarBplist) {
        this.solarBplist = solarBplist;
    }

    public @Nullable String getTimeBplist() {
        return this.timeBplist;
    }

    public void setTimeBplist(final @Nullable String timeBplist) {
        this.timeBplist = timeBplist;
    }

    public @Nullable String getAppearanceBplist() {
        return this.appearanceBplist;
    }

    public void setAppearanceBplist(final @Nullable String appearanceBplist) {
        this.appearanceBplist = appearanceBplist;
    }

    public short getNumberOfFrames() {
        return this.numberOfFrames;
    }

    public void setNumberOfFrames(final short numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public List<DynamicWallpaperDefinition> toDynamicWallpaperDefinitions(final String hash) throws Exception {
        final List<DynamicWallpaperDefinition> definitions;
        if (this.solarBplist != null) {
            definitions = BplistReader.parseSolarBplist(this.solarBplist, this.numberOfFrames);
        } else if (this.timeBplist != null) {
            definitions = BplistReader.parseTimeBplist(this.timeBplist, this.numberOfFrames);
        } else if (this.appearanceBplist != null) {
            definitions = List.of(BplistReader.parseAppearanceBplist(this.appearanceBplist, this.numberOfFrames));
        } else {
            throw new InvalidDynamicWallpaperException("");
        }
        
        for (final var definition : definitions) {
            definition.setNumberOfFrames(this.numberOfFrames);
            definition.setHash(hash);
            definition.setFilename(this.filename);
            definition.setHeight(this.height);
            definition.setWidth(this.width);
        }
        return definitions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final CachedWallpaperDefinition that)) return false;
        return height == that.height
            && width == that.width
            && numberOfFrames == that.numberOfFrames
            && filename.equals(that.filename)
            && solarBplist.equals(that.solarBplist)
            && timeBplist.equals(that.timeBplist)
            && appearanceBplist.equals(that.appearanceBplist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, height, width, solarBplist, timeBplist, appearanceBplist, numberOfFrames);
    }

    @Override
    public String toString() {
        return "CachedWallpaperDefinition{" +
            "filename='" + filename + '\'' +
            ", height=" + height +
            ", width=" + width +
            ", solarBplist='" + solarBplist + '\'' +
            ", timeBplist='" + timeBplist + '\'' +
            ", appearanceBplist='" + appearanceBplist + '\'' +
            ", numberOfFrames=" + numberOfFrames +
            '}';
    }
}
