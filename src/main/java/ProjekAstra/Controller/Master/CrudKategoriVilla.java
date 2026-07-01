package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.KategoriVilla;
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

public class CrudKategoriVilla implements Initializable {

    @FXML private TextField txtId, txtNamaKategori, txtCari;
    @FXML private TextArea txtDeskripsi;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    @FXML private TableView<KategoriVilla> tableKategori;
    @FXML private TableColumn<KategoriVilla, String> colId, colNamaKategori, colDeskripsi;

    private final ObservableList<KategoriVilla> listKategori = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTable();
        setClose();

        tableKategori.setOnMouseClicked(e -> {
            KategoriVilla k = tableKategori.getSelectionModel().getSelectedItem();
            if (k != null) populateForm(k);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariKategori(newVal));
        generateIdVilla();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colNamaKategori.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
    }

    private void loadTable() {
        listKategori.clear();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllKategori}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                listKategori.add(new KategoriVilla(
                        rs.getString("IdKategori"),
                        rs.getString("NamaKategori"),
                        rs.getString("Deskripsi")
                ));
            }
            tableKategori.setItems(listKategori);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariKategori(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableKategori.setItems(listKategori);
            return;
        }
        ObservableList<KategoriVilla> hasil = FXCollections.observableArrayList();
        for (KategoriVilla k : listKategori) {
            if (k.getNamaKategori().toLowerCase().contains(keyword.toLowerCase()) ||
                    k.getIdKategori().toLowerCase().contains(keyword.toLowerCase())) {
                hasil.add(k);
            }
        }
        tableKategori.setItems(hasil);
    }

    @FXML
    private void handleSimpan() {
        if (!validasi()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertKategori(?, ?)}");
            cs.setString(1, txtNamaKategori.getText().trim());
            cs.setString(2, txtDeskripsi.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Kategori berhasil ditambahkan!");
            setClose();
            loadTable();
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateKategori(?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNamaKategori.getText().trim());
            cs.setString(3, txtDeskripsi.getText().trim());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Kategori berhasil diubah!");
            setClose();
            loadTable();
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
        konfirmasi.setContentText("Yakin ingin menghapus kategori " + txtNamaKategori.getText() + "?");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_DeleteKategori(?)}");
            cs.setString(1, txtId.getText());
            cs.execute();

            alert(Alert.AlertType.INFORMATION, "Kategori berhasil dihapus!");
            setClose();
            loadTable();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Gagal menghapus (mungkin masih dipakai oleh data Villa): " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    private void populateForm(KategoriVilla k) {
        txtId.setText(k.getIdKategori());
        txtNamaKategori.setText(k.getNamaKategori());
        txtDeskripsi.setText(k.getDeskripsi());
    }

    private void setClose() {
        txtId.clear();
        txtNamaKategori.clear();
        txtDeskripsi.clear();
        tableKategori.getSelectionModel().clearSelection();
    }

    private boolean validasi() {
        if (txtNamaKategori.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Nama kategori wajib diisi!");
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
            String sql = "SELECT dbo.fnNextIdKategori() AS IdKategori";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("IdKategori"));
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