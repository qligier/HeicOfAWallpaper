package ch.qligier.heicofawallpaper.utils.heic;

import com.thebuzzmedia.exiftool.Tag;

/**
 * Custom tags used by exiftool to access metadata.
 *
 * @author Quentin Ligier
 **/
public enum CustomTag implements Tag {

    /**
     * The key "apple_desktop:solar", the content is a string of base64-encoded binary content.
     */
    XMP_SOLAR("Solar"),

    /**
     * The key "apple_desktop:h24", the content is a string of base64-encoded binary content.
     */
    XMP_H24("H24"),

    /**
     * The key "apple_desktop:apr", the content is a string of base64-encoded binary content.
     */
    XMP_APR("Apr"),

    /**
     * The QuickTime "Metadata Image Size".
     */
    QUICKTIME_METAIMAGESIZE("MetaImageSize");

    private final String name;

    CustomTag(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.name;
    }

    public String parse(final String value) {
        return value;
    }
}

