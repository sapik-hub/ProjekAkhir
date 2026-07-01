package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardPenyewa {

    @FXML private StackPane contentPane;
    @FXML private Label lblJudul;

    @FXML
    private void handlePenyewa() {
        loadContent("/UICrud/UICrudPenyewa.fxml", "Data Penyewa");
    }

    @FXML
    private void handleLogout() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void loadContent(String fxmlPath, String judul) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            lblJudul.setText(judul);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}