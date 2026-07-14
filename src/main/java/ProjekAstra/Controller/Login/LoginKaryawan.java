package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import ProjekAstra.Util.NotifUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
    @FXML private TextField regUmur;
    @FXML private DatePicker regTglMasuk;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void showLogin() {
        paneLogin.setVisible(true);
        paneLogin.setManaged(true);

        paneRegister.setVisible(false);
        paneRegister.setManaged(false);

        tabLogin.getStyleClass().setAll("tab-btn-active");
        tabRegister.getStyleClass().setAll("tab-btn-inactive");
    }

    @FXML
    private void showRegister() {
        paneRegister.setVisible(true);
        paneRegister.setManaged(true);

        paneLogin.setVisible(false);
        paneLogin.setManaged(false);

        tabRegister.getStyleClass().setAll("tab-btn-active");
        tabLogin.getStyleClass().setAll("tab-btn-inactive");
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
            String sql = "SELECT NamaKaryawan, Status, Role FROM Karyawan WHERE Username=? AND Password=? AND Status='AKTIF'";

            PreparedStatement ps = k.conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("NamaKaryawan");
                NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                        ("Login berhasil! Selamat datang, " + nama + "👤"),
                        () -> MainApp.switchScene("/UIDashboard/UIDashboardKaryawan.fxml"));
            } else {
                notifLogin(NotifUtil.Type.ERROR, "Username/Password salah atau akun tidak aktif!");
            }

        } catch (Exception e) {
            notifLogin(NotifUtil.Type.ERROR, "Gagal terhubung ke database : " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleRegister() {

        String nama = regNama.getText().trim();
        String noTelp = regNoTelp.getText().trim();
        String alamat = regAlamat.getText().trim();
        String umurText = regUmur.getText().trim();
        LocalDate tglMasuk = regTglMasuk.getValue();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();

        if (nama.isEmpty() ||
                noTelp.isEmpty() ||
                alamat.isEmpty() ||
                umurText.isEmpty() ||
                tglMasuk == null ||
                username.isEmpty() ||
                password.isEmpty()) {

            notifRegister(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return;
        }

        int umur;
        try {
            umur = Integer.parseInt(umurText);
        } catch (NumberFormatException e) {
            notifRegister(NotifUtil.Type.WARNING, "Umur harus berupa angka!");
            return;
        }

        Koneksi k = new Koneksi();

        try {
            CallableStatement cs =
                    k.conn.prepareCall("{call sp_InsertKaryawan(?, ?, ?, ?, ?, ?, ?)}");

            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, alamat);
            cs.setInt(4, umur);
            cs.setString(5, username);
            cs.setString(6, password);
            cs.setDate(7, java.sql.Date.valueOf(tglMasuk));

            cs.execute();

            NotifUtil.show(regNama, NotifUtil.Type.SUCCESS,
                    "Pendaftaran berhasil! Silakan login.",
                    () -> {
                        clearRegisterForm();
                        showLogin();
                    });

        } catch (Exception e) {
            notifRegister(NotifUtil.Type.ERROR, "Gagal mendaftar : " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void clearRegisterForm() {
        regNama.clear();
        regNoTelp.clear();
        regAlamat.clear();
        regUmur.clear();
        regTglMasuk.setValue(null);
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