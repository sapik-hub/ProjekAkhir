package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import ProjekAstra.Util.NotifUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class LoginPenyewa {

    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private Button tabLogin;
    @FXML private Button tabRegister;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    @FXML private TextField regNama;
    @FXML private TextField regNoTelp;
    @FXML private TextField regNikKtp;
    @FXML private DatePicker regTglLahir;
    @FXML private TextField regAlamat;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    @FXML
    private void handleNikOnlyNumber(KeyEvent event) {

        String karakter = event.getCharacter();

        if (!karakter.matches("[0-9]")) {
            event.consume();
        }

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
            notifLogin(NotifUtil.Type.WARNING, "Username dan Password wajib diisi!");
            return;
        }

        Koneksi k = new Koneksi();

        try {

            String sql = "SELECT Nama FROM Penyewa WHERE Username = ? AND Password = ?";

            PreparedStatement ps = k.conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String nama = rs.getString("Nama");
                NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                        "Login berhasil! Selamat datang, " + nama + " 🌸",
                        () -> MainApp.switchScene("/UIDashboard/UIDashboardPenyewa.fxml"));

            } else {

                notifLogin(NotifUtil.Type.ERROR, "Username atau Password salah!");

            }

        } catch (Exception e) {

            notifLogin(NotifUtil.Type.ERROR, "Gagal terhubung ke database : " + e.getMessage());

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
        String nikKtp = regNikKtp.getText().trim();
        LocalDate tglLahir = regTglLahir.getValue();
        String alamat = regAlamat.getText().trim();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();

        if (nama.isEmpty() ||
                noTelp.isEmpty() ||
                nikKtp.isEmpty() ||
                tglLahir == null ||
                alamat.isEmpty() ||
                username.isEmpty() ||
                password.isEmpty()) {

            notifRegister(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return;
        }

        if (!nikKtp.matches("\\d{16}")) {
            notifRegister(NotifUtil.Type.WARNING, "NIK KTP harus terdiri dari 16 digit angka!");
            return;
        }

        Koneksi k = new Koneksi();

        try {

            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPenyewa(?, ?, ?, ?, ?, ?, ?)}");

            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, nikKtp);
            cs.setDate(4, java.sql.Date.valueOf(tglLahir));
            cs.setString(5, alamat);
            cs.setString(6, username);
            cs.setString(7, password);

            cs.execute();

            NotifUtil.show(regNama, NotifUtil.Type.SUCCESS,
                    "Pendaftaran berhasil! Silakan login.",
                    () -> {
                        clearRegisterForm();
                        showLogin();
                    });

        } catch (Exception e) {

            notifRegister(NotifUtil.Type.ERROR,
                    "Gagal mendaftar (username mungkin sudah dipakai): " + e.getMessage());

        } finally {

            try {
                k.conn.close();
            } catch (Exception ignored) {}

        }
    }

    private void clearRegisterForm() {

        regNama.clear();
        regNoTelp.clear();
        regNikKtp.clear();
        regTglLahir.setValue(null);
        regAlamat.clear();
        regUsername.clear();
        regPassword.clear();

    }

    @FXML
    private void handleKembali() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void notifLogin(NotifUtil.Type type, String msg) {
        NotifUtil.show(loginUsername, type, msg);
    }

    private void notifRegister(NotifUtil.Type type, String msg) {
        NotifUtil.show(regNama, type, msg);
    }
}