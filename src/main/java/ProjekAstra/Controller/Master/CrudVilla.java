package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Villa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class CrudVilla implements Initializable {

    @FXML private TextField txtId, txtNamaVilla, txtKapasitas, txtHarga, txtAlamat, txtCari;
    @FXML private ComboBox<String> cbPemilik, cbKategori, cbStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    @FXML private TableView<Villa> tableVilla;
    @FXML private TableColumn<Villa, String> colId, colPemilik, colKategori, colNamaVilla, colAlamat, colStatus;
    @FXML private TableColumn<Villa, Integer> colKapasitas;
    @FXML private TableColumn<Villa, BigDecimal> colHarga;

    private final ObservableList<Villa> listVilla = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatus.getItems().addAll("Tersedia", "Tidak Tersedia", "Maintenance");

        setupTable();
        loadComboPemilik();
        loadComboKategori();
        loadTable();
        setClose();

        tableVilla.setOnMouseClicked(e -> {
            Villa v = tableVilla.getSelectionModel().getSelectedItem();
            if (v != null) populateForm(v);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariVilla(newVal));
        generateIdVilla();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idVilla"));
        colPemilik.setCellValueFactory(new PropertyValueFactory<>("namaPemilik"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        colNamaVilla.setCellValueFactory(new PropertyValueFactory<>("namaVilla"));
        colKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamatVilla"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ===== Combo Pemilik (format: "PMK0001 - Budi Santoso") =====
    private void loadComboPemilik() {
        cbPemilik.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllPemilik}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbPemilik.getItems().add(rs.getString("IdPemilik") + " - " + rs.getString("Nama"));
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data pemilik: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Combo Kategori (format: "KAT0001 - Villa Mewah") =====
    private void loadComboKategori() {
        cbKategori.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllKategori}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbKategori.getItems().add(rs.getString("IdKategori") + " - " + rs.getString("NamaKategori"));
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data kategori: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void loadTable() {
        listVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                listVilla.add(new Villa(
                        rs.getString("IdVilla"),
                        rs.getString("NamaPemilik"),
                        rs.getString("NamaKategori"),
                        rs.getString("NamaVilla"),
                        rs.getInt("Kapasitas"),
                        rs.getBigDecimal("Harga"),
                        rs.getString("AlamatVilla"),
                        rs.getString("Status")
                ));
            }
            tableVilla.setItems(listVilla);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariVilla(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableVilla.setItems(listVilla);
            return;
        }
        ObservableList<Villa> hasil = FXCollections.observableArrayList();
        for (Villa v : listVilla) {
            if (v.getNamaVilla().toLowerCase().contains(keyword.toLowerCase()) ||
                    v.getIdVilla().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(v);
            }
        }
        tableVilla.setItems(hasil);
    }

    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertVilla(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, getIdFromCombo(cbPemilik.getValue()));
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(txtHarga.getText().trim()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, cbStatus.getValue());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Villa berhasil ditambahkan!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Kapasitas dan Harga harus berupa angka!");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal menyimpan: " + e.getMessage());
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateVilla(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(txtHarga.getText().trim()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, cbStatus.getValue());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Villa berhasil diubah!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Kapasitas dan Harga harus berupa angka!");
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
        konfirmasi.setContentText("Yakin ingin menghapus villa " + txtNamaVilla.getText() + "?");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_DeleteVilla(?)}");
            cs.setString(1, txtId.getText());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Villa berhasil dihapus!");
            setClose();
            loadTable();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal menghapus (mungkin masih ada data Fasilitas/Booking terkait): " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    // Catatan: Pemilik & Kategori di table cuma kasih NAMA, jadi pas edit kita gak bisa
    // langsung tau ID-nya. Pemilik tetap ditampilkan (read-only) karena gak boleh diubah,
    // sementara Kategori dicari ID-nya berdasarkan kecocokan nama di daftar combo.
    private void populateForm(Villa v) {
        txtId.setText(v.getIdVilla());
        txtNamaVilla.setText(v.getNamaVilla());
        txtKapasitas.setText(String.valueOf(v.getKapasitas()));
        txtHarga.setText(v.getHarga().toPlainString());
        txtAlamat.setText(v.getAlamatVilla());
        cbStatus.setValue(v.getStatus());

        selectComboByName(cbPemilik, v.getNamaPemilik());
        selectComboByName(cbKategori, v.getNamaKategori());

        // Pemilik villa tidak ikut diubah lewat sp_UpdateVilla
        cbPemilik.setDisable(true);
    }

    private void selectComboByName(ComboBox<String> combo, String nama) {
        for (String item : combo.getItems()) {
            if (item.endsWith(" - " + nama)) {
                combo.setValue(item);
                return;
            }
        }
    }

    private String getIdFromCombo(String comboValue) {
        if (comboValue == null) return null;
        return comboValue.split(" - ")[0];
    }

    private void setClose() {
        txtId.clear();
        txtNamaVilla.clear();
        txtKapasitas.clear();
        txtHarga.clear();
        txtAlamat.clear();
        cbPemilik.setValue(null);
        cbKategori.setValue(null);
        cbStatus.setValue(null);
        cbPemilik.setDisable(false);
        tableVilla.getSelectionModel().clearSelection();
    }

    private boolean validasiInsert() {
        if (cbPemilik.getValue() == null || cbKategori.getValue() == null ||
                txtNamaVilla.getText().trim().isEmpty() || txtKapasitas.getText().trim().isEmpty() ||
                txtHarga.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                cbStatus.getValue() == null) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private boolean validasiUpdate() {
        if (cbKategori.getValue() == null ||
                txtNamaVilla.getText().trim().isEmpty() || txtKapasitas.getText().trim().isEmpty() ||
                txtHarga.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                cbStatus.getValue() == null) {
            alert(Alert.AlertType.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    public void generateIdVilla() {
        Koneksi k = new Koneksi();

        try {
            String sql = "SELECT dbo.fnNextIdVilla() AS IdVilla";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("IdVilla"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                k.conn.close();
            } catch (Exception ex) {
            }
        }
    }
}