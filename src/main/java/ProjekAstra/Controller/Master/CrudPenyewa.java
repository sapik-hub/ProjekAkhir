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
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CrudPenyewa implements Initializable {

    // ===== FORM =====
    @FXML private TextField txtId, txtNama, txtNoTelp, txtNikKtp, txtAlamat, txtCari;
    @FXML private DatePicker dpTglLahir;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // ===== TABLE =====
    @FXML private TableView<Penyewa> tablePenyewa;
    @FXML private TableColumn<Penyewa, String> colId, colNama, colNoTelp, colNikKtp, colAlamat;
    @FXML private TableColumn<Penyewa, LocalDate> colTglLahir;

    private final ObservableList<Penyewa> listPenyewa = FXCollections.observableArrayList();

    // Formatter untuk DatePicker (DD/MM/YYYY)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===========================================================
    // INIT
    // ===========================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupDatePicker();  // <-- TAMBAHKAN INI
        loadTable();
        setClose();

        tablePenyewa.setOnMouseClicked(e -> {
            Penyewa p = tablePenyewa.getSelectionModel().getSelectedItem();
            if (p != null) populateForm(p);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariPenyewa(newVal));
    }

    // ===========================================================
    // SETUP DATE PICKER (Format DD/MM/YYYY)
    // ===========================================================
    private void setupDatePicker() {
        // Konverter untuk menampilkan tanggal dengan format dd/MM/yyyy
        dpTglLahir.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return DATE_FORMATTER.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, DATE_FORMATTER);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // Set prompt text
        dpTglLahir.setPromptText("dd/MM/yyyy");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPenyewa"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNoTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));
        colNikKtp.setCellValueFactory(new PropertyValueFactory<>("nikKtp"));
        colTglLahir.setCellValueFactory(new PropertyValueFactory<>("tglLahir"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        // Format tanggal di tabel
        colTglLahir.setCellFactory(col -> new TableCell<Penyewa, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });
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
                Date tgl = rs.getDate("Tgl_Lahir");
                listPenyewa.add(new Penyewa(
                        rs.getString("Id_Penyewa"),
                        rs.getString("Nama"),
                        rs.getString("No_Telepon"),
                        rs.getString("No_KTP"),
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
                    p.getIdPenyewa().toLowerCase().contains(keyword.toLowerCase()) ||
                    p.getNoTelp().contains(keyword)) {
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
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPenyewa(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
            cs.setString(3, txtNikKtp.getText().trim());

            // Hitung umur dari tanggal lahir
            int umur = hitungUmur(dpTglLahir.getValue());
            cs.setInt(4, umur);

            cs.setDate(5, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setNull(7, java.sql.Types.VARCHAR); // Username = NULL
            cs.setNull(8, java.sql.Types.VARCHAR); // Password = NULL
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil ditambahkan!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (Exception e) {
            String pesan = e.getMessage() != null ? e.getMessage() : "";
            if (pesan.contains("UNIQUE") || pesan.contains("UQ")) {
                notif(NotifUtil.Type.WARNING, "Nomor KTP atau Telepon sudah terdaftar!");
            } else {
                notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + pesan);
            }
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // Helper: Hitung umur dari tanggal lahir
    private int hitungUmur(LocalDate tglLahir) {
        if (tglLahir == null) return 0;
        return java.time.Period.between(tglLahir, LocalDate.now()).getYears();
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
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdatePenyewa(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtNama.getText().trim());
            cs.setString(3, txtNoTelp.getText().trim());

            // Hitung umur dari tanggal lahir
            int umur = hitungUmur(dpTglLahir.getValue());
            cs.setInt(4, umur);

            cs.setDate(5, Date.valueOf(dpTglLahir.getValue()));
            cs.setString(6, txtAlamat.getText().trim());
            cs.execute();

            NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil diubah!",
                    () -> {
                        setClose();
                        loadTable();
                    });
        } catch (Exception e) {
            String pesan = e.getMessage() != null ? e.getMessage() : "";
            if (pesan.contains("UNIQUE") || pesan.contains("UQ")) {
                notif(NotifUtil.Type.WARNING, "Nomor Telepon sudah terdaftar!");
            } else {
                notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + pesan);
            }
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
                "Yakin ingin menonaktifkan penyewa " + txtNama.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_DeletePenyewa(?)}");
                        cs.setString(1, txtId.getText());
                        cs.execute();

                        NotifUtil.show(txtNama, NotifUtil.Type.SUCCESS, "Data penyewa berhasil dinonaktifkan!",
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
    private void populateForm(Penyewa p) {
        txtId.setText(p.getIdPenyewa());
        txtNama.setText(p.getNama());
        txtNoTelp.setText(p.getNoTelp());
        txtNikKtp.setText(p.getNikKtp());
        dpTglLahir.setValue(p.getTglLahir());
        txtAlamat.setText(p.getAlamat());

        txtNikKtp.setDisable(true);

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
        dpTglLahir.setValue(null);

        txtNikKtp.setDisable(false);

        tablePenyewa.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);

        generateIdPenyewa();
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    private boolean validasiInsert() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                txtNikKtp.getText().trim().isEmpty() || dpTglLahir.getValue() == null ||
                txtAlamat.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }

        if (!txtNikKtp.getText().trim().matches("\\d{16}")) {
            notif(NotifUtil.Type.WARNING, "NIK KTP harus terdiri dari 16 digit angka!");
            return false;
        }

        // Validasi umur minimal 17 tahun
        int umur = hitungUmur(dpTglLahir.getValue());
        if (umur < 17) {
            notif(NotifUtil.Type.WARNING, "Penyewa minimal berusia 17 tahun!");
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

        // Validasi umur minimal 17 tahun
        int umur = hitungUmur(dpTglLahir.getValue());
        if (umur < 17) {
            notif(NotifUtil.Type.WARNING, "Penyewa minimal berusia 17 tahun!");
            return false;
        }

        return validasiFormat();
    }

    private boolean validasiFormat() {
        String nama = txtNama.getText().trim();
        String noTelp = txtNoTelp.getText().trim();
        String alamat = txtAlamat.getText().trim();

        if (!nama.matches("^[a-zA-Z\\s]+$")) {
            notif(NotifUtil.Type.WARNING, "Nama tidak boleh mengandung angka atau simbol!");
            return false;
        }

        if (!noTelp.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "No Telp hanya boleh berisi angka!");
            return false;
        }

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