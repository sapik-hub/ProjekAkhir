package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Penyewa;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import java.sql.PreparedStatement;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CrudPenyewa implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtNikKtp, txtAlamat, txtUsername, txtCari;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpTglLahir;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Penyewa> tablePenyewa;
    @FXML private TableColumn<Penyewa, String> colId, colNama, colNoTelp, colNikKtp, colAlamat, colUsername;
    @FXML private TableColumn<Penyewa, LocalDate> colTglLahir;

    private final ObservableList<Penyewa> listPenyewa = FXCollections.observableArrayList();

    // ===========================================================
    // INIT
    // ===========================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTable();
        setClose();   // ini udah manggil generateIdPenyewa() di dalamnya, jadi otomatis kepanggil

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
        colNikKtp.setCellValueFactory(new PropertyValueFactory<>("nikKtp"));
        colTglLahir.setCellValueFactory(new PropertyValueFactory<>("tglLahir"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    // Batasi input NIK cuma angka
    @FXML
    private void handleNikOnlyNumber(KeyEvent event) {
        String karakter = event.getCharacter();
        if (!karakter.matches("[0-9]")) {
            event.consume();
        }
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
                        rs.getString("NoKtp"),
                        tgl != null ? tgl.toLocalDate() : null,
                        rs.getString("Alamat"),
                        rs.getString("Username")
                ));
            }
            tablePenyewa.setItems(listPenyewa);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data: " + e.getMessage());
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
            // Urutan HARUS sama dengan sp_InsertPenyewa: Nama, NoTelp, NikKtp, TglLahir, Alamat, Username, Password
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPenyewa(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setString(3, txtNikKtp.getText().trim());
            cs.setDate(4, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(5, txtAlamat.getText().trim());
            cs.setString(6, txtUsername.getText().trim());
            cs.setString(7, txtPassword.getText().trim());
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil ditambahkan!",
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
            // Urutan HARUS sama dengan sp_UpdatePenyewa: IdPenyewa, Nama, NoTelp, TglLahir, Alamat
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdatePenyewa(?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());
            cs.setDate(4, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(5, txtAlamat.getText().trim());
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil diubah!",
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
                "Yakin ingin menghapus penyewa " + txtNama.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeletePenyewa(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil dihapus!",
                                () -> {
                                    setClose();
                                    loadTable();
                                });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menghapus: " + e.getMessage());
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
    private void populateForm(Penyewa p) {
        txtId.setText(p.getIdPenyewa());
        txtNama.setText(p.getNama());
        txtNoTelp.setText(p.getNoTelp());
        txtNikKtp.setText(p.getNikKtp());
        dpTglLahir.setValue(p.getTglLahir());
        txtAlamat.setText(p.getAlamat());
        txtUsername.setText(p.getUsername());
        txtPassword.clear();

        // NIK, Username & password tidak ikut diubah lewat sp_UpdatePenyewa
        txtNikKtp.setDisable(true);
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
        txtNikKtp.clear();
        txtAlamat.clear();
        txtUsername.clear();
        txtPassword.clear();
        dpTglLahir.setValue(null);

        txtNikKtp.setDisable(false);
        txtUsername.setDisable(false);
        txtPassword.setDisable(false);

        tablePenyewa.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);

        generateIdPenyewa();
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    // - Nama    : wajib diisi, tidak boleh angka, tidak boleh simbol (huruf & spasi saja)
    // - NoTelp  : wajib diisi, hanya boleh angka (tidak boleh huruf/simbol)
    // - NikKtp  : wajib diisi, harus 16 digit angka (hanya divalidasi saat insert)
    // - Alamat  : wajib diisi, tidak boleh angka, tidak boleh simbol seperti @#$%^&*!
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtNikKtp.getText().trim().isEmpty() || dpTglLahir.getValue() == null ||
                txtAlamat.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() ||
                txtPassword.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }

        if (!txtNikKtp.getText().trim().matches("\\d{16}")) {
            notif(NotifUtil.Type.WARNING, "NIK KTP harus terdiri dari 16 digit angka!");
            return false;
        }

        return validasiFormat();
    }

    private boolean validasiUpdate() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                dpTglLahir.getValue() == null || txtAlamat.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return validasiFormat();
    }

    // Validasi format field yang sama-sama dipakai insert & update
    private boolean validasiFormat() {
        String nama = txtNama.getText().trim();
        String noTelp = txtNoTelp.getText().trim();
        String alamat = txtAlamat.getText().trim();

        // Nama: hanya huruf dan spasi, tidak boleh angka/simbol
        if (!nama.matches("^[a-zA-Z\\s]+$")) {
            notif(NotifUtil.Type.WARNING, "Nama tidak boleh mengandung angka atau simbol!");
            return false;
        }

        // No Telp: hanya angka, tidak boleh huruf/simbol
        if (!noTelp.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "No Telp hanya boleh berisi angka!");
            return false;
        }

        // Alamat: tidak boleh angka dan tidak boleh simbol seperti @#$%^&*!
        if (!alamat.matches("^[a-zA-Z\\s]+$")) {
            notif(NotifUtil.Type.WARNING, "Alamat tidak boleh mengandung angka atau simbol!");
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

    public void generateIdPenyewa() {
        Koneksi k = new Koneksi();
        try {
            String sql = "SELECT dbo.fnNextIdPenyewa() AS IdPenyewa";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("IdPenyewa"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }
}