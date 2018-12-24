package fay.betacat.dev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class QQMusic extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("qqmusic.fxml"));

        Scene scene = new Scene(root, 500, 300);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("qqmusic.css").toExternalForm());

        primaryStage.setTitle("QQ Music Download");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
