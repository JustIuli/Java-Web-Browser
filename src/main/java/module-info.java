module justiuli.javafxwebbrowser {
    requires javafx.controls;
    requires javafx.fxml;


    opens justiuli.javafxwebbrowser to javafx.fxml;
    exports justiuli.javafxwebbrowser;
}