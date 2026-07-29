package ProjekAstra.Util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PopupUtil {
    public static void showPopup(Stage owner, Parent content, String title, boolean modal, boolean resizable) {
        Stage popupStage = new Stage();
        popupStage.initOwner(owner);
        popupStage.setTitle(title);
        popupStage.setScene(new Scene(content));
        popupStage.setResizable(resizable);

        if (modal) {
            popupStage.initModality(Modality.WINDOW_MODAL);
        }

        popupStage.showAndWait();
    }
}