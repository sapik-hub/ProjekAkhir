package ProjekAstra.Controller.MainView;

import javafx.fxml.FXML;
import ProjekAstra.MainApp;

public class Tampilan {

    @FXML
    private void handleKaryawan() {
        MainApp.switchScene("/UILogin/UILoginKaryawan.fxml");
    }

    @FXML
    private void handlePenyewa() {
        MainApp.switchScene("/UIDashboard/UIDashboardPenyewa.fxml");
    }

    @FXML
    private void handlePemilik() {
        MainApp.switchScene("/UILogin/UILoginPemilik.fxml");
    }
}