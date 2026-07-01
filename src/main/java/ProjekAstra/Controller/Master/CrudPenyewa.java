package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Penyewa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CrudPenyewa implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtUmur, txtAlamat, txtUsername, txtCari;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpTglLahir;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Penyewa> tablePenyewa;
    @FXML private TableColumn<Penyewa, String> colId, colNama, colNoTelp, colAlamat, colUsername;
    @FXML private TableColumn<Penyewa, Integer> colUmur;
    @FXML private TableColumn<Penyewa, LocalDate> colTglLahir;

    private final ObservableList<Penyewa> listPenyewa = FXCollections.observableArrayList();

    // ===========================================================
    // INIT
    // ===========================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTable();
        setClose();

        tablePenyewa.setOnMouseClicked(e -> {
            Penyewa p = tablePenyewa.getSelectionModel().getSelectedItem();
            if (p != null) populateForm(p);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariPenyewa(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPenyewa"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNoTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));
        colUmur.setCellValueFactory(new PropertyValueFactory<>("umur"));
        colTglLahir.setCellValueFactory(new PropertyValueFactory<>("tglLahir"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    // ===========================================================
    // LOAD & SEARCH
    // ===========================================================
    private void loadTable() {
        listPenyewa.clear();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllPenyewa}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                Date tgl = rs.getDate("TglLahir");
                listPenyewa.add(new Penyewa(
                        rs.getString("IdPenyewa"),
                        rs.getString("Nama"),
                        rs.getString("NoTelp"),
                        rs.getInt("Umur"),
                        tgl != null ? tgl.toLocalDate() : null,
                        rs.getString("Alamat"),
                        rs.getString("Username")
                ));
            }
            tablePenyewa.setItems(listPenyewa);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariPenyewa(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tablePenyewa.setItems(listPenyewa);
            return;
        }
        ObservableList<Penyewa> hasil = FXCollections.observableArrayList();
        for (Penyewa p : listPenyewa) {
            if (p.getNama().toLowerCase().contains(keyword.toLowerCase()) ||
                    p.getIdPenyewa().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(p);
            }
        }
        tablePenyewa.setItems(hasil);
    }

    // ===========================================================
    // CRUD ACTIONS
    // ===========================================================
    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPenyewa(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setInt(3, Integer.parseInt(txtUmur.getText().trim()));
            cs.setDate(4, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(5, txtAlamat.getText().trim());
            cs.setString(6, txtUsername.getText().trim());
            cs.setString(7, txtPassword.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data penyewa berhasil ditambahkan!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Umur harus berupa angka!");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal menyimpan (username mungkin sudah dipakai): " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Pilih data yang ingin diubah terlebih dahulu!");
            return;
        }
        if (!validasiUpdate()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdatePenyewa(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());
            cs.setInt(4, Integer.parseInt(txtUmur.getText().trim()));
            cs.setDate(5, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data penyewa berhasil diubah!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Umur harus berupa angka!");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Pilih data yang ingin dihapus terlebih dahulu!");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Yakin ingin menghapus penyewa " + txtNama.getText() + "?");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_DeletePenyewa(?)}");
            cs.setString(1, txtId.getText());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data penyewa berhasil dihapus!");
            setClose();
            loadTable();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal menghapus: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    // ===========================================================
    // FORM HELPERS
    // ===========================================================
    private void populateForm(Penyewa p) {
        txtId.setText(p.getIdPenyewa());
        txtNama.setText(p.getNama());
        txtNoTelp.setText(p.getNoTelp());
        txtUmur.setText(String.valueOf(p.getUmur()));
        dpTglLahir.setValue(p.getTglLahir());
        txtAlamat.setText(p.getAlamat());
        txtUsername.setText(p.getUsername());
        txtPassword.clear();

        // Username & password tidak ikut diubah lewat sp_UpdatePenyewa
        txtUsername.setDisable(true);
        txtPassword.setDisable(true);
    }

    private void setClose() {
        txtId.clear();
        txtNama.clear();
        txtNoTelp.clear();
        txtUmur.clear();
        txtAlamat.clear();
        txtUsername.clear();
        txtPassword.clear();
        dpTglLahir.setValue(null);

        txtUsername.setDisable(false);
        txtPassword.setDisable(false);

        tablePenyewa.getSelectionModel().clearSelection();
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || dpTglLahir.getValue() == null ||
                txtAlamat.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() ||
                txtPassword.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private boolean validasiUpdate() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || dpTglLahir.getValue() == null ||
                txtAlamat.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    // ===========================================================
    // UTIL
    // ===========================================================
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}