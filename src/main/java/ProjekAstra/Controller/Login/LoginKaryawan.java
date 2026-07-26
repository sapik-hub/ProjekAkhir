package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
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

        // ================= AKUN TEST CEPAT (SEMENTARA) =================
        // Dipakai untuk coba dashboard SuperAdmin & Manager tanpa perlu
        // setting Role di database dulu. Hapus/comment blok ini kalau
        // sudah pakai data Role asli dari tabel Karyawan.
        if (username.equals("s") && password.equals("a")) {
            Session.setKaryawan("TEST-SA", "SuperAdmin (Test)", "SuperAdmin");
            NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                    "Login berhasil! Selamat datang, SuperAdmin👤",
                    () -> MainApp.switchScene("/UIDashboard/UIDashboardKaryawan.fxml"));
            return;
        }
        if (username.equals("a") && password.equals("s")) {
            Session.setKaryawan("TEST-MGR", "Manager (Test)", "Manager");
            NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                    "Login berhasil! Selamat datang, Manager👤",
                    () -> MainApp.switchScene("/UIDashboard/UIDashboardPenyewa.fxml"));
            return;
        }
        // =================================================================

        Koneksi k = new Koneksi();

        try {
            String sql = "SELECT Id_Karyawan, Nama_Karyawan, Status, Role FROM Karyawan WHERE Username=? AND Password=? AND Status='AKTIF'";

            PreparedStatement ps = k.conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String id = rs.getString("Id_Karyawan");
                String nama = rs.getString("Nama_Karyawan");
                String role = rs.getString("Role");

                Session.setKaryawan(id, nama, role);

                String tujuanDashboard = getDashboardByRole(role);

                if (tujuanDashboard == null) {
                    notifLogin(NotifUtil.Type.ERROR, "Role '" + role + "' tidak dikenali, hubungi admin!");
                    return;
                }

                NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                        ("Login berhasil! Selamat datang, " + nama + "👤"),
                        () -> MainApp.switchScene(tujuanDashboard));
            } else {
                notifLogin(NotifUtil.Type.ERROR, "Username/Password salah atau akun tidak aktif!");
            }

        } catch (Exception e) {
            notifLogin(NotifUtil.Type.ERROR, "Gagal terhubung ke database : " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Menentukan tujuan dashboard berdasarkan Role yang tersimpan di database.
     * Sesuaikan value String di sini kalau penamaan Role di tabel Karyawan berbeda.
     */
    private String getDashboardByRole(String role) {
        if (role == null) return null;

        switch (role.trim().toLowerCase()) {
            case "superadmin":
            case "admin":
                return "/UIDashboard/UIDashboardKaryawan.fxml";
            case "manager":
                return "/UIDashboard/UIDashboardPenyewa.fxml";
            default:
                return null;
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