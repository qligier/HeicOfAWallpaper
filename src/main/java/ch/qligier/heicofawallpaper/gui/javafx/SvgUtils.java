package ch.qligier.heicofawallpaper.gui.javafx;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This class provides a way to load SVG files as JavaFX images.
 * <p>
 * https://github.com/weisJ/darklaf/blob/master/property-loader/src/main/java/com/github/weisj/darklaf/properties/icons/ThemedSVGIconParserProvider.java
 *
 * @author Quentin Ligier
 **/
public class SvgUtils {

    /**
     * This class is not instantiable.
     */
    private SvgUtils() {
    }

    public static WritableImage loadFromResource(final String svgPath) {
        return copyToBufferedImage(new SVGLoader().load(SvgUtils.class.getResource(svgPath)));
    }

    public static WritableImage loadFromContent(final String svgContent) {
        return copyToBufferedImage(new SVGLoader().load(new ByteArrayInputStream(svgContent.getBytes(StandardCharsets.UTF_8))));
    }

    private static WritableImage copyToBufferedImage(final SVGDocument svg) {
        final FloatSize size = svg.size();
        final var image = new BufferedImage((int) size.getWidth(),
                                            (int) size.getHeight(),
                                            BufferedImage.TYPE_INT_ARGB_PRE);
        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                        RenderingHints.VALUE_ANTIALIAS_ON));
        svg.render(null, graphics2D);
        graphics2D.dispose();
        return SwingFXUtils.toFXImage(image, null); // This process could be optimized
    }
}
