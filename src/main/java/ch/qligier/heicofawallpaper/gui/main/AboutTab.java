package ch.qligier.heicofawallpaper.gui.main;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.Utils;
import ch.qligier.heicofawallpaper.configuration.StaticConfiguration;
import com.sandec.mdfx.MarkdownView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Objects;

/**
 * The 'about' tab controller.
 *
 * @author Quentin Ligier
 **/
public class AboutTab extends AbstractContentTab {

    /**
     * JavaFX markup.
     */
    @FXML
    protected ScrollPane changelogScrollPane;

    @FXML
    protected ScrollPane creditsScrollPane;

    @FXML
    protected Text licenseText;

    @FXML
    protected Label appNameLabel;

    @FXML
    protected Label appVersionLabel;

    @FXML
    protected Hyperlink gitHubLink;

    public AboutTab(final HoawApplication app,
                    final MainWindow window) {
        super(app, window);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main/about.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception exception) {
            Utils.showException(exception);
            return;
        }

        this.appNameLabel.setText(StaticConfiguration.APP_NAME);
        this.appVersionLabel.setText(StaticConfiguration.APP_VERSION);

        this.licenseText.setText(this.loadResource("/LICENSE.txt"));
        this.changelogScrollPane.setContent(new CustomMarkdownView(this.loadResource("/CHANGELOG.md")));
        this.creditsScrollPane.setContent(new CustomMarkdownView(this.loadResource("/NOTICE.md")));
    }

    /**
     * Opens the GitHub project in a browser.
     */
    @FXML
    protected void openGitHub() {
        Utils.openBrowser("https://github.com/qligier/HeicOfAWallpaper");
        this.gitHubLink.setVisited(false);
    }

    /**
     * Opens my personal website in a browser.
     */
    @FXML
    protected void openPersonalSite() {
        Utils.openBrowser("https://www.qligier.ch");
        this.gitHubLink.setVisited(false);
    }

    /**
     * Fetches a resource and reads it as a {@link String}.
     *
     * @param path The resource path.
     * @return the resource content or an empty string.
     */
    private String loadResource(final String path) {
        try (final var is = AboutTab.class.getResourceAsStream(path)) {
            if (is == null) {
                return "";
            }
            return new String(is.readAllBytes());
        } catch (final IOException e) {
            Utils.showException(e);
            return "";
        }
    }

    /**
     * A customization of the markdown view component.
     */
    private static final class CustomMarkdownView extends MarkdownView {

        public CustomMarkdownView(final String mdString) {
            super(mdString);
            final var cssPath = Objects.requireNonNull(AboutTab.class.getResource("mdfx.css")).toExternalForm();
            this.getStylesheets().clear();
            this.getStylesheets().add(cssPath);
            this.setPrefWidth(420);
            this.setMaxWidth(420);
        }


        @Override
        public void setLink(final Node node, final String link, final String description) {
            node.setOnMouseClicked(e -> Utils.openBrowser(link.strip()));
            Tooltip.install(node, new Tooltip(link));
        }
    }
}
