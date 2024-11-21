module MinizaloApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires javafx.graphics;
	requires javafx.media;
    
    opens client.controller to javafx.fxml;
    exports client;
}