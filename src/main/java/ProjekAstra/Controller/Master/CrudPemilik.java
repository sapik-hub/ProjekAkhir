package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Pemilik;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class CrudPemilik implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtEmail, txtAlamat, txtUsername, txtCari;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Pemilik> tablePemilik;
    @FXML private TableColumn<Pemilik, String> colId, colNama, colNoTelp, colEmail, colAlamat, colUsername;

    private final ObservableList<Pemilik> listPemilik = FXCollections.observableArrayList();

    // ===========================================================
    // INIT
    // ===========================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTable();
        setClose();

        tablePemilik.setOnMouseClicked(e -> {
            Pemilik p = tablePemilik.getSelectionModel().getSelectedItem();
            if (p != null) populateForm(p);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariPemilik(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPemilik"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNoTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    // ===========================================================
    // LOAD & SEARCH
    // ===========================================================
    private void loadTable() {
        listPemilik.clear();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllPemilik}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                listPemilik.add(new Pemilik(
                        rs.getString("IdPemilik"),
                        rs.getString("Nama"),
                        rs.getString("NoTelp"),
                        rs.getString("Email"),
                        rs.getString("Alamat"),
                        rs.getString("Username")
                ));
            }
            tablePemilik.setItems(listPemilik);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariPemilik(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tablePemilik.setItems(listPemilik);
            return;
        }
        ObservableList<Pemilik> hasil = FXCollections.observableArrayList();
        for (Pemilik p : listPemilik) {
            if (p.getNama().toLowerCase().contains(keyword.toLowerCase()) ||
                    p.getIdPemilik().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(p);
            }
        }
        tablePemilik.setItems(hasil);
    }

    // ===========================================================
    // CRUD ACTIONS
    // ===========================================================
    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPemilik(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setString(3, txtEmail.getText().trim());
            cs.setString(4, txtAlamat.getText().trim());
            cs.setString(5, txtUsername.getText().trim());
            cs.setString(6, txtPassword.getText().trim());
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data pemilik berhasil ditambahkan!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan (username mungkin sudah dipakai): " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data yang ingin diubah terlebih dahulu!");
            return;
        }
        if (!validasiUpdate()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdatePemilik(?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());
            cs.setString(4, txtEmail.getText().trim());
            cs.setString(5, txtAlamat.getText().trim());
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data pemilik berhasil diubah!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data yang ingin dihapus terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtNama,
                "Yakin ingin menghapus pemilik " + txtNama.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeletePemilik(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data pemilik berhasil dihapus!",
                                () -> {
                                    setClose();
                                    loadTable();
                                });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menghapus (mungkin masih ada data Villa terkait): " + e.getMessage());
                    } finally {
                        try { k.conn.close(); } catch (Exception ignored) {}
                    }
                });
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    // ===========================================================
    // FORM HELPERS
    // ===========================================================
    private void populateForm(Pemilik p) {
        txtId.setText(p.getIdPemilik());
        txtNama.setText(p.getNama());
        txtNoTelp.setText(p.getNoTelp());
        txtEmail.setText(p.getEmail());
        txtAlamat.setText(p.getAlamat());
        txtUsername.setText(p.getUsername());
        txtPassword.clear();

        // Username & password tidak ikut diubah lewat sp_UpdatePemilik
        txtUsername.setDisable(true);
        txtPassword.setDisable(true);

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
    }

    private void setClose() {
        txtId.clear();
        txtNama.clear();
        txtNoTelp.clear();
        txtEmail.clear();
        txtAlamat.clear();
        txtUsername.clear();
        txtPassword.clear();

        txtUsername.setDisable(false);
        txtPassword.setDisable(false);

        tablePemilik.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                txtUsername.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private boolean validasiUpdate() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    // ===========================================================
    // UTIL
    // ===========================================================
    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtNama, type, msg);
    }
}