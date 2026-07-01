package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardPemilik {

    @FXML private StackPane contentPane;
    @FXML private Label lblJudul;
    @FXML private Button btnVilla, btnKategori, btnFasilitas;

    @FXML
    private void handleVilla() {
        loadContent("/UICrud/UICrudVilla.fxml", "Data Villa");
        setActiveButton(btnVilla);
    }

    @FXML
    private void handleKategori() {
        loadContent("/UICrud/UICrudKategoriVilla.fxml", "Data Kategori Villa");
        setActiveButton(btnKategori);
    }

    @FXML
    private void handleFasilitas() {
        loadContent("/UICrud/UICrudFasilitas.fxml", "Data Fasilitas");
        setActiveButton(btnFasilitas);
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
        String aktif = "-fx-background-color: white; -fx-text-fill: #1565C0; -fx-background-radius: 8; -fx-font-weight: bold;";
        String nonAktif = "-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;";

        btnVilla.setStyle(btnVilla == active ? aktif : nonAktif);
        btnKategori.setStyle(btnKategori == active ? aktif : nonAktif);
        btnFasilitas.setStyle(btnFasilitas == active ? aktif : nonAktif);
    }
}