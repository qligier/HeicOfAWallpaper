package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.model.DynWallDefinition;
import ch.qligier.heicofawallpaper.service.FileSystemService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class PreviewGenerator {

    /**
     * The preview size in pixels.
     */
    public static final short WIDTH = 160;
    public static final short HEIGHT = 90;
    private static final Logger LOG = Logger.getLogger("PreviewGenerator");

    /**
     * The maximum number of frames to show in the preview.
     */
    private static final short MAX_FRAMES = 6;


    public static BufferedImage generate(final DynWallDefinition wallpaper) throws IOException {
        final Path cacheDirectory = FileSystemService.getDataPath().resolve(wallpaper.fileHash());

        // All calcules are made with the original size of the wallpaper. Resizing is done at the end
        final float wallpaperRatio = (float) wallpaper.width() / wallpaper.height();
        final float previewRatio = (float) WIDTH / HEIGHT;
        final short minX;
        final short maxX;
        final short minY;
        final short maxY;
        final short croppedWidth;
        final short croppedHeight;
        if (wallpaperRatio == previewRatio) {
            croppedWidth = wallpaper.width();
            croppedHeight = wallpaper.height();
            minX = 0;
            maxX = (short) (wallpaper.width() - 1);
            minY = 0;
            maxY = (short) (wallpaper.height() - 1);
        } else if (wallpaperRatio > previewRatio) {
            croppedWidth = (short) Math.round(previewRatio * wallpaper.height());
            croppedHeight = wallpaper.height();
            minX = (short) Math.round((float) (wallpaper.width() - croppedWidth) / 2);
            maxX = (short) (minX + croppedWidth);
            minY = 0;
            maxY = (short) (wallpaper.height() - 1);
        } else {
            croppedWidth = wallpaper.width();
            croppedHeight = (short) Math.round(wallpaper.width() / previewRatio);
            minX = 0;
            maxX = (short) (wallpaper.width() - 1);
            minY = (short) Math.round((float) (wallpaper.height() - croppedHeight) / 2);
            maxY = (short) (minY + croppedHeight);
        }

        final short effectiveFrameNumber = (short) Math.min(wallpaper.numberOfFrames(), MAX_FRAMES);
        final short frameWidth = (short) Math.round((1. / effectiveFrameNumber) * croppedWidth);
        final short boundaryDeltaX = (short) Math.round(0.08 * croppedWidth);

        // Frame 0: copy the whole image
        final BufferedImage preview = loadFrame((short) 0, cacheDirectory);

        // For the next frames, copy only part of the image
        for (short frameIndex = 1; frameIndex < effectiveFrameNumber; ++frameIndex) {
            final Graphics2D graphics = preview.createGraphics();
            final BufferedImage frame = loadFrame(frameIndex, cacheDirectory);

            final short boundaryX = (short) (frameIndex * frameWidth + minX);
            final Shape shape = new Polygon(new int[]{boundaryX + boundaryDeltaX, boundaryX - boundaryDeltaX, maxX,
                maxX},
                                            new int[]{minY, maxY, maxY, minY},
                                            4);
            graphics.setClip(shape);
            graphics.drawImage(frame, 0, 0, null);
            graphics.dispose();
        }

        // At this point, we cut and resize the full-size preview to the thumbnail size
        // The aspect ratio may not match, so we need to calculate the right thumbnail dimensions and the cropping to
        // apply.
        final short rescaledWidth;
        final short rescaledHeight;
        final short rescaledDeltaX;
        final short rescaledDeltaY;
        if (wallpaperRatio == previewRatio) {
            rescaledWidth = WIDTH;
            rescaledHeight = HEIGHT;
            rescaledDeltaX = 0;
            rescaledDeltaY = 0;
        } else if (wallpaperRatio > previewRatio) {
            rescaledWidth = (short) Math.round(wallpaperRatio * HEIGHT);
            rescaledHeight = HEIGHT;
            rescaledDeltaX = (short) -Math.round((float) (rescaledWidth - WIDTH) / 2);
            rescaledDeltaY = 0;
        } else {
            rescaledWidth = WIDTH;
            rescaledHeight = (short) Math.round(WIDTH / wallpaperRatio);
            rescaledDeltaX = 0;
            rescaledDeltaY = (short) -Math.round((float) (rescaledHeight - HEIGHT) / 2);
        }

        // The destination image that will be saved
        final BufferedImage thumbnail = new BufferedImage(WIDTH, HEIGHT, preview.getType());
        final Graphics2D graphics = thumbnail.createGraphics();

        // The rescaled image that we'll paste in the new buffer
        final Image rescaled = preview.getScaledInstance(rescaledWidth, rescaledHeight, Image.SCALE_AREA_AVERAGING);
        graphics.drawImage(rescaled, rescaledDeltaX, rescaledDeltaY, null);
        graphics.dispose();

        return thumbnail;
    }

    protected static BufferedImage loadFrame(final short frameIndex,
                                             final Path cacheDirectory) throws IOException {
        final String filename = String.format("frame-%d.jpg", frameIndex);
        return ImageIO.read(cacheDirectory.resolve(filename).toFile());
    }
}
