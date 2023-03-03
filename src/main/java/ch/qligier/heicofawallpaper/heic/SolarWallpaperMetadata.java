package ch.qligier.heicofawallpaper.heic;

import com.dd.plist.NSDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HeicOfADesktop
 *
 * @author Quentin Ligier
 * @see <a href="https://gist.github.com/ole/6b6b5ef20fbec12e9227075e20c6e6ef">Reverse-engineering the dynamic wallpaper
 * file format in macOS Mojave.</a>
 * @see <a href="https://github.com/mczachurski/wallpapper">This is simple console application for macOS to create
 * dynamic wallpapers introduced in macOS Mojave.</a>
 **/
public class SolarWallpaperMetadata {

    /**
     * The metadata for each frame.
     */
    private final List<SolarFrameMetadata> frameMetadata;

    /**
     * Construct a new solar dynamic wallpaper.
     *
     * @param frameMetadata The metadata for each frame.
     */
    public SolarWallpaperMetadata(final List<SolarFrameMetadata> frameMetadata) {
        Objects.requireNonNull(frameMetadata, "frameMetadata shall not be null in SolarWallpaperMetadata()");
        this.frameMetadata = new ArrayList<>(frameMetadata);
    }

    public static SolarWallpaperMetadata fromBplist(final NSDictionary bplistDictionnary) {

        /*if (frameNumber != frameMetadata.size()) {
            throw new RuntimeException("");
        }*/
        return new SolarWallpaperMetadata(new ArrayList<>());
    }

    public static class SolarFrameMetadata {

        /**
         *
         *
         private final int index;

         /**
         *
         *
         private final boolean darkMode;

         /**
         * The angle between the sun and the observer's local horizon.
         *
         private final float altitude;

         /**
         * The angle of the sun around the horizon.
         *
         private final float azimuth;
         */
    }
}
