module justiuli.javafxwebbrowser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;
    requires org.jsoup;


    opens justiuli.javafxwebbrowser to javafx.fxml;
    exports justiuli.javafxwebbrowser;
}