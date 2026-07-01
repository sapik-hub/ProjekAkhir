package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Fasilitas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class CrudFasilitas implements Initializable {

    @FXML private TextField txtId, txtNamaFasilitas, txtJumlah, txtCari;
    @FXML private TextArea txtDeskripsi;
    @FXML private ComboBox<String> cbVilla;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    @FXML private TableView<Fasilitas> tableFasilitas;
    @FXML private TableColumn<Fasilitas, String> colId, colVilla, colNamaFasilitas, colDeskripsi;
    @FXML private TableColumn<Fasilitas, Integer> colJumlah;

    private final ObservableList<Fasilitas> listFasilitas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadComboVilla();
        loadTable();
        setClose();

        tableFasilitas.setOnMouseClicked(e -> {
            Fasilitas f = tableFasilitas.getSelectionModel().getSelectedItem();
            if (f != null) populateForm(f);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariFasilitas(newVal));
        generateIdVilla();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFasilitas"));
        colVilla.setCellValueFactory(new PropertyValueFactory<>("namaVilla"));
        colNamaFasilitas.setCellValueFactory(new PropertyValueFactory<>("namaFasilitas"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
    }

    // ===== Combo Villa (format: "VLA0001 - Villa Bukit Indah") =====
    private void loadComboVilla() {
        cbVilla.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbVilla.getItems().add(rs.getString("IdVilla") + " - " + rs.getString("NamaVilla"));
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void loadTable() {
        listFasilitas.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllFasilitas}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                listFasilitas.add(new Fasilitas(
                        rs.getString("IdFasilitas"),
                        rs.getString("NamaVilla"),
                        rs.getString("NamaFasilitas"),
                        rs.getInt("Jumlah"),
                        rs.getString("Deskripsi")
                ));
            }
            tableFasilitas.setItems(listFasilitas);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariFasilitas(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableFasilitas.setItems(listFasilitas);
            return;
        }
        ObservableList<Fasilitas> hasil = FXCollections.observableArrayList();
        for (Fasilitas f : listFasilitas) {
            if (f.getNamaFasilitas().toLowerCase().contains(keyword.toLowerCase()) ||
                    f.getIdFasilitas().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(f);
            }
        }
        tableFasilitas.setItems(hasil);
    }

    @FXML
    private void handleSimpan() {
        if (!validasi()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertFasilitas(?, ?, ?, ?)}");
            cs.setString(1, getIdFromCombo(cbVilla.getValue()));
            cs.setString(2, txtNamaFasilitas.getText().trim());
            cs.setInt(3, Integer.parseInt(txtJumlah.getText().trim()));
            cs.setString(4, txtDeskripsi.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Fasilitas berhasil ditambahkan!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Jumlah harus berupa angka!");
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
        if (!validasi()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateFasilitas(?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNamaFasilitas.getText().trim());
            cs.setInt(3, Integer.parseInt(txtJumlah.getText().trim()));
            cs.setString(4, txtDeskripsi.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Fasilitas berhasil diubah!");
            setClose();
            loadTable();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Jumlah harus berupa angka!");
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
        konfirmasi.setContentText("Yakin ingin menghapus fasilitas " + txtNamaFasilitas.getText() + "?");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_DeleteFasilitas(?)}");
            cs.setString(1, txtId.getText());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Fasilitas berhasil dihapus!");
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

    // Villa di table cuma kasih NAMA, jadi pas edit kita disable combo Villa
    // (karena sp_UpdateFasilitas juga gak nerima ganti Villa) dan cuma tampilin info-nya.
    private void populateForm(Fasilitas f) {
        txtId.setText(f.getIdFasilitas());
        txtNamaFasilitas.setText(f.getNamaFasilitas());
        txtJumlah.setText(String.valueOf(f.getJumlah()));
        txtDeskripsi.setText(f.getDeskripsi());

        selectComboByName(cbVilla, f.getNamaVilla());
        cbVilla.setDisable(true);
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
        txtNamaFasilitas.clear();
        txtJumlah.clear();
        txtDeskripsi.clear();
        cbVilla.setValue(null);
        cbVilla.setDisable(false);
        tableFasilitas.getSelectionModel().clearSelection();
    }

    private boolean validasi() {
        if (cbVilla.getValue() == null || txtNamaFasilitas.getText().trim().isEmpty() ||
                txtJumlah.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Villa, Nama Fasilitas, dan Jumlah wajib diisi!");
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
            String sql = "SELECT dbo.fnNextIdFasilitas() AS IdFasilitas";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("IdFasilitas"));
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