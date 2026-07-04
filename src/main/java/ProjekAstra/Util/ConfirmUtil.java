package ProjekAstra.Util;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ConfirmUtil {

    private static final String WRAPPER_KEY = "notifWrapper";

    public static void show(Node owner, String message, Runnable onConfirm) {
        show(owner, message, onConfirm, null);
    }

    public static void show(Node owner, String message, Runnable onConfirm, Runnable onCancel) {
        Scene scene = owner.getScene();
        if (scene == null) return;

        StackPane wrapper = getOrCreateWrapper(scene);

        // Backdrop biar background gak bisa diklik selama konfirmasi tampil
        Region backdrop = new Region();
        backdrop.getProperties().put("isConfirmLayer", true);
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        backdrop.prefWidthProperty().bind(wrapper.widthProperty());
        backdrop.prefHeightProperty().bind(wrapper.heightProperty());
        backdrop.setPickOnBounds(true);

        VBox card = buildCard(message,
                () -> close(wrapper, backdrop, onConfirm),
                () -> close(wrapper, backdrop, onCancel));
        card.getProperties().put("isConfirmLayer", true);

        wrapper.getChildren().addAll(backdrop, card);
        StackPane.setAlignment(card, Pos.CENTER);

        backdrop.setOpacity(0);
        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);

        FadeTransition fadeBg = new FadeTransition(Duration.millis(150), backdrop);
        fadeBg.setFromValue(0);
        fadeBg.setToValue(1);

        FadeTransition fadeCard = new FadeTransition(Duration.millis(150), card);
        fadeCard.setFromValue(0);
        fadeCard.setToValue(1);

        fadeBg.play();
        fadeCard.play();
        card.setScaleX(1);
        card.setScaleY(1);
    }

    private static void close(StackPane wrapper, Region backdrop, Runnable callback) {
        wrapper.getChildren().removeIf(n -> Boolean.TRUE.equals(n.getProperties().get("isConfirmLayer")));
        if (callback != null) callback.run();
    }

    private static VBox buildCard(String message, Runnable onConfirm, Runnable onCancel) {
        Label icon = new Label("❓");
        icon.setStyle("-fx-font-size: 26px;");

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(260);
        lblMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A5568; -fx-font-weight: bold; -fx-text-alignment: center;");
        lblMessage.setAlignment(Pos.CENTER);

        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(
                "-fx-background-color: #F1F1F4;" +
                        "-fx-text-fill: #616161;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        btnBatal.setPrefWidth(110);
        btnBatal.setOnAction(e -> onCancel.run());

        Button btnYa = new Button("Ya, Lanjutkan");
        btnYa.setStyle(
                "-fx-background-color: linear-gradient(to right, #F76C8A, #FF9AA8);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        btnYa.setPrefWidth(140);
        btnYa.setOnAction(e -> onConfirm.run());

        HBox buttons = new HBox(10, btnBatal, btnYa);
        buttons.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, icon, lblMessage, buttons);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(26, 28, 22, 28));
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 20, 0, 0, 6);"
        );

        return card;
    }

    private static StackPane getOrCreateWrapper(Scene scene) {
        Parent currentRoot = scene.getRoot();

        if (currentRoot instanceof StackPane && Boolean.TRUE.equals(currentRoot.getProperties().get(WRAPPER_KEY))) {
            return (StackPane) currentRoot;
        }

        StackPane wrapper = new StackPane();
        wrapper.getProperties().put(WRAPPER_KEY, true);
        wrapper.getChildren().add(currentRoot);
        scene.setRoot(wrapper);
        return wrapper;
    }
}