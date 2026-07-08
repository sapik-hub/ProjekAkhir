package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Karyawan;
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
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CrudKaryawan implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtUmur, txtAlamat, txtUsername, txtCari;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpTanggalMasuk;
    @FXML private Label lblStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Karyawan> tableKaryawan;
    @FXML private TableColumn<Karyawan, String> colId, colNama, colNoTelp, colAlamat, colUsername, colStatus, colRole;
    @FXML private TableColumn<Karyawan, Integer> colUmur;
    @FXML private TableColumn<Karyawan, LocalDate> colTglMasuk;

    private final ObservableList<Karyawan> listKaryawan = FXCollections.observableArrayList();

    // ===========================================================
    // INIT
    // ===========================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
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
                        rs.getString("Status"),
                        rs.getString("Role")
                ));
            }
            tableKaryawan.setItems(listKaryawan);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data: " + e.getMessage());
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
            // Status & Role otomatis di dalam SP (AKTIF / SuperAdmin)
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertKaryawan(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setString(3, txtAlamat.getText().trim());
            cs.setInt(4, Integer.parseInt(txtUmur.getText().trim()));
            cs.setString(5, txtUsername.getText().trim());
            cs.setString(6, txtPassword.getText().trim());
            cs.setDate(7, Date.valueOf(dpTanggalMasuk.getValue()));
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data karyawan berhasil ditambahkan!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Umur harus berupa angka!");
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateKaryawan(?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());
            cs.setString(4, txtAlamat.getText().trim());
            cs.setInt(5, Integer.parseInt(txtUmur.getText().trim()));
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data karyawan berhasil diubah!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Umur harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data yang ingin dinonaktifkan terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtNama,
                "Yakin ingin menonaktifkan karyawan " + txtNama.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeleteKaryawan(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Karyawan berhasil dinonaktifkan!",
                                () -> {
                                    setClose();
                                    loadTable();
                                });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menonaktifkan: " + e.getMessage());
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
    private void populateForm(Karyawan k) {
        txtId.setText(k.getIdKaryawan());
        txtNama.setText(k.getNama());
        txtNoTelp.setText(k.getNoTelp());
        txtUmur.setText(String.valueOf(k.getUmur()));
        txtAlamat.setText(k.getAlamat());
        dpTanggalMasuk.setValue(k.getTanggalMasuk());
        txtUsername.setText(k.getUsername());
        txtPassword.clear();
        lblStatus.setText(k.getStatus());

        txtUsername.setDisable(true);
        txtPassword.setDisable(true);
        dpTanggalMasuk.setDisable(true);

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
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
        lblStatus.setText("-");

        txtUsername.setDisable(false);
        txtPassword.setDisable(false);
        dpTanggalMasuk.setDisable(false);

        tableKaryawan.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    // - Nama    : wajib diisi, tidak boleh angka, tidak boleh simbol (huruf & spasi saja)
    // - NoTelp  : wajib diisi, hanya boleh angka (tidak boleh huruf/simbol)
    // - Umur    : wajib diisi, hanya angka, maksimal 2 digit (0-99)
    // - Alamat  : wajib diisi, tidak boleh angka, tidak boleh simbol seperti @#$%^&*!
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                dpTanggalMasuk.getValue() == null ||
                txtUsername.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return validasiFormat();
    }

    private boolean validasiUpdate() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtUmur.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return validasiFormat();
    }

    // Validasi format field yang sama-sama dipakai insert & update
    private boolean validasiFormat() {
        String nama = txtNama.getText().trim();
        String noTelp = txtNoTelp.getText().trim();
        String umur = txtUmur.getText().trim();
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

        // Umur: hanya angka, maksimal 2 digit
        if (!umur.matches("^[0-9]{1,2}$")) {
            notif(NotifUtil.Type.WARNING, "Umur harus berupa angka dan maksimal 2 digit!");
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
}