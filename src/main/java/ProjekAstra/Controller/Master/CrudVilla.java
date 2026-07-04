package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Villa;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
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
    @FXML private TextField txtPemilik; // read-only, isinya nama pemilik yang login
    @FXML private ComboBox<String> cbKategori, cbStatus;
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
        loadComboKategori();
        loadTable();
        setClose();

        tableVilla.setOnMouseClicked(e -> {
            Villa v = tableVilla.getSelectionModel().getSelectedItem();
            if (v != null) populateForm(v);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariVilla(newVal));
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
            notif(NotifUtil.Type.ERROR, "Gagal memuat data kategori: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // Cuma nampilin villa milik Pemilik yang sedang login
    private void loadTable() {
        listVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetVillaByPemilik(?)}");
            cs.setString(1, Session.getIdPemilik());
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
            notif(NotifUtil.Type.ERROR, "Gagal memuat data: " + e.getMessage());
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
            cs.setString(1, Session.getIdPemilik());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(txtHarga.getText().trim()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, cbStatus.getValue());
            cs.execute();

            NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Villa berhasil ditambahkan!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Kapasitas dan Harga harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateVilla(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(txtHarga.getText().trim()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, cbStatus.getValue());
            cs.execute();

            NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Villa berhasil diubah!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Kapasitas dan Harga harus berupa angka!");
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

        ConfirmUtil.show(txtNamaVilla,
                "Yakin ingin menghapus villa " + txtNamaVilla.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeleteVilla(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Villa berhasil dihapus!",
                                () -> {
                                    setClose();
                                    loadTable();
                                });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menghapus (mungkin masih ada data Fasilitas/Booking terkait): " + e.getMessage());
                    } finally {
                        try { k.conn.close(); } catch (Exception ignored) {}
                    }
                });
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    private void populateForm(Villa v) {
        txtId.setText(v.getIdVilla());
        txtNamaVilla.setText(v.getNamaVilla());
        txtKapasitas.setText(String.valueOf(v.getKapasitas()));
        txtHarga.setText(v.getHarga().toPlainString());
        txtAlamat.setText(v.getAlamatVilla());
        cbStatus.setValue(v.getStatus());

        selectComboByName(cbKategori, v.getNamaKategori());

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
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
        cbKategori.setValue(null);
        cbStatus.setValue(null);
        tableVilla.getSelectionModel().clearSelection();

        if (txtPemilik != null) {
            txtPemilik.setText(Session.getNamaPemilik());
        }

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
        generateIdVilla();
    }

    private boolean validasiInsert() {
        if (cbKategori.getValue() == null ||
                txtNamaVilla.getText().trim().isEmpty() || txtKapasitas.getText().trim().isEmpty() ||
                txtHarga.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                cbStatus.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return true;
    }

    private boolean validasiUpdate() {
        return validasiInsert();
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtNamaVilla, type, msg);
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
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }
}