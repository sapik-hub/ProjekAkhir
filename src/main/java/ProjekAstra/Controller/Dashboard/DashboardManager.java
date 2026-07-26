package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardManager {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label lblJudul;

    @FXML
    private Button btnKaryawan;

    @FXML
    private Button btnLaporanTransaksi;

    @FXML
    private Button btnLaporanRefund;


    @FXML
    public void initialize() {
        handleKaryawan();
    }

    @FXML
    private void handleKaryawan() {
        loadContent(
                "/UICrud/UICrudKaryawan.fxml",
                "Data Karyawan"
        );
        setActiveButton(btnKaryawan);
    }

    @FXML
    private void handleLaporanTransaksi() {
        loadContent("/UILaporan/UILaporanBooking.fxml", "Laporan Transaksi Booking");
        setActiveButton(btnLaporanTransaksi);
    }

    @FXML
    private void handleLaporanRefund() {
        loadContent("/UILaporan/UILaporanRefund.fxml", "Laporan Transaksi Refund");
        setActiveButton(btnLaporanRefund);
    }

    @FXML
    private void handleLogout() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void loadContent(String fxmlPath, String judul) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            lblJudul.setText(judul);

        } catch (IOException | NullPointerException e) {
            System.out.println(
                    "Gagal membuka file : " + fxmlPath
            );
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button active) {
        String aktif =
                "-fx-background-color: white;" +
                        "-fx-text-fill: #1565C0;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";


        String nonAktif =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";

        btnKaryawan.setStyle(
                btnKaryawan == active ? aktif : nonAktif
        );
        btnLaporanTransaksi.setStyle(
                btnLaporanTransaksi == active ? aktif : nonAktif
        );
        btnLaporanRefund.setStyle(
                btnLaporanRefund == active ? aktif : nonAktif
        );
    }
}