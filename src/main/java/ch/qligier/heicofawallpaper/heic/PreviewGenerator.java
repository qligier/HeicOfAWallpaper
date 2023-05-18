package ch.qligier.heicofawallpaper.heic;

import ch.qligier.heicofawallpaper.model.DynamicWallpaperDefinition;
import ch.qligier.heicofawallpaper.service.FileSystemService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class PreviewGenerator {
    private static final Logger LOG = Logger.getLogger("PreviewGenerator");

    /**
     * The preview size in pixels.
     */
    private static final short WIDTH = 300;
    private static final short HEIGHT = 200;

    /**
     * The boundary angle, in degrees.
     */
    private static final short BOUNDARY_ANGLE = 70;

    /**
     * The maximum number of frames to show in the preview.
     */
    private static final short MAX_FRAMES = 6;


    public void generate(final DynamicWallpaperDefinition wallpaper,
                         final File wallpaperFile) throws IOException {
        final Path destinationFolder = FileSystemService.getDataPath().resolve(wallpaper.hash());

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
        final BufferedImage preview = this.loadFrame(wallpaper, (short) 0, destinationFolder);

        // For the next frames, copy only part of the image
        for (short frameIndex = 1; frameIndex < effectiveFrameNumber; ++frameIndex) {
            final Graphics2D graphics = preview.createGraphics();
            final BufferedImage frame = this.loadFrame(wallpaper, frameIndex, destinationFolder);

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
        final BufferedImage thumbnail = new BufferedImage(WIDTH, HEIGHT, preview.getType());
        final Graphics2D graphics = thumbnail.createGraphics();
        final Image rescaled = preview.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_AREA_AVERAGING);
        graphics.drawImage(rescaled, 0, 0, null);
        graphics.dispose();

        final File file = new File("preview.png");
        ImageIO.write(thumbnail, "png", file);
    }

    protected BufferedImage loadFrame(final DynamicWallpaperDefinition wallpaper,
                                      final short frameIndex,
                                      final Path destinationFolder) throws IOException {
        final String filename = String.format("%s-%d.jpg",
                                              wallpaper.filename().substring(0, wallpaper.filename().length() - 5),
                                              frameIndex);
        return ImageIO.read(destinationFolder.resolve(filename).toFile());
    }
}
