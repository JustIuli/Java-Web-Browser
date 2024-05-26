package justiuli.javafxwebbrowser;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

final public class Helpers {

    private static final Logger logger = Logger.getLogger(Helpers.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("webbrowser.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void LoadStartPage(WebEngine webEngine) {
        try {
            URL startPageUrl = Helpers.class.getResource("html/startPage.html");
            if (startPageUrl != null) {
                webEngine.load(startPageUrl.toExternalForm());
            } else {
                System.out.println("Unable to load startPage.html");
                logger.severe("Failed to load startPage.html");
            }
        } catch (Exception e) {
            logger.severe("An exception occurred: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public static void ChangeSceneTitle(String title, WebView webView){
        Stage stage = (Stage) webView.getScene().getWindow();
        Platform.runLater(() -> stage.setTitle(title));
    }

    public static void HandleUrlChange(TextArea htmlTextArea, String newLocationURL, TextField searchTextField, TabPane tabPane) {
        WebView currentWebView = getCurrentWebView(tabPane);
        if (currentWebView != null) {
            String htmlContent = (String) currentWebView.getEngine().executeScript("document.documentElement.outerHTML");
            htmlTextArea.setText(String.valueOf(Jsoup.parse(htmlContent)));
            htmlTextArea.setWrapText(true);


            if (newLocationURL.endsWith("startPage.html")) {
                searchTextField.setText("Start Page");
            } else {
                searchTextField.setText(newLocationURL);
            }
        }
    }

   public static WebView getCurrentWebView(TabPane tabPane) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            return (WebView) selectedTab.getContent();
        }
        return null;
    }

    public static void processTabSelectionChange(Tab newTab, TextField searchTextField, TextArea htmlTextArea){
            WebView webView = (WebView) newTab.getContent();
            String newTabUrl = webView.getEngine().getLocation();
            searchTextField.setText(newTabUrl);
            String htmlContent = (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
            htmlTextArea.setText(String.valueOf(Jsoup.parse(htmlContent)));
            htmlTextArea.setWrapText(true);
    }

    public static void loadPageFromTextField(TextField searchTextField ,TabPane tabPane) {
        String searchText = searchTextField.getText();
        WebView webView = getCurrentWebView(tabPane);

        if(webView == null) {
            logger.severe("An exception occurred : WebView is null");
            return;
        }

        if (!isValidUrl(searchText)) {
            String searchQuery = searchText.replace(" ", "+");
            String searchUrl = "https://www.google.com/search?q=" + searchQuery;
            webView.getEngine().load(searchUrl);
        } else {
                webView.getEngine().load(searchText);
        }
    }

    private static boolean isValidUrl(String searchText) {
        try {
            new URL(searchText).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void toggleHtmlViewer(SplitPane splitPane , VBox htmlViewer , TextArea htmlTextArea , TabPane tabPane) {
        if (splitPane.getItems().contains(htmlViewer)) {
            splitPane.getItems().remove(htmlViewer);
        } else {
            WebView currentWebView = getCurrentWebView(tabPane);
            if (currentWebView != null) {
                String htmlContent = (String) currentWebView.getEngine().executeScript("document.documentElement.outerHTML");
                htmlTextArea.setText(String.valueOf(Jsoup.parse(htmlContent)));
                htmlViewer.setVisible(true); // Add HTML content to the text field
            }
            splitPane.getItems().add(htmlViewer);
            splitPane.setDividerPositions(0.7);
        }
    }

    public static void createNewTab(TabPane tabPane){
        try {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            Tab tab = new Tab("New Tab", webView);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setClosable(true);

            webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
                if (newTitle != null && !newTitle.isEmpty()) {
                    tab.setText(newTitle);
                }
            });

            URL startPageUrl = Helpers.class.getResource("startPage.html");

            if (startPageUrl != null) {
                webEngine.load(startPageUrl.toExternalForm());
            } else {
                logger.warning("Failed to load startPage.html, redirecting to Google.");
                webEngine.load("https://www.google.com");
            }

            Tab defaultTab = findDefaultTab(tabPane);

            if (defaultTab != null) {
                tabPane.getTabs().remove(defaultTab);
            }
        } catch (Exception e) {
            logger.severe("An error occurred while adding a new tab: " + e.getMessage());
        }
    }

    public static Tab findDefaultTab(TabPane tabPane) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals("Start Tab")) {
                return tab;
            }
        }
        return null;
    }
}
