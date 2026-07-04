package ProjekAstra.Controller.Login;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.MainApp;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.CallableStatement;
import java.sql.ResultSet;

public class LoginPemilik {

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

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
            CallableStatement cs = k.conn.prepareCall("{call sp_LoginPemilik(?, ?)}");
            cs.setString(1, username);
            cs.setString(2, password);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                String id = rs.getString("Id");
                String nama = rs.getString("Nama");

                Session.setPemilik(id, nama);

                NotifUtil.show(loginUsername, NotifUtil.Type.SUCCESS,
                        "Login berhasil! Selamat datang, " + nama + " 🌸",
                        () -> MainApp.switchScene("/UIDashboard/UIDashboardPemilik.fxml"));
            } else {
                notifLogin(NotifUtil.Type.ERROR, "Username atau password salah!");
            }
        } catch (Exception e) {
            notifLogin(NotifUtil.Type.ERROR, "Gagal terhubung ke database : " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleKembali() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }

    private void notifLogin(NotifUtil.Type type, String msg) {
        NotifUtil.show(loginUsername, type, msg);
    }
}