package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class LoginKaryawan implements Initializable {

    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private Button tabLogin;
    @FXML private Button tabRegister;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    @FXML private TextField regNama;
    @FXML private TextField regNoTelp;
    @FXML private TextField regAlamat;
    @FXML private TextField regUmur;      // Prompt di FXML = NIK KTP
    @FXML private DatePicker regTglMasuk;
    @FXML private ComboBox<String> regStatus;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        regStatus.getItems().addAll(
                "Admin",
                "Resepsionis",
                "Keuangan",
                "Manajer"
        );
    }

    @FXML
    private void showLogin() {
        paneLogin.setVisible(true);
        paneLogin.setManaged(true);

        paneRegister.setVisible(false);
        paneRegister.setManaged(false);

        tabLogin.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8;");
        tabRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #616161;");
    }

    @FXML
    private void showRegister() {
        paneRegister.setVisible(true);
        paneRegister.setManaged(true);

        paneLogin.setVisible(false);
        paneLogin.setManaged(false);

        tabRegister.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8;");
        tabLogin.setStyle("-fx-background-color: transparent; -fx-text-fill: #616161;");
    }

    @FXML
    private void handleLogin() {

        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Username dan Password wajib diisi!");
            return;
        }

        Koneksi k = new Koneksi();

        try {

            String sql = "SELECT NamaKaryawan, Status FROM Karyawan WHERE Username=? AND Password=?";

            PreparedStatement ps = k.conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                MainApp.switchScene("/UIDashboard/UIDashboardKaryawan.fxml");

            } else {

                alert(Alert.AlertType.ERROR, "Username atau Password salah!");

            }

        } catch (Exception e) {

            alert(Alert.AlertType.ERROR,
                    "Gagal terhubung ke database : " + e.getMessage());

        } finally {

            try {
                k.conn.close();
            } catch (Exception ignored) {}

        }
    }

    @FXML
    private void handleRegister() {

        String nama = regNama.getText().trim();
        String noTelp = regNoTelp.getText().trim();
        String alamat = regAlamat.getText().trim();
        String nikKtp = regUmur.getText().trim(); // field NIK KTP
        LocalDate tglMasuk = regTglMasuk.getValue();
        String status = regStatus.getValue();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();

        if (nama.isEmpty() ||
                noTelp.isEmpty() ||
                alamat.isEmpty() ||
                nikKtp.isEmpty() ||
                tglMasuk == null ||
                status == null ||
                username.isEmpty() ||
                password.isEmpty()) {

            alert(Alert.AlertType.WARNING,
                    "Semua field wajib diisi!");

            return;
        }

        Koneksi k = new Koneksi();

        try {

            CallableStatement cs =
                    k.conn.prepareCall("{call sp_InsertKaryawan(?, ?, ?, ?, ?, ?, ?, ?)}");

            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, alamat);
            cs.setString(4, nikKtp); // sementara memakai parameter ke-4
            cs.setString(5, username);
            cs.setString(6, password);
            cs.setDate(7, java.sql.Date.valueOf(tglMasuk));
            cs.setString(8, status);

            cs.execute();

            alert(Alert.AlertType.INFORMATION,
                    "Pendaftaran berhasil! Silakan login.");

            clearRegisterForm();

            showLogin();

        } catch (Exception e) {

            alert(Alert.AlertType.ERROR,
                    "Gagal mendaftar : " + e.getMessage());

        } finally {

            try {
                k.conn.close();
            } catch (Exception ignored) {}

        }
    }

    private void clearRegisterForm() {

        regNama.clear();
        regNoTelp.clear();
        regAlamat.clear();
        regUmur.clear();
        regTglMasuk.setValue(null);
        regStatus.setValue(null);
        regUsername.clear();
        regPassword.clear();

    }

    @FXML
    private void handleKembali() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void alert(Alert.AlertType type, String msg) {

        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();

    }
}