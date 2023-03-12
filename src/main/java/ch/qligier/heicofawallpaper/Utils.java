package ch.qligier.heicofawallpaper;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Various utilities.
 *
 * @author Quentin Ligier
 **/
public class Utils {

    /**
     * This class is not instantiable.
     */
    private Utils() {
    }

    /**
     * Open the default browser to the given URL.
     *
     * @param link The URL to open.
     */
    public static void openBrowser(final String link) {
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (final IOException | URISyntaxException e) {
            showException(e);
        }
    }

    /**
     * Returns the project logo as a 128x128 {@link Image}.
     *
     * @return the project logo.
     */
    public static Image getLogo() {
        return new Image(Objects.requireNonNull(
            Utils.class.getResourceAsStream("/icon/logo_color_128.png")));
    }

    /**
     * Opens a dialog that shows details of the given exception.
     *
     * @param exception The exception to display to the user.
     */
    public static void showException(final Throwable exception) {
        final var alert = new Alert(
            Alert.AlertType.ERROR,
            "The exception stacktrace was:",
            ButtonType.CLOSE,
            ButtonType.YES
        );
        alert.setTitle("An exception arose");
        alert.setHeaderText("The application failed at some point. Sorry!");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(getLogo());
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CLOSE)).setDefaultButton(true);
        final var reportButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        reportButton.setText("Report the bug…");
        reportButton.setDefaultButton(false);

        final var stringWriter = new StringWriter();
        final var printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();

        final var textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent(textArea);

        final var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            openBrowser("https://github.com/qligier/HeicOfAWallpaper/issues/new");
        }
    }

    /**
     * Parses an EDID (Extended Display Identification Data) and extracts the monitor name from it.
     *
     * @param edid The EDID.
     * @return the monitor name provided in the EDID.
     * @implNote This implementation is based on <a
     * href="https://github.com/oshi/oshi/blob/master/oshi-core/src/main/java/oshi/util/EdidUtil.java">OSHI</a>,
     * released under the MIT License.
     * @see <a href="https://en.wikipedia.org/wiki/Extended_Display_Identification_Data#Display_Descriptors">Display
     * Descriptors</a>
     */
    public static String parseMonitorNameFromEdid(final byte[] edid) {
        for (int i = 0; i < 4; ++i) {
            final int type = ByteBuffer.wrap(Arrays.copyOfRange(edid, 54 + 18 * i, 58 + 18 * i)).getInt();

            if (type == 252) {
                return new String(Arrays.copyOfRange(edid, 58 + 18 * i, 72 + 18 * i),
                                  StandardCharsets.US_ASCII).trim();
            }
        }
        return "NO_NAME";
    }
}
