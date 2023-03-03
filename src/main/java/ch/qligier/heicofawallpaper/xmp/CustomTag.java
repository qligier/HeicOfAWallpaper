package ch.qligier.heicofawallpaper.xmp;

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
    XMP_SOLAR("solar", Type.STRING),

    /**
     * The key "apple_desktop:h24", the content is a string of base64-encoded binary content.
     */
    XMP_H24("h24", Type.STRING),

    /**
     * The key "apple_desktop:apr", the content is a string of base64-encoded binary content.
     */
    XMP_APR("apr", Type.STRING);

    private final String name;
    private final Type type;

    CustomTag(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.name;
    }

    public <T> T parse(String value) {
        return this.type.parse(value);
    }

    private enum Type {
        STRING {
            @Override
            public <T> T parse(String value) {
                return (T) value;
            }
        };

        public abstract <T> T parse(String var1);
    }
}

