package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import com.dlsc.formsfx.model.structure.Element;
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
    @FXML private Button btnPenyewa;

    @FXML
    public void initialize() {
        handleKaryawan();
    }

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
    private void handlePenyewa(){
        loadContent("/UICrud/UICrudPenyewa.fxml", "Data Penyewa");
        setActiveButton(btnPenyewa);
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

        btnKaryawan.setStyle(btnKaryawan == active ? aktif : nonAktif);
        btnPenyewa.setStyle(btnPenyewa == active ? aktif : nonAktif);
        btnPemilik.setStyle(btnPemilik == active ? aktif : nonAktif);
    }

}