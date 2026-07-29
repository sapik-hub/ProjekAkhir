package ProjekAstra.Controller.MainView;

import javafx.fxml.FXML;
import ProjekAstra.MainApp;

public class Tampilan {

    @FXML
    private void handleKaryawan() {
        MainApp.switchScene("/UILogin/UIloginKaryawan.fxml");
    }

    @FXML
    private void handlePenyewa() {
        MainApp.switchScene("/UIDashboard/UIDashboardPenyewa.fxml");
    }

}