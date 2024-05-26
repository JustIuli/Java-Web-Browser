package justiuli.javafxwebbrowser;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class BrowserController implements Initializable {

    private static final Logger logger = Logger.getLogger(BrowserController.class.getName());

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField searchTextField;

    @FXML
    private WebView webView;

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox htmlViewerVBOX;

    @FXML
    private TextArea htmlTextArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Triggers the html viewer if you press F12
        splitPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        WebEngine webEngine = webView.getEngine();
        Helpers.LoadStartPage(webEngine);

        ChangeListener<String> urlChangeListener = (obs, oldLocation, newLocation) -> Platform.runLater(() -> {
            Helpers.HandleUrlChange(htmlTextArea, newLocation, searchTextField , tabPane);
            htmlViewerVBOX.setVisible(true);
        });

        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> Helpers.ChangeSceneTitle(newTitle , webView));

        webEngine.locationProperty().addListener(urlChangeListener);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> Platform.runLater(() -> Helpers.processTabSelectionChange(newTab , searchTextField , htmlTextArea)));

        searchTextField.setOnAction(event -> Helpers.loadPageFromTextField(searchTextField , tabPane));

        splitPane.getItems().remove(htmlViewerVBOX);

        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (change.getList().isEmpty()) {
              Platform.exit();
            }
        });
    }



    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.F12) {
            Helpers.toggleHtmlViewer(splitPane , htmlViewerVBOX , htmlTextArea , tabPane);
        }
    }

    @FXML
    final public void addNewTab(){
        Helpers.createNewTab(tabPane);
    }

    @FXML
    final public void refreshPage() {
        try {
            WebView webView = Helpers.getCurrentWebView(tabPane);
            if (webView != null) {
                webView.getEngine().reload();
            } else {
                logger.warning("No web view found for refresh operation");
            }
        } catch (Exception e) {
            logger.severe("Failed to refresh the page: " + e.getMessage());
        }
    }

    @FXML
    final public void goBack() {
        try {
            WebView webView = Helpers.getCurrentWebView(tabPane);
            if (webView != null) {
                webView.getEngine().getHistory().go(-1);
            } else {
                logger.warning("No web view found for back navigation");
            }
        } catch (IndexOutOfBoundsException e) {
            logger.warning("Cannot go back: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    final public void goForward() {
        try {
            WebView webView = Helpers.getCurrentWebView(tabPane);
            if (webView == null) {
                throw new IllegalStateException("No web view found");
            }
            webView.getEngine().getHistory().go(1);
        } catch (IllegalStateException e) {
            logger.warning(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.warning("Cannot go forward: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Failed to go forward: " + e.getMessage());
        }
    }
}
