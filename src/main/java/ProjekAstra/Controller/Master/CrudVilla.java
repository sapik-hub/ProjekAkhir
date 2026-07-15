package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Villa;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.FileUtil;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.TableCell;

public class CrudVilla implements Initializable {

    @FXML private TextField txtId, txtNamaVilla, txtKapasitas, txtHargaWeekday, txtHargaWeekend, txtAlamat, txtCari;
    @FXML private TextField txtPemilik; // read-only, isinya nama pemilik yang login
    @FXML private ComboBox<String> cbKategori, cbStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus, btnPilihFoto;
    @FXML private ImageView imgPreview;

    @FXML private TableView<Villa> tableVilla;
    @FXML private TableColumn<Villa, String> colId, colPemilik, colKategori, colNamaVilla, colAlamat, colStatus;
    @FXML private TableColumn<Villa, Integer> colKapasitas;
    @FXML private TableColumn<Villa, BigDecimal> colHargaWeekday, colHargaWeekend;

    private final ObservableList<Villa> listVilla = FXCollections.observableArrayList();

    // File asli yang baru dipilih user (belum di-copy), null kalau belum ganti foto
    private File fotoTerpilih;
    // Nama file foto yang sedang aktif buat data ini (dari DB, atau hasil upload baru)
    private String namaFotoAktif;

    // Formatter khusus rupiah: pemisah ribuan pakai titik, tanpa desimal
    private static final DecimalFormat RUPIAH_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        RUPIAH_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatus.getItems().addAll("Tersedia", "Tidak Tersedia", "Maintenance");

        setupTable();
        loadComboKategori();
        loadTable();
        setClose();
        setupHargaFormatter();

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
        colHargaWeekday.setCellValueFactory(new PropertyValueFactory<>("hargaWeekday"));
        colHargaWeekend.setCellValueFactory(new PropertyValueFactory<>("hargaWeekend"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamatVilla"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupHargaColumn(colHargaWeekday);
        setupHargaColumn(colHargaWeekend);
    }

    // Bikin kolom harga di tabel tampil "Rp 10.000.000" bukan angka mentah
    private void setupHargaColumn(TableColumn<Villa, BigDecimal> kolom) {
        kolom.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Villa, BigDecimal> call(TableColumn<Villa, BigDecimal> column) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(BigDecimal harga, boolean empty) {
                        super.updateItem(harga, empty);
                        if (empty || harga == null) {
                            setText(null);
                        } else {
                            setText("Rp " + RUPIAH_FORMAT.format(harga));
                        }
                    }
                };
            }
        });
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
                        rs.getBigDecimal("HargaWeekday"),
                        rs.getBigDecimal("HargaWeekend"),
                        rs.getString("AlamatVilla"),
                        rs.getString("Foto"),
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

    // ===========================================================
    // UPLOAD FOTO
    // ===========================================================
    @FXML
    private void handlePilihFoto() {
        File file = FileUtil.pilihGambar(imgPreview.getScene().getWindow());
        if (file != null) {
            fotoTerpilih = file;
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        // Kalau user pilih foto baru, copy dulu ke folder upload
        String namaFotoUntukDisimpan = null;
        if (fotoTerpilih != null) {
            namaFotoUntukDisimpan = FileUtil.simpanFoto(fotoTerpilih);
            if (namaFotoUntukDisimpan == null) {
                notif(NotifUtil.Type.ERROR, "Gagal menyimpan foto ke penyimpanan!");
                return;
            }
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertVilla(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, Session.getIdPemilik());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(unformatRupiah(txtHargaWeekday.getText().trim())));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, namaFotoUntukDisimpan); // bisa null kalau gak upload foto
            cs.setString(8, cbStatus.getValue());
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

        // Default: pakai foto lama. Kalau user pilih foto baru, upload & hapus foto lama.
        String namaFotoUntukDisimpan = namaFotoAktif;
        if (fotoTerpilih != null) {
            String fotoBaru = FileUtil.simpanFoto(fotoTerpilih);
            if (fotoBaru == null) {
                notif(NotifUtil.Type.ERROR, "Gagal menyimpan foto ke penyimpanan!");
                return;
            }
            if (namaFotoAktif != null) {
                FileUtil.hapusFoto(namaFotoAktif); // bersihin foto lama
            }
            namaFotoUntukDisimpan = fotoBaru;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateVilla(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(unformatRupiah(txtHargaWeekday.getText().trim())));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, namaFotoUntukDisimpan);
            cs.setString(8, cbStatus.getValue());
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
        txtHargaWeekday.setText(formatRupiah(v.getHargaWeekday().toBigInteger().toString()));
        txtHargaWeekend.setText(formatRupiah(v.getHargaWeekend().toBigInteger().toString()));
        txtAlamat.setText(v.getAlamatVilla());
        cbStatus.setValue(v.getStatus());

        selectComboByName(cbKategori, v.getNamaKategori());

        // Tampilkan foto yang tersimpan, kalau ada & filenya masih ada di disk
        namaFotoAktif = v.getFoto();
        fotoTerpilih = null; // reset pilihan foto baru
        if (namaFotoAktif != null) {
            String fullPath = FileUtil.getFullPath(namaFotoAktif);
            File imgFile = new File(fullPath);
            imgPreview.setImage(imgFile.exists() ? new Image(imgFile.toURI().toString()) : null);
        } else {
            imgPreview.setImage(null);
        }

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
        txtHargaWeekday.clear();
        txtHargaWeekend.clear();
        txtAlamat.clear();
        cbKategori.setValue(null);
        cbStatus.setValue(null);
        tableVilla.getSelectionModel().clearSelection();

        imgPreview.setImage(null);
        fotoTerpilih = null;
        namaFotoAktif = null;

        if (txtPemilik != null) {
            txtPemilik.setText(Session.getNamaPemilik());
        }

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
        generateIdVilla();
    }

    // ===========================================================
    // FORMAT HARGA (kayak kalkulator, titik ribuan otomatis)
    // Weekday diketik manual, Weekend otomatis dihitung x1.5 real-time
    // ===========================================================
    private void setupHargaFormatter() {
        txtHargaWeekday.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(oldVal)) return;

            String digitsOnly = unformatRupiah(newVal);
            String formatted = formatRupiah(digitsOnly);

            if (!formatted.equals(newVal)) {
                txtHargaWeekday.setText(formatted);
                txtHargaWeekday.positionCaret(formatted.length());
                return; // listener bakal kepanggil ulang, biarin update di panggilan berikutnya
            }

            hitungHargaWeekendOtomatis(digitsOnly);
        });
    }

    // Weekend = Weekday x 1.5, cuma buat preview di form (hitungan asli tetap di server/SP)
    private void hitungHargaWeekendOtomatis(String digitsOnly) {
        if (digitsOnly == null || digitsOnly.isEmpty()) {
            txtHargaWeekend.clear();
            return;
        }
        try {
            BigDecimal weekday = new BigDecimal(digitsOnly);
            BigDecimal weekend = weekday.multiply(new BigDecimal("1.5"));
            txtHargaWeekend.setText(formatRupiah(weekend.toBigInteger().toString()));
        } catch (NumberFormatException e) {
            txtHargaWeekend.clear();
        }
    }

    // Format "10000000" -> "10.000.000"
    private String formatRupiah(String digitsOnly) {
        if (digitsOnly == null || digitsOnly.isEmpty()) return "";
        digitsOnly = digitsOnly.replaceFirst("^0+(?!$)", "");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            sb.insert(0, digitsOnly.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) sb.insert(0, '.');
        }
        return sb.toString();
    }

    // Balikin "10.000.000" -> "10000000"
    private String unformatRupiah(String formatted) {
        return formatted == null ? "" : formatted.replaceAll("[^0-9]", "");
    }

    // ===========================================================
    // VALIDASI
    // ===========================================================
    private boolean validasiInsert() {
        if (cbKategori.getValue() == null ||
                txtNamaVilla.getText().trim().isEmpty() || txtKapasitas.getText().trim().isEmpty() ||
                txtHargaWeekday.getText().trim().isEmpty() || txtAlamat.getText().trim().isEmpty() ||
                cbStatus.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Semua field wajib diisi!");
            return false;
        }
        return validasiFormat();
    }

    private boolean validasiUpdate() {
        return validasiInsert();
    }

    private boolean validasiFormat() {
        String namaVilla = txtNamaVilla.getText().trim();
        String kapasitas = txtKapasitas.getText().trim();
        String harga = unformatRupiah(txtHargaWeekday.getText().trim());
        String alamat = txtAlamat.getText().trim();

        if (!namaVilla.matches("^[^0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Nama Villa tidak boleh mengandung angka!");
            return false;
        }

        if (!kapasitas.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Kapasitas harus berupa angka dan tidak boleh mengandung huruf!");
            return false;
        }

        if (harga.isEmpty() || !harga.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Harga Weekday harus berupa angka dan tidak boleh mengandung huruf!");
            return false;
        }

        if (!alamat.matches("^[a-zA-Z\\s]+$")) {
            notif(NotifUtil.Type.WARNING, "Alamat tidak boleh mengandung angka atau simbol!");
            return false;
        }

        return true;
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