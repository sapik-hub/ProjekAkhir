package ProjekAstra.Util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class NotifUtil {

    public enum Type { SUCCESS, ERROR, WARNING, INFO }

    private static final String WRAPPER_KEY = "notifWrapper";
    private static final double DEFAULT_DURATION_SECONDS = 2.0;

    public static void show(Node owner, Type type, String message) {
        show(owner, type, message, DEFAULT_DURATION_SECONDS, null);
    }

    public static void show(Node owner, Type type, String message, Runnable onClose) {
        show(owner, type, message, DEFAULT_DURATION_SECONDS, onClose);
    }

    public static void show(Node owner, Type type, String message, double durationSeconds, Runnable onClose) {
        Scene scene = owner.getScene();
        if (scene == null) {
            if (onClose != null) onClose.run();
            return;
        }

        StackPane wrapper = getOrCreateWrapper(scene);

        wrapper.getChildren().removeIf(n -> Boolean.TRUE.equals(n.getProperties().get("isNotifLayer")));

        HBox toast = buildToast(type, message);
        wrapper.getChildren().add(toast);

        StackPane.setAlignment(toast, Pos.TOP_LEFT);
        StackPane.setMargin(toast, new Insets(24, 0, 0, 24));

        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition wait = new PauseTransition(Duration.seconds(durationSeconds));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, wait, fadeOut);
        sequence.setOnFinished(e -> {
            wrapper.getChildren().remove(toast);
            if (onClose != null) onClose.run();
        });
        sequence.play();
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

    private static HBox buildToast(Type type, String message) {
        Label icon = new Label(iconFor(type));
        icon.setStyle("-fx-font-size: 15px;");

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(260);
        lblMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A5568; -fx-font-weight: bold;");

        HBox toast = new HBox(8, icon, lblMessage);
        toast.getProperties().put("isNotifLayer", true);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 14));

        // KUNCI PERBAIKAN: paksa toast hanya sebesar isinya,
        // jangan biarkan StackPane menstretch-nya.
        toast.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        toast.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        toast.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + borderColorFor(type) + ";" +
                        "-fx-border-width: 0 0 0 4;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 12, 0, 0, 4);"
        );
        toast.setPickOnBounds(false);

        return toast;
    }

    private static String iconFor(Type type) {
        return switch (type) {
            case SUCCESS -> "✅";
            case ERROR -> "⚠️";
            case WARNING -> "❗";
            case INFO -> "ℹ️";
        };
    }

    private static String borderColorFor(Type type) {
        return switch (type) {
            case SUCCESS -> "#4CAF7D";
            case ERROR -> "#F76C8A";
            case WARNING -> "#FFC98B";
            case INFO -> "#7FC1E3";
        };
    }
}