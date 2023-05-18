package ch.qligier.heicofawallpaper.model;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * The interface of a dynamic wallpaper definition.
 *
 * @author Quentin Ligier
 */
public abstract class DynamicWallpaperDefinition {

    /**
     * The wallpaper image height;
     */
    protected short height;

    /**
     * The wallpaper image width;
     */
    protected short width;

    /**
     * The wallpaper file hash.
     */
    @MonotonicNonNull
    protected String fileHash = null;

    /**
     * The wallpaper filename.
     */
    @MonotonicNonNull
    protected String filename = null;

    /**
     * The wallpaper bplist metadata, base64-encoded.
     */
    @MonotonicNonNull
    protected String bplist = null;
    /**
     * The number of frames in the wallpaper (not to be mistaken with the number of phases).
     */
    protected short numberOfFrames;

    /**
     * Returns the identifier of this dynamic wallpaper definition.
     */
    public String identifier() {
        return String.format("%s-%s", this.fileHash, this.type().name());
    }

    /**
     * Returns the number of phases in the dynamic wallpaper. A phase is a mapping of a frame to conditions to show the
     * frame. Multiple phases may use the same frame, and some frames may not be used by any phase.
     */
    public abstract short numberOfPhases();

    /**
     * Returns the index of the frame to show, given the current environment.
     *
     * @param currentEnvironment The current environment.
     * @return the index of the frame to show.
     */
    public abstract short currentFrame(final CurrentEnvironment currentEnvironment);

    /**
     * Returns the type of the dynamic wallpaper.
     */
    public abstract DynamicWallpaperType type();

    /**
     * Returns the wallpaper height.
     */
    public short height() {
        return this.height;
    }

    /**
     * Returns the wallpaper width.
     */
    public short width() {
        return this.width;
    }

    /**
     * Returns the wallpaper file hash.
     */
    public String fileHash() {
        return this.fileHash;
    }

    /**
     * Returns the wallpaper file name.
     */
    public String filename() {
        return this.filename;
    }

    /**
     * Returns the wallpaper bplist metadata.
     */
    public String bplist() {
        return this.bplist;
    }

    /**
     * Returns the number of frames (different images) in the dynamic wallpaper.
     */
    public short numberOfFrames() {
        return this.numberOfFrames;
    }

    public void setHeight(final short height) {
        this.height = height;
    }

    public void setWidth(final short width) {
        this.width = width;
    }

    public void setHash(final String hash) {
        this.fileHash = hash;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public void setNumberOfFrames(final short numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public void setBplist(final String bplist) {
        this.bplist = bplist;
    }
}
