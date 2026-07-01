package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ProjekAstra.Koneksi.Koneksi;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class LoginPemilik {

    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private Button tabLogin;
    @FXML private Button tabRegister;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    @FXML private TextField regNama;
    @FXML private TextField regNoTelp;
    @FXML private TextField regEmail;
    @FXML private TextField regAlamat;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    @FXML
    private void showLogin() {
        paneLogin.setVisible(true); paneLogin.setManaged(true);
        paneRegister.setVisible(false); paneRegister.setManaged(false);
        tabLogin.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8;");
        tabRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #616161;");
    }

    @FXML
    private void showRegister() {
        paneRegister.setVisible(true); paneRegister.setManaged(true);
        paneLogin.setVisible(false); paneLogin.setManaged(false);
        tabRegister.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8;");
        tabLogin.setStyle("-fx-background-color: transparent; -fx-text-fill: #616161;");
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Username dan password wajib diisi!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_LoginPemilik(?, ?)}");
            cs.setString(1, username);
            cs.setString(2, password);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                MainApp.switchScene("/UIDashboard/UIDashboardPemilik.fxml");
                alert(Alert.AlertType.INFORMATION, "Login berhasil! Selamat datang " + rs.getString("Nama"));
            } else {
                alert(Alert.AlertType.ERROR, "Username atau password salah!");
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal terhubung ke database: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleRegister() {
        String nama = regNama.getText().trim();
        String noTelp = regNoTelp.getText().trim();
        String email = regEmail.getText().trim();
        String alamat = regAlamat.getText().trim();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPemilik(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, email);
            cs.setString(4, alamat);
            cs.setString(5, username);
            cs.setString(6, password);
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Pendaftaran berhasil! Silakan login.");
            clearRegisterForm();
            showLogin();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal mendaftar (username mungkin sudah dipakai): " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void clearRegisterForm() {
        regNama.clear(); regNoTelp.clear(); regEmail.clear();
        regAlamat.clear(); regUsername.clear(); regPassword.clear();
    }

    @FXML
    private void handleKembali() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}