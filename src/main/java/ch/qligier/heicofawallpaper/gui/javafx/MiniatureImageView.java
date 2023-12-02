package ch.qligier.heicofawallpaper.gui.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A specialisation of the JavaFX ImageView that displays a miniature of a given image path.
 *
 * @author Quentin Ligier
 **/
public class MiniatureImageView extends ImageView {

    public MiniatureImageView(final String imagePath, final int width, final int height) {
        super(new Image(imagePath, width, height, false, true, false));
        this.setFitWidth(width);
        this.setFitHeight(height);
    }
}
