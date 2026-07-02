package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardKaryawan {

    @FXML private StackPane contentPane;
    @FXML private Label lblJudul;

    @FXML private Button btnKaryawan;
    @FXML private Button btnPemilik;

    @FXML
    private void handleKaryawan() {
        loadContent("/UICrud/UICrudKaryawan.fxml", "Data Karyawan");
        setActiveButton(btnKaryawan);
    }

    @FXML
    private void handlePemilik() {
        loadContent("/UICrud/UICrudPemilik.fxml", "Data Pemilik");
        setActiveButton(btnPemilik);
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

    private void setActiveButton(Button active) {
        btnKaryawan.getStyleClass().setAll("sidebar-menu-btn");
        btnPemilik.getStyleClass().setAll("sidebar-menu-btn");
        active.getStyleClass().setAll("sidebar-menu-btn-active");
    }
}