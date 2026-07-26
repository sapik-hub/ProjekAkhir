package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.DetailFasilitas;
import ProjekAstra.Util.NotifUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class DialogTambahFasilitas implements Initializable {

    @FXML private ComboBox<DetailFasilitas> cbFasilitas;
    @FXML private TextField txtJumlahFasilitas;
    @FXML private Button btnSimpanFasilitas;

    private String idVilla;
    private boolean berhasilDitambahkan = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComboFasilitas();
    }

    // Dipanggil dari CrudVilla sebelum dialog ditampilkan
    public void setIdVilla(String idVilla) {
        this.idVilla = idVilla;
    }

    public boolean isBerhasilDitambahkan() {
        return berhasilDitambahkan;
    }

    private void loadComboFasilitas() {
        cbFasilitas.setItems(FXCollections.observableArrayList());
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllFasilitas}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbFasilitas.getItems().add(new DetailFasilitas(
                        rs.getString("Id_Fasilitas"),
                        rs.getString("Nama_Fasilitas")
                ));
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data fasilitas: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleSimpan() {
        if (cbFasilitas.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Pilih fasilitas yang ingin ditambahkan!");
            return;
        }
        String jumlahStr = txtJumlahFasilitas.getText().trim();
        if (!jumlahStr.matches("^[1-9][0-9]*$")) {
            notif(NotifUtil.Type.WARNING, "Jumlah fasilitas harus berupa angka lebih dari 0!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_TambahFasilitasVilla(?, ?, ?)}");
            cs.setString(1, idVilla);
            cs.setString(2, cbFasilitas.getValue().getIdFasilitas());
            cs.setInt(3, Integer.parseInt(jumlahStr));
            cs.execute();

            berhasilDitambahkan = true;
            tutupDialog();
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menambah fasilitas: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleBatal() {
        berhasilDitambahkan = false;
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) btnSimpanFasilitas.getScene().getWindow();
        stage.close();
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtJumlahFasilitas, type, msg);
    }
}