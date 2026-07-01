package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Karyawan;
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

public class CrudKaryawan implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtUmur, txtAlamat, txtUsername, txtCari;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpTanggalMasuk;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Karyawan> tableKaryawan;
    @FXML private TableColumn<Karyawan, String> colId, colNama, colNoTelp, colAlamat, colUsername, colStatus;
    @FXML private TableColumn<Karyawan, Integer> colUmur;
    @FXML private TableColumn<Karyawan, LocalDate> colTglMasuk;

    private final ObservableList<Karyawan> listKaryawan = FXCollections.observableArrayList();

    // ===========================================================
    // INIT
    // ===========================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatus.getItems().addAll("AKTIF", "TIDAK AKTIF");

        setupTable();
        loadTable();
        setClose();

        tableKaryawan.setOnMouseClicked(e -> {
            Karyawan k = tableKaryawan.getSelectionModel().getSelectedItem();
            if (k != null) populateForm(k);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariKaryawan(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idKaryawan"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNoTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));
        colUmur.setCellValueFactory(new PropertyValueFactory<>("umur"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        colTglMasuk.setCellValueFactory(new PropertyValueFactory<>("tanggalMasuk"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ===========================================================
    // LOAD & SEARCH
    // ===========================================================
    private void loadTable() {
        listKaryawan.clear();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllKaryawan}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                Date tgl = rs.getDate("TanggalMasuk");
                listKaryawan.add(new Karyawan(
                        rs.getString("IdKaryawan"),
                        rs.getString("NamaKaryawan"),
                        rs.getString("NoTelp"),
                        rs.getString("Alamat"),
                        rs.getInt("Umur"),
                        rs.getString("Username"),
                        tgl != null ? tgl.toLocalDate() : null,
                        rs.getString("Status")
                ));
            }
            tableKaryawan.setItems(listKaryawan);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariKaryawan(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableKaryawan.setItems(listKaryawan);
            return;
        }
        ObservableList<Karyawan> hasil = FXCollections.observableArrayList();
        for (Karyawan k : listKaryawan) {
            if (k.getNama().toLowerCase().contains(keyword.toLowerCase()) ||
                    k.getIdKaryawan().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(k);
            }
        }
        tableKaryawan.setItems(hasil);
    }

    // ===========================================================
    // CRUD ACTIONS
    // ===========================================================
    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertKaryawan(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setString(3, txtAlamat.getText().trim());
            cs.setInt(4, Integer.parseInt(txtUmur.getText().trim()));
            cs.setString(5, txtUsername.getText().trim());
            cs.setString(6, txtPassword.getText().trim());
            cs.setDate(7, Date.valueOf(dpTanggalMasuk.getValue()));
            cs.setString(8, cbStatus.getValue());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data karyawan berhasil ditambahkan!");
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateKaryawan(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());
            cs.setString(4, txtAlamat.getText().trim());
            cs.setInt(5, Integer.parseInt(txtUmur.getText().trim()));
            cs.setString(6, cbStatus.getValue());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data karyawan berhasil diubah!");
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
        konfirmasi.setContentText("Yakin ingin menghapus karyawan " + txtNama.getText() + "?");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_DeleteKaryawan(?)}");
            cs.setString(1, txtId.getText());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Data karyawan berhasil dihapus!");
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
    private void populateForm(Karyawan k) {
        txtId.setText(k.getIdKaryawan());
        txtNama.setText(k.getNama());
        txtNoTelp.setText(k.getNoTelp());
        txtUmur.setText(String.valueOf(k.getUmur()));
        txtAlamat.setText(k.getAlamat());
        dpTanggalMasuk.setValue(k.getTanggalMasuk());
        txtUsername.setText(k.getUsername());
        txtPassword.clear();
        cbStatus.setValue(k.getStatus());

        // Username, password, dan tanggal masuk tidak ikut diubah lewat sp_UpdateKaryawan
        txtUsername.setDisable(true);
        txtPassword.setDisable(true);
        dpTanggalMasuk.setDisable(true);
    }

    private void setClose() {
        txtId.clear();
        txtNama.clear();
        txtNoTelp.clear();
        txtUmur.clear();
        txtAlamat.clear();
        txtUsername.clear();
        txtPassword.clear();
        dpTanggalMasuk.setValue(null);
        cbStatus.setValue(null);

        txtUsername.setDisable(false);
        txtPassword.setDisable(false);
        dpTanggalMasuk.setDisable(false);

        tableKaryawan.getSelectionModel().clearSelection();
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                dpTanggalMasuk.getValue() == null || cbStatus.getValue() == null ||
                txtUsername.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private boolean validasiUpdate() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                cbStatus.getValue() == null) {
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