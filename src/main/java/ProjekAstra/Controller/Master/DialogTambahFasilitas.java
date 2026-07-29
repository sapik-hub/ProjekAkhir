package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Fasilitas;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.PopupUtil; // Import PopupUtil
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    @FXML private ComboBox<Fasilitas> cbFasilitas;
    @FXML private TextField txtQty;
    @FXML private Button btnSimpanFasilitas;
    @FXML private Button btnBatal;

    private String idVilla;
    private boolean berhasilDitambahkan = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComboFasilitas();

        // Set listener untuk auto-close dengan escape key atau enter
        txtQty.setOnAction(e -> handleSimpan());
    }

    public void setIdVilla(String idVilla) {
        this.idVilla = idVilla;
    }

    public boolean isBerhasilDitambahkan() {
        return berhasilDitambahkan;
    }

    private void loadComboFasilitas() {
        ObservableList<Fasilitas> listFasilitas = FXCollections.observableArrayList();
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
            cbFasilitas.setItems(listFasilitas);

            // Gunakan display text yang lebih informatif
            cbFasilitas.setConverter(new javafx.util.StringConverter<Fasilitas>() {
                @Override
                public String toString(Fasilitas fasilitas) {
                    return fasilitas != null ? fasilitas.getNamaFasilitas() : "";
                }

                @Override
                public Fasilitas fromString(String string) {
                    return null;
                }
            });

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

        String qtyStr = txtQty.getText().trim();
        if (qtyStr.isEmpty() || !qtyStr.matches("^[1-9][0-9]*$")) {
            notif(NotifUtil.Type.WARNING, "Jumlah fasilitas harus berupa angka lebih dari 0!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_TambahFasilitasVilla(?, ?, ?)}");
            cs.setString(1, idVilla);
            cs.setString(2, cbFasilitas.getValue().getIdFasilitas());
            cs.setInt(3, Integer.parseInt(qtyStr));
            cs.execute();

            berhasilDitambahkan = true;
            NotifUtil.show(txtQty, NotifUtil.Type.SUCCESS, "Fasilitas berhasil ditambahkan!", 1.5, this::tutupDialog);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Fasilitas ini sudah ditambahkan")) {
                notif(NotifUtil.Type.WARNING, "Fasilitas ini sudah ada di villa ini!");
            } else {
                notif(NotifUtil.Type.ERROR, "Gagal menambah fasilitas: " + errorMsg);
            }
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
        // Tutup menggunakan PopupUtil jika ada, atau gunakan stage
        Stage stage = (Stage) btnSimpanFasilitas.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtQty, type, msg);
    }
}