package ProjekAstra;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        mainStage = primaryStage;

        Parent root = loadFXML("/UIMainView/UITampilan.fxml");
        Scene scene = new Scene(root);

        primaryStage.setTitle("VillaNesia");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();

        fadeIn(root);
    }

    public static void switchScene(String fxmlPath) {
        try {
            Parent newRoot = loadFXML(fxmlPath);

            // set opacity 0 dulu sebelum ditampilkan, biar nggak "kedip" pas ganti scene
            newRoot.setOpacity(0);

            Scene currentScene = mainStage.getScene();
            currentScene.setRoot(newRoot);

            // jaga-jaga kalau fullscreen ke-toggle off, pastiin tetap fullscreen
            if (!mainStage.isFullScreen()) {
                mainStage.setFullScreen(true);
            }

            fadeIn(newRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fadeIn(Parent root) {
        FadeTransition fade = new FadeTransition(Duration.millis(350), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private static Parent loadFXML(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(path));
        return loader.load();
    }

    public static Stage getStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}