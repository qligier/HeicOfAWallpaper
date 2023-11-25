package ch.qligier.heicofawallpaper.gui.wallpaper_detail;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class FrameCell extends ListCell<FrameCell.FrameInfo> {

    @Override
    protected void updateItem(final @Nullable FrameInfo frameInfo, final boolean empty) {
        super.updateItem(frameInfo, empty);
        if (empty || frameInfo == null) {
            setGraphic(null);
            return;
        }
        final var hBox = new HBox();

        // Show the frame
        final var preview = new ImageView(new Image(frameInfo.framePath));
        preview.setFitWidth(128);
        preview.setFitHeight(72);
        hBox.getChildren().add(preview);

        // Show the frame index
        final var text = new Text("Frame %d".formatted(frameInfo.index));
        hBox.getChildren().add(text);

        this.setGraphic(hBox);
    }

    public record FrameInfo(int index,
                            String framePath) {
    }
}
