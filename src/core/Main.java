package core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("szperacz.fxml"));
        primaryStage.setTitle("Wyszukiwarka Gier");
        primaryStage.setScene(new Scene(root, 801, 631));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
