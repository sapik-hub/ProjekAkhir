package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Fasilitas;
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

public class CrudFasilitas implements Initializable {

    @FXML private TextField txtId, txtNamaFasilitas, txtJumlah, txtCari;
    @FXML private TextArea txtDeskripsi;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    @FXML private TableView<Fasilitas> tableFasilitas;
    @FXML private TableColumn<Fasilitas, String> colId, colNamaFasilitas, colDeskripsi;
    @FXML private TableColumn<Fasilitas, Integer> colJumlah;

    private final ObservableList<Fasilitas> listFasilitas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTable();
        setClose();

        tableFasilitas.setOnMouseClicked(e -> {
            Fasilitas f = tableFasilitas.getSelectionModel().getSelectedItem();
            if (f != null) populateForm(f);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariFasilitas(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFasilitas"));
        colNamaFasilitas.setCellValueFactory(new PropertyValueFactory<>("namaFasilitas"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
    }

    private void loadTable() {
        listFasilitas.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllFasilitas}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                listFasilitas.add(new Fasilitas(
                        rs.getString("Id_Fasilitas"),
                        rs.getString("Nama_Fasilitas"),
                        rs.getInt("Jumlah"),
                        rs.getString("Deskripsi"),
                        rs.getString("Status")
                ));
            }
            tableFasilitas.setItems(listFasilitas);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data: " + e.getMessage());
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
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertFasilitas(?, ?, ?)}");
            cs.setString(1, txtNamaFasilitas.getText().trim());
            cs.setInt(2, Integer.parseInt(txtJumlah.getText().trim()));
            cs.setString(3, txtDeskripsi.getText().trim());
            cs.execute();

            NotifUtil.show(txtNamaFasilitas, NotifUtil.Type.SUCCESS, "Fasilitas berhasil ditambahkan!",
                    () -> {
                        setClose();
                        loadTable();
                    });
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
        if (!validasi()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateFasilitas(?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNamaFasilitas.getText().trim());
            cs.setInt(3, Integer.parseInt(txtJumlah.getText().trim()));
            cs.setString(4, txtDeskripsi.getText().trim());
            cs.execute();

            NotifUtil.show(txtNamaFasilitas, NotifUtil.Type.SUCCESS, "Fasilitas berhasil diubah!",
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

        ConfirmUtil.show(txtNamaFasilitas,
                "Yakin ingin menghapus fasilitas " + txtNamaFasilitas.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeleteFasilitas(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNamaFasilitas, NotifUtil.Type.SUCCESS, "Fasilitas berhasil dihapus!",
                                () -> {
                                    setClose();
                                    loadTable();
                                });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menghapus (mungkin masih dipakai di beberapa villa): " + e.getMessage());
                    } finally {
                        try { k.conn.close(); } catch (Exception ignored) {}
                    }
                });
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    private void populateForm(Fasilitas f) {
        txtId.setText(f.getIdFasilitas());
        txtNamaFasilitas.setText(f.getNamaFasilitas());
        txtJumlah.setText(String.valueOf(f.getJumlah()));
        txtDeskripsi.setText(f.getDeskripsi());

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
    }

    private void setClose() {
        txtId.clear();
        txtNamaFasilitas.clear();
        txtJumlah.clear();
        txtDeskripsi.clear();
        tableFasilitas.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
    }

    // Validasi: Nama wajib diisi tanpa angka, Jumlah wajib angka >= 0
    private boolean validasi() {
        String nama = txtNamaFasilitas.getText().trim();
        if (nama.isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Nama Fasilitas wajib diisi!");
            return false;
        }
        if (!nama.matches("^[^0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Nama Fasilitas tidak boleh mengandung angka!");
            return false;
        }
        if (txtJumlah.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Jumlah wajib diisi!");
            return false;
        }
        try {
            int jumlah = Integer.parseInt(txtJumlah.getText().trim());
            if (jumlah < 0) {
                notif(NotifUtil.Type.WARNING, "Jumlah tidak boleh negatif!");
                return false;
            }
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Jumlah harus berupa angka!");
            return false;
        }
        return true;
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtNamaFasilitas, type, msg);
    }
}