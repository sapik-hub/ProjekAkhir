package ProjekAstra.Controller.Master;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.DetailFasilitas;
import ProjekAstra.Model.Villa;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.FileUtil;
import ProjekAstra.Util.NotifUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.scene.control.TableCell;

public class CrudVilla implements Initializable {

    @FXML private TextField txtId, txtNamaVilla, txtKapasitas, txtHargaWeekday, txtHargaWeekend, txtAlamat, txtCari;
    @FXML private ComboBox<String> cbKategori, cbStatus;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    // >>> FOTO
    @FXML private ImageView imgPreview;
    @FXML private Button btnPilihFoto;

    @FXML private TableView<Villa> tableVilla;
    @FXML private TableColumn<Villa, String> colId, colKategori, colNamaVilla, colAlamat, colStatus;
    @FXML private TableColumn<Villa, Integer> colKapasitas;
    @FXML private TableColumn<Villa, BigDecimal> colHargaWeekday, colHargaWeekend;

    // >>> FASILITAS
    @FXML private Button btnBukaDialogFasilitas, btnHapusFasilitas;
    @FXML private TableView<DetailFasilitas> tableFasilitasVilla;
    @FXML private TableColumn<DetailFasilitas, String> colFasNama, colFasDeskripsi;
    @FXML private TableColumn<DetailFasilitas, Integer> colFasQty;

    private final ObservableList<Villa> listVilla = FXCollections.observableArrayList();
    private final ObservableList<DetailFasilitas> listFasilitasVilla = FXCollections.observableArrayList();

    // >>> FOTO: file yang baru dipilih user (belum di-copy ke folder foto) & nama foto lama waktu mode edit
    private File fotoTerpilih;
    private String fotoLama;

    private static final DecimalFormat RUPIAH_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        RUPIAH_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatus.getItems().addAll("Tersedia", "Dipesan", "Maintenance");

        setupTable();
        setupTableFasilitas();
        loadComboKategori();
        loadTable();
        setClose();
        setupHargaFormatter(txtHargaWeekday);
        setupHargaWeekendAutoCalc();

        tableVilla.setOnMouseClicked(e -> {
            Villa v = tableVilla.getSelectionModel().getSelectedItem();
            if (v != null) populateForm(v);
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariVilla(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idVilla"));
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

    private void setupTableFasilitas() {
        colFasNama.setCellValueFactory(new PropertyValueFactory<>("namaFasilitas"));
        colFasQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
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
                        setText((empty || harga == null) ? null : "Rp " + RUPIAH_FORMAT.format(harga));
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
                cbKategori.getItems().add(rs.getString("Id_Kategori") + " - " + rs.getString("Nama_Kategori"));
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data kategori: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void loadFasilitasVilla(String idVilla) {
        listFasilitasVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetFasilitasByVilla(?)}");
            cs.setString(1, idVilla);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                listFasilitasVilla.add(new DetailFasilitas(
                        rs.getString("Id_Villa"),
                        rs.getString("Id_Fasilitas"),
                        rs.getString("Nama_Fasilitas"),
                        rs.getInt("Qty"),
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
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                listVilla.add(new Villa(
                        rs.getString("Id_Villa"),
                        rs.getString("Nama_Kategori"),
                        rs.getString("Nama_Villa"),
                        rs.getInt("Kapasitas"),
                        rs.getBigDecimal("HargaWeekday"),
                        rs.getBigDecimal("HargaWeekend"),
                        rs.getString("Alamat_Villa"),
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

    // >>> FOTO: buka file chooser buat pilih gambar
    @FXML
    private void handlePilihFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Foto Villa");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(txtNamaVilla.getScene().getWindow());
        if (file != null) {
            fotoTerpilih = file;
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    // >>> FOTO: copy foto yang baru dipilih ke folder penyimpanan, generate nama unik.
    // Kalau user gak pilih foto baru, tetap pakai foto lama (bisa null kalau villa baru & belum pernah diisi foto)
    private String simpanFotoJikaAda(String fotoFallback) {
        if (fotoTerpilih == null) {
            return fotoFallback;
        }
        try {
            String namaAsli = fotoTerpilih.getName();
            String ekstensi = namaAsli.contains(".") ? namaAsli.substring(namaAsli.lastIndexOf('.')) : "";
            String namaBaru = UUID.randomUUID().toString().replace("-", "") + ekstensi;

            File tujuan = new File(FileUtil.getFullPath(namaBaru));
            Files.copy(fotoTerpilih.toPath(), tujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return namaBaru;
        } catch (IOException e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan foto: " + e.getMessage());
            return fotoFallback;
        }
    }

    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        String idBaru = txtId.getText();
        String namaFoto = simpanFotoJikaAda(null); // villa baru: gak ada foto lama

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertVilla(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, getIdFromCombo(cbKategori.getValue()));
            cs.setString(2, txtNamaVilla.getText().trim());
            cs.setInt(3, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(4, new BigDecimal(unformatRupiah(txtHargaWeekday.getText().trim())));
            cs.setBigDecimal(5, new BigDecimal(unformatRupiah(txtHargaWeekend.getText().trim())));
            cs.setString(6, txtAlamat.getText().trim());
            if (namaFoto == null) cs.setNull(7, Types.VARCHAR); else cs.setString(7, namaFoto);
            cs.setString(8, cbStatus.getValue());
            cs.execute();

            NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Villa berhasil ditambahkan! Sekarang kamu bisa tambahkan fasilitasnya.",
                    () -> {
                        loadTable();
                        selectVillaById(idBaru);
                    });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Kapasitas dan Harga harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

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

        String namaFoto = simpanFotoJikaAda(fotoLama); // kalau gak pilih foto baru, pertahankan foto lama

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateVilla(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbKategori.getValue()));
            cs.setString(3, txtNamaVilla.getText().trim());
            cs.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
            cs.setBigDecimal(5, new BigDecimal(unformatRupiah(txtHargaWeekday.getText().trim())));
            cs.setBigDecimal(6, new BigDecimal(unformatRupiah(txtHargaWeekend.getText().trim())));
            cs.setString(7, txtAlamat.getText().trim());
            if (namaFoto == null) cs.setNull(8, Types.VARCHAR); else cs.setString(8, namaFoto);
            cs.setString(9, cbStatus.getValue());
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

    @FXML
    private void handleBukaDialogFasilitas() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Simpan atau pilih villa dulu sebelum menambah fasilitas!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UICrud/UIDialogTambahFasilitas.fxml"));
            Parent root = loader.load();

            DialogTambahFasilitas controller = loader.getController();
            controller.setIdVilla(txtId.getText());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Tambah Fasilitas");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isBerhasilDitambahkan()) {
                loadFasilitasVilla(txtId.getText());
                NotifUtil.show(txtNamaVilla, NotifUtil.Type.SUCCESS, "Fasilitas berhasil ditambahkan!");
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal membuka form fasilitas: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapusFasilitas() {
        DetailFasilitas selected = tableFasilitasVilla.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notif(NotifUtil.Type.WARNING, "Pilih fasilitas yang ingin dihapus dari villa ini!");
            return;
        }

        ConfirmUtil.show(txtNamaVilla,
                "Yakin ingin menghapus fasilitas " + selected.getNamaFasilitas() + " dari villa ini?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_HapusFasilitasVilla(?, ?)}");
                        cs.setString(1, selected.getIdVilla());
                        cs.setString(2, selected.getIdFasilitas());
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
        txtAlamat.setText(v.getAlamatVilla());
        cbStatus.setValue(v.getStatus());

        selectComboByName(cbKategori, v.getNamaKategori());

        // >>> FOTO: reset pilihan file baru, tampilkan foto yang sudah tersimpan (kalau ada)
        fotoTerpilih = null;
        fotoLama = v.getFoto();
        if (fotoLama != null) {
            File file = new File(FileUtil.getFullPath(fotoLama));
            imgPreview.setImage(file.exists() ? new Image(file.toURI().toString()) : null);
        } else {
            imgPreview.setImage(null);
        }

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);

        loadFasilitasVilla(v.getIdVilla());
        setFasilitasPanelEnabled(true);
    }

    private void setFasilitasPanelEnabled(boolean enabled) {
        btnBukaDialogFasilitas.setDisable(!enabled);
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

        // >>> FOTO: reset preview & state foto
        fotoTerpilih = null;
        fotoLama = null;
        imgPreview.setImage(null);

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
        generateIdVilla();

        listFasilitasVilla.clear();
        tableFasilitasVilla.setItems(listFasilitasVilla);
        setFasilitasPanelEnabled(false);
    }

    private void setupHargaFormatter(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(oldVal)) return;

            String digitsOnly = unformatRupiah(newVal);
            String formatted = formatRupiah(digitsOnly);

            if (!formatted.equals(newVal)) {
                field.setText(formatted);
                field.positionCaret(formatted.length());
            }
        });
    }

    // >>> HARGA WEEKEND: otomatis dihitung dari Harga Weekday x 1.5, field dikunci dari input manual
    private void setupHargaWeekendAutoCalc() {
        txtHargaWeekend.setEditable(false);
        txtHargaWeekend.setFocusTraversable(false);

        txtHargaWeekday.textProperty().addListener((obs, oldVal, newVal) -> {
            String digitsOnly = unformatRupiah(newVal);
            if (digitsOnly.isEmpty()) {
                txtHargaWeekend.setText("");
                return;
            }
            BigDecimal weekday = new BigDecimal(digitsOnly);
            BigDecimal weekend = weekday.multiply(new BigDecimal("1.5"))
                    .setScale(0, RoundingMode.HALF_UP);
            txtHargaWeekend.setText(formatRupiah(weekend.toBigInteger().toString()));
        });
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
                txtHargaWeekday.getText().trim().isEmpty() ||
                txtAlamat.getText().trim().isEmpty() || cbStatus.getValue() == null) {
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
        String hargaWeekday = unformatRupiah(txtHargaWeekday.getText().trim());
        String alamat = txtAlamat.getText().trim();

        if (!namaVilla.matches("^[^0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Nama Villa tidak boleh mengandung angka!");
            return false;
        }

        if (!kapasitas.matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Kapasitas harus berupa angka dan tidak boleh mengandung huruf!");
            return false;
        }

        if (hargaWeekday.isEmpty() || !hargaWeekday.matches("^[0-9]+$")) {
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
            java.sql.PreparedStatement ps = k.conn.prepareStatement(sql);
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