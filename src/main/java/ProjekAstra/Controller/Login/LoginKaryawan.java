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
import java.time.Period;
import java.util.ResourceBundle;

public class LoginKaryawan implements Initializable {

    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private Button tabLogin;
    @FXML private Button tabRegister;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    // ===== REGISTER FORM =====
    @FXML private TextField regNama;
    @FXML private TextField regNoTelp;
    @FXML private TextField regAlamat;
    @FXML private TextField regUmur;
    @FXML private DatePicker regTglLahir;
    @FXML private DatePicker regTglMasuk;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Auto-calc Umur dari Tanggal Lahir
        regTglLahir.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int umur = hitungUmur(newVal);
                regUmur.setText(String.valueOf(umur));
            } else {
                regUmur.clear();
            }
        });

        // Default tampilan login
        showLogin();
    }

    // ===========================================================
    // HITUNG UMUR OTOMATIS
    // ===========================================================
    private int hitungUmur(LocalDate tanggalLahir) {
        if (tanggalLahir == null) return 0;
        LocalDate today = LocalDate.now();
        Period period = Period.between(tanggalLahir, today);
        return period.getYears();
    }

    // ===========================================================
    // TAB SWITCH
    // ===========================================================
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

    // ===========================================================
    // LOGIN
    // ===========================================================
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
            String sql = "SELECT Id_Karyawan, Nama_Karyawan, Status, Role FROM Karyawan WHERE Username=? AND Password=? AND Status='Aktif'";

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
                        ("Login berhasil! Selamat datang, " + nama + " 👤"),
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

    private String getDashboardByRole(String role) {
        if (role == null) return null;

        switch (role.trim().toLowerCase()) {
            case "superadmin":
            case "admin":
                return "/UIDashboard/UIDashboardKaryawan.fxml";
            case "manager":
                return "/UIDashboard/UIDashboardManager.fxml";
            default:
                return null;
        }
    }

    // ===========================================================
    // REGISTER
    // ===========================================================
    @FXML
    private void handleRegister() {

        String nama = regNama.getText().trim();
        String noTelp = regNoTelp.getText().trim();
        String alamat = regAlamat.getText().trim();
        String umurText = regUmur.getText().trim();
        LocalDate tglLahir = regTglLahir.getValue();
        LocalDate tglMasuk = regTglMasuk.getValue();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();

        // ===== VALIDASI =====
        if (nama.isEmpty() ||
                noTelp.isEmpty() ||
                alamat.isEmpty() ||
                umurText.isEmpty() ||
                tglLahir == null ||
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

        // ===== VALIDASI FORMAT =====
        if (!nama.matches("^[a-zA-Z\\s]+$")) {
            notifRegister(NotifUtil.Type.WARNING, "Nama tidak boleh mengandung angka atau simbol!");
            return;
        }

        if (!noTelp.matches("^[0-9]+$")) {
            notifRegister(NotifUtil.Type.WARNING, "No Telp hanya boleh berisi angka!");
            return;
        }

        if (!alamat.matches("^[a-zA-Z\\s]+$")) {
            notifRegister(NotifUtil.Type.WARNING, "Alamat tidak boleh mengandung angka atau simbol!");
            return;
        }

        // ===== SIMPAN KE DATABASE =====
        Koneksi k = new Koneksi();

        try {
            // SP Insert Karyawan dengan Tanggal Lahir (9 parameter)
            CallableStatement cs =
                    k.conn.prepareCall("{call sp_InsertKaryawan(?, ?, ?, ?, ?, ?, ?, ?, ?)}");

            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, alamat);
            cs.setInt(4, umur);
            cs.setDate(5, java.sql.Date.valueOf(tglLahir));
            cs.setString(6, username);
            cs.setString(7, password);
            cs.setDate(8, java.sql.Date.valueOf(tglMasuk));
            cs.setString(9, "Admin");  // Default Role = Admin untuk pendaftaran

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
        regTglLahir.setValue(null);
        regTglMasuk.setValue(null);
        regUsername.clear();
        regPassword.clear();
    }

    @FXML
    private void handleKembali() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    // ===========================================================
    // NOTIF
    // ===========================================================
    private void notifLogin(NotifUtil.Type type, String msg) {
        NotifUtil.show(loginUsername, type, msg);
    }

    private void notifRegister(NotifUtil.Type type, String msg) {
        NotifUtil.show(regNama, type, msg);
    }
}