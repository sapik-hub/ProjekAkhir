package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Fasilitas;
import ProjekAstra.Model.Villa;
import ProjekAstra.Model.VillaFasilitas;
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
    @FXML private TextField txtPemilik;
    @FXML private ComboBox<String> cbKategori, cbStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus, btnPilihFoto;
    @FXML private ImageView imgPreview;

    @FXML private TableView<Villa> tableVilla;
    @FXML private TableColumn<Villa, String> colId, colPemilik, colKategori, colNamaVilla, colAlamat, colStatus;
    @FXML private TableColumn<Villa, Integer> colKapasitas;
    @FXML private TableColumn<Villa, BigDecimal> colHargaWeekday, colHargaWeekend;

    // >>> FASILITAS: komponen panel tambah fasilitas ke villa
    @FXML private ComboBox<Fasilitas> cbFasilitas;
    @FXML private TextField txtJumlahFasilitas, txtDeskripsiFasilitas;
    @FXML private Button btnTambahFasilitas, btnHapusFasilitas;
    @FXML private TableView<VillaFasilitas> tableFasilitasVilla;
    @FXML private TableColumn<VillaFasilitas, String> colFasNama, colFasDeskripsi;
    @FXML private TableColumn<VillaFasilitas, Integer> colFasJumlah;

    private final ObservableList<Villa> listVilla = FXCollections.observableArrayList();
    private final ObservableList<VillaFasilitas> listFasilitasVilla = FXCollections.observableArrayList(); // >>> FASILITAS

    private File fotoTerpilih;
    private String namaFotoAktif;

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
        setupTableFasilitas(); // >>> FASILITAS
        loadComboKategori();
        loadComboFasilitas(); // >>> FASILITAS
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

    // >>> FASILITAS
    private void setupTableFasilitas() {
        colFasNama.setCellValueFactory(new PropertyValueFactory<>("namaFasilitas"));
        colFasJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colFasDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
    }

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

    // >>> FASILITAS: load daftar master fasilitas buat combobox
    private void loadComboFasilitas() {
        cbFasilitas.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllFasilitas}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbFasilitas.getItems().add(new Fasilitas(rs.getString("IdFasilitas"), rs.getString("NamaFasilitas")));
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data fasilitas: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // >>> FASILITAS: load fasilitas yang sudah nempel ke satu villa tertentu
    private void loadFasilitasVilla(String idVilla) {
        listFasilitasVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetFasilitasByVilla(?)}");
            cs.setString(1, idVilla);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                listFasilitasVilla.add(new VillaFasilitas(
                        rs.getString("IdVillaFasilitas"),
                        rs.getString("IdVilla"),
                        rs.getString("IdFasilitas"),
                        rs.getString("NamaFasilitas"),
                        rs.getInt("Jumlah"),
                        rs.getString("Deskripsi")
                ));
            }
            tableFasilitasVilla.setItems(listFasilitasVilla);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat fasilitas villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

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

        String namaFotoUntukDisimpan = null;
        if (fotoTerpilih != null) {
            namaFotoUntukDisimpan = FileUtil.simpanFoto(fotoTerpilih);
            if (namaFotoUntukDisimpan == null) {
                notif(NotifUtil.Type.ERROR, "Gagal menyimpan foto ke penyimpanan!");
                return;
            }
        }

        String idBaru = txtId.getText(); // >>> FASILITAS: simpan ID yg sudah di-generate sebelum form ke-reset

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertVilla(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, Session.getIdPemilik());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(unformatRupiah(txtHargaWeekday.getText().trim())));
            cs.setString(6, txtAlamat.getText().trim());
            cs.setString(7, namaFotoUntukDisimpan);
            cs.setString(8, cbStatus.getValue());
            cs.execute();

            NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Villa berhasil ditambahkan! Sekarang kamu bisa tambahkan fasilitasnya.",
                    () -> {
                        loadTable();
                        selectVillaById(idBaru); // >>> FASILITAS: langsung masuk mode edit villa yg baru dibuat
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Kapasitas dan Harga harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // >>> FASILITAS: cari villa di list lokal berdasarkan ID, lalu populate form (biar panel fasilitas aktif)
    private void selectVillaById(String idVilla) {
        for (Villa v : listVilla) {
            if (v.getIdVilla().equals(idVilla)) {
                populateForm(v);
                tableVilla.getSelectionModel().select(v);
                break;
            }
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data yang ingin diubah terlebih dahulu!");
            return;
        }
        if (!validasiUpdate()) return;

        String namaFotoUntukDisimpan = namaFotoAktif;
        if (fotoTerpilih != null) {
            String fotoBaru = FileUtil.simpanFoto(fotoTerpilih);
            if (fotoBaru == null) {
                notif(NotifUtil.Type.ERROR, "Gagal menyimpan foto ke penyimpanan!");
                return;
            }
            if (namaFotoAktif != null) {
                FileUtil.hapusFoto(namaFotoAktif);
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

    // >>> FASILITAS: tambah 1 fasilitas ke villa yang lagi aktif di form
    @FXML
    private void handleTambahFasilitas() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Simpan atau pilih villa dulu sebelum menambah fasilitas!");
            return;
        }
        if (cbFasilitas.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Pilih fasilitas yang ingin ditambahkan!");
            return;
        }
        String jumlahStr = txtJumlahFasilitas.getText().trim();
        if (!jumlahStr.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Jumlah fasilitas harus berupa angka!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_TambahFasilitasVilla(?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, cbFasilitas.getValue().getIdFasilitas());
            cs.setInt(3, Integer.parseInt(jumlahStr));
            cs.setString(4, txtDeskripsiFasilitas.getText().trim());
            cs.execute();

            cbFasilitas.setValue(null);
            txtJumlahFasilitas.clear();
            txtDeskripsiFasilitas.clear();
            loadFasilitasVilla(txtId.getText());
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menambah fasilitas: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // >>> FASILITAS: hapus 1 baris fasilitas dari villa (bukan hapus master-nya)
    @FXML
    private void handleHapusFasilitas() {
        VillaFasilitas selected = tableFasilitasVilla.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notif(NotifUtil.Type.WARNING, "Pilih fasilitas yang ingin dihapus dari villa ini!");
            return;
        }

        ConfirmUtil.show(txtNamaVilla,
                "Yakin ingin menghapus fasilitas " + selected.getNamaFasilitas() + " dari villa ini?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_HapusFasilitasVilla(?)}");
                        cs.setString(1, selected.getIdVillaFasilitas());
                        cs.execute();
                        loadFasilitasVilla(txtId.getText());
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal menghapus fasilitas: " + e.getMessage());
                    } finally {
                        try { k.conn.close(); } catch (Exception ignored) {}
                    }
                });
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

        namaFotoAktif = v.getFoto();
        fotoTerpilih = null;
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

        loadFasilitasVilla(v.getIdVilla()); // >>> FASILITAS
        setFasilitasPanelEnabled(true);     // >>> FASILITAS
    }

    // >>> FASILITAS
    private void setFasilitasPanelEnabled(boolean enabled) {
        cbFasilitas.setDisable(!enabled);
        txtJumlahFasilitas.setDisable(!enabled);
        txtDeskripsiFasilitas.setDisable(!enabled);
        btnTambahFasilitas.setDisable(!enabled);
        btnHapusFasilitas.setDisable(!enabled);
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

        // >>> FASILITAS: kosongin & matiin panel karena belum ada villa aktif
        listFasilitasVilla.clear();
        tableFasilitasVilla.setItems(listFasilitasVilla);
        cbFasilitas.setValue(null);
        txtJumlahFasilitas.clear();
        txtDeskripsiFasilitas.clear();
        setFasilitasPanelEnabled(false);
    }

    private void setupHargaFormatter() {
        txtHargaWeekday.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(oldVal)) return;

            String digitsOnly = unformatRupiah(newVal);
            String formatted = formatRupiah(digitsOnly);

            if (!formatted.equals(newVal)) {
                txtHargaWeekday.setText(formatted);
                txtHargaWeekday.positionCaret(formatted.length());
                return;
            }

            hitungHargaWeekendOtomatis(digitsOnly);
        });
    }

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

    private String unformatRupiah(String formatted) {
        return formatted == null ? "" : formatted.replaceAll("[^0-9]", "");
    }

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