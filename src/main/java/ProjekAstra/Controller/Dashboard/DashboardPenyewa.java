package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import ProjekAstra.Model.Villa;
import ProjekAstra.Model.DetailFasilitas;
import ProjekAstra.Model.Penyewa;
import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.FileUtil;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardPenyewa {

    @FXML private Label lblJudul;

    @FXML private VBox mainView;
    @FXML private TextField txtCari;
    @FXML private FlowPane bubbleContainer;

    @FXML private StackPane overlayPane;

    // ---- detail bubble ----
    @FXML private VBox detailBubble;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailNama;
    @FXML private Label lblDetailKategori;
    @FXML private Label lblDetailKapasitas;
    @FXML private Label lblDetailHargaWeekday;
    @FXML private Label lblDetailHargaWeekend;
    @FXML private Label lblDetailAlamat;
    @FXML private Label lblDetailPemilik;
    @FXML private ImageView imgDetailFoto;
    @FXML private FlowPane fasilitasContainer;

    // ---- verifikasi bubble ----
    @FXML private VBox verifBubble;
    @FXML private TextField txtNoTeleponCari;

    // ---- registrasi bubble ----
    @FXML private VBox registrasiBubble;
    @FXML private TextField txtRegNama;
    @FXML private TextField txtRegNoTelp;
    @FXML private TextField txtRegNoKtp;
    @FXML private DatePicker dpRegTglLahir;
    @FXML private TextArea txtRegAlamat;

    // ---- booking bubble ----
    @FXML private VBox bookingPane;
    @FXML private Label lblVillaTerpilih;
    @FXML private Label lblPenyewaAktif;
    @FXML private ComboBox<String> cbMetodePembayaran;
    @FXML private TextArea txtAlamatBooking;
    @FXML private TextArea txtCatatan;
    @FXML private DatePicker dpCheckin;
    @FXML private DatePicker dpCheckout;
    @FXML private TextField txtJumlahTamu;
    @FXML private Label lblGrandHarga;

    private final List<Villa> daftarVilla = new ArrayList<>();
    private Villa villaTerpilih;

    // >>> Penyewa yang aktif di sesi kiosk saat ini (hasil dari verifikasi/registrasi)
    private Penyewa penyewaAktif;

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        muatDaftarVilla();
        renderBubble(daftarVilla);

        setupComboMetodePembayaran();

        dpCheckin.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
        dpCheckout.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
    }

    private void setupComboMetodePembayaran() {
        cbMetodePembayaran.getItems().setAll("Cash", "Transfer Bank", "QRIS");
    }

    private void muatDaftarVilla() {
        daftarVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                daftarVilla.add(new Villa(
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
        } catch (Exception e) {
            NotifUtil.show(bubbleContainer, NotifUtil.Type.ERROR, "Gagal memuat data villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private List<DetailFasilitas> muatFasilitas(String idVilla) {
        List<DetailFasilitas> hasil = new ArrayList<>();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetFasilitasByVilla(?)}");
            cs.setString(1, idVilla);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                hasil.add(new DetailFasilitas(
                        rs.getString("Id_Villa"),
                        rs.getString("Id_Fasilitas"),
                        rs.getString("Nama_Fasilitas"),
                        rs.getInt("Qty"),
                        rs.getString("Deskripsi")
                ));
            }
        } catch (Exception e) {
            NotifUtil.show(fasilitasContainer, NotifUtil.Type.ERROR, "Gagal memuat fasilitas: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
        return hasil;
    }

    private void renderFasilitas(List<DetailFasilitas> list) {
        fasilitasContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label kosong = new Label("Belum ada fasilitas");
            kosong.getStyleClass().add("detail-desc");
            fasilitasContainer.getChildren().add(kosong);
            return;
        }
        for (DetailFasilitas f : list) {
            String teks = f.getNamaFasilitas() + (f.getQty() > 1 ? " (" + f.getQty() + ")" : "");
            Label chip = new Label(teks);
            chip.getStyleClass().add("fasilitas-chip");
            fasilitasContainer.getChildren().add(chip);
        }
    }

    private void renderBubble(List<Villa> list) {
        bubbleContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label kosong = new Label("Villa tidak ditemukan");
            kosong.getStyleClass().add("bubble-empty");
            bubbleContainer.getChildren().add(kosong);
            return;
        }
        for (Villa v : list) {
            bubbleContainer.getChildren().add(buatBubble(v));
        }
    }

    // ===========================================================
    // PERBAIKAN: buatBubble() dengan foto yang benar
    // ===========================================================
    private Button buatBubble(Villa v) {
        // Buat ImageView untuk foto
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(230);
        fotoView.setFitHeight(190);
        fotoView.setPreserveRatio(false);
        fotoView.getStyleClass().add("card-image");

        // Load foto jika ada
        if (v.getFoto() != null && !v.getFoto().isEmpty()) {
            String fullPath = FileUtil.getFullPath(v.getFoto());
            if (fullPath != null) {
                File file = new File(fullPath);
                if (file.exists()) {
                    try {
                        Image image = new Image(file.toURI().toString());
                        fotoView.setImage(image);
                    } catch (Exception e) {
                        System.out.println("Gagal load foto untuk " + v.getNamaVilla() + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("File foto tidak ditemukan: " + fullPath);
                }
            }
        }

        // Jika foto tidak ada atau gagal load, tampilkan placeholder
        if (fotoView.getImage() == null) {
            fotoView.setImage(null);
            fotoView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
        }

        Label lblNama = new Label(v.getNamaVilla());
        lblNama.getStyleClass().add("card-nama");
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(220);

        Label lblKapasitas = new Label("👥 " + v.getKapasitas() + " orang");
        lblKapasitas.getStyleClass().add("card-kapasitas");

        Label lblHarga = new Label(RUPIAH.format(v.getHargaWeekday()) + " / malam (weekday)");
        lblHarga.getStyleClass().add("card-kapasitas");

        VBox isiCard = new VBox(8, fotoView, lblNama, lblKapasitas, lblHarga);
        isiCard.setAlignment(Pos.CENTER);
        isiCard.setPadding(new Insets(0, 0, 18, 0));

        Button card = new Button();
        card.setGraphic(isiCard);
        card.getStyleClass().add("villa-card");
        card.setPrefWidth(250);
        card.setOnAction(e -> tampilkanDetail(v));
        return card;
    }

    // ===========================================================
    // PERBAIKAN: tampilkanDetail() dengan foto yang benar
    // ===========================================================
    private void tampilkanDetail(Villa v) {
        villaTerpilih = v;

        lblDetailStatus.setText(v.getStatus());
        lblDetailNama.setText(v.getNamaVilla());
        lblDetailKategori.setText("🏷 Kategori: " + v.getNamaKategori());
        lblDetailKapasitas.setText("👥 Kapasitas: " + v.getKapasitas() + " orang");
        lblDetailHargaWeekday.setText("Weekday: " + RUPIAH.format(v.getHargaWeekday()) + " / malam");
        lblDetailHargaWeekend.setText("Weekend (Jum-Sab/Sab-Min): " + RUPIAH.format(v.getHargaWeekend()) + " / malam");
        lblDetailAlamat.setText(v.getAlamatVilla());
        lblDetailPemilik.setText("");

        // Load foto untuk detail
        if (imgDetailFoto != null) {
            imgDetailFoto.setImage(null);
            if (v.getFoto() != null && !v.getFoto().isEmpty()) {
                String fullPath = FileUtil.getFullPath(v.getFoto());
                if (fullPath != null) {
                    File file = new File(fullPath);
                    if (file.exists()) {
                        try {
                            Image image = new Image(file.toURI().toString());
                            imgDetailFoto.setImage(image);
                        } catch (Exception e) {
                            System.out.println("Gagal load foto detail: " + e.getMessage());
                        }
                    }
                }
            }
            // Jika foto tidak ada, set placeholder style
            if (imgDetailFoto.getImage() == null) {
                imgDetailFoto.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
            } else {
                imgDetailFoto.setStyle("");
            }
        }

        renderFasilitas(muatFasilitas(v.getIdVilla()));

        showOverlayBubble(detailBubble);
    }

    // ===========================================================
    // METHOD LAINNYA (TETAP SAMA)
    // ===========================================================

    @FXML
    private void handleCari() {
        String kw = txtCari.getText() == null ? "" : txtCari.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            renderBubble(daftarVilla);
            return;
        }
        List<Villa> hasil = new ArrayList<>();
        for (Villa v : daftarVilla) {
            boolean cocokNama = v.getNamaVilla().toLowerCase().contains(kw);
            boolean cocokKapasitas = String.valueOf(v.getKapasitas()).equals(kw);
            if (cocokNama || cocokKapasitas) hasil.add(v);
        }
        renderBubble(hasil);
    }

    @FXML
    private void handleResetCari() {
        txtCari.clear();
        renderBubble(daftarVilla);
    }

    private Node[] semuaBubble() {
        return new Node[]{ detailBubble, verifBubble, registrasiBubble, bookingPane };
    }

    private void showOverlayBubble(Node bubble) {
        overlayPane.setVisible(true);
        overlayPane.setManaged(true);

        for (Node b : semuaBubble()) {
            boolean aktif = (b == bubble);
            b.setVisible(aktif);
            b.setManaged(aktif);
        }

        bubble.setScaleX(0.7);
        bubble.setScaleY(0.7);
        bubble.setOpacity(0);

        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(220), bubble);
        scale.setToX(1);
        scale.setToY(1);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(220), bubble);
        fade.setToValue(1);
        scale.play();
        fade.play();
    }

    private void closeOverlay() {
        Node current = null;
        for (Node b : semuaBubble()) {
            if (b.isVisible()) { current = b; break; }
        }
        if (current == null) {
            overlayPane.setVisible(false);
            overlayPane.setManaged(false);
            return;
        }

        Node finalCurrent = current;
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(180), current);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            overlayPane.setVisible(false);
            overlayPane.setManaged(false);
            finalCurrent.setVisible(false);
            finalCurrent.setManaged(false);
            mainView.setVisible(true);
        });
        fade.play();
    }

    @FXML
    private void handleKembaliBubble() {
        closeOverlay();
    }

    // =========================================================
    // TAHAP 1: VERIFIKASI PENYEWA
    // =========================================================

    @FXML
    private void handleMulaiVerifikasi() {
        if (villaTerpilih == null) return;
        txtNoTeleponCari.clear();
        showOverlayBubble(verifBubble);
    }

    @FXML
    private void handlePilihSudahPernah() {
        String noTelp = txtNoTeleponCari.getText() == null ? "" : txtNoTeleponCari.getText().trim();
        if (noTelp.isEmpty()) {
            NotifUtil.show(txtNoTeleponCari, NotifUtil.Type.WARNING, "Masukkan nomor telepon Anda terlebih dahulu!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_CariPenyewaByTelepon(?)}");
            cs.setString(1, noTelp);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                penyewaAktif = new Penyewa(
                        rs.getString("Id_Penyewa"),
                        rs.getString("Nama"),
                        rs.getString("No_Telepon"),
                        rs.getString("No_KTP"),
                        rs.getDate("Tgl_Lahir") == null ? null : rs.getDate("Tgl_Lahir").toLocalDate(),
                        rs.getString("Alamat"),
                        rs.getString("Username")
                );
                Session.setPenyewa(penyewaAktif.getIdPenyewa(), penyewaAktif.getNama());
                tampilkanFormBooking();
            } else {
                NotifUtil.show(txtNoTeleponCari, NotifUtil.Type.WARNING,
                        "Nomor tidak ditemukan. Silakan isi data diri Anda dulu.");
                txtRegNama.clear();
                txtRegNoTelp.setText(noTelp);
                txtRegNoKtp.clear();
                dpRegTglLahir.setValue(null);
                txtRegAlamat.clear();
                showOverlayBubble(registrasiBubble);
            }
        } catch (Exception e) {
            NotifUtil.show(txtNoTeleponCari, NotifUtil.Type.ERROR, "Gagal mencari data: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handlePilihBelumPernah() {
        txtRegNama.clear();
        txtRegNoTelp.setText(txtNoTeleponCari.getText() == null ? "" : txtNoTeleponCari.getText().trim());
        txtRegNoKtp.clear();
        dpRegTglLahir.setValue(null);
        txtRegAlamat.clear();
        showOverlayBubble(registrasiBubble);
    }

    @FXML
    private void handleKembaliDariVerif() {
        closeOverlay();
    }

    // =========================================================
    // TAHAP 2: REGISTRASI PENYEWA BARU
    // =========================================================

    @FXML
    private void handleSimpanRegistrasi() {
        if (!validasiRegistrasi()) return;

        ConfirmUtil.show(txtRegNama,
                "Pastikan data diri yang Anda isi sudah benar. Lanjutkan simpan?",
                this::simpanDataRegistrasi);
    }

    private void simpanDataRegistrasi() {
        String nama = txtRegNama.getText().trim();
        String noTelp = txtRegNoTelp.getText().trim();
        String noKtp = txtRegNoKtp.getText().trim();
        LocalDate tglLahir = dpRegTglLahir.getValue();
        String alamat = txtRegAlamat.getText().trim();
        int umur = Period.between(tglLahir, LocalDate.now()).getYears();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertPenyewa(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, nama);
            cs.setString(2, noTelp);
            cs.setString(3, noKtp);
            cs.setInt(4, umur);
            cs.setDate(5, java.sql.Date.valueOf(tglLahir));
            cs.setString(6, alamat);
            cs.setNull(7, Types.VARCHAR);
            cs.setNull(8, Types.VARCHAR);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                String idBaru = rs.getString("Id_Penyewa");
                penyewaAktif = new Penyewa(idBaru, nama, noTelp, noKtp, tglLahir, alamat, null);
                Session.setPenyewa(idBaru, nama);

                NotifUtil.show(txtRegNama, NotifUtil.Type.SUCCESS, "Data diri berhasil disimpan!",
                        this::tampilkanFormBooking);
            }
        } catch (Exception e) {
            String pesan = e.getMessage() != null ? e.getMessage() : "";
            if (pesan.contains("UQ__Penyewa") || (pesan.contains("UNIQUE") && pesan.contains("No_KTP"))) {
                NotifUtil.show(txtRegNoKtp, NotifUtil.Type.WARNING,
                        "Nomor KTP sudah terdaftar! Coba klik 'Sudah Pernah' dan cari pakai nomor telepon Anda.");
            } else {
                NotifUtil.show(txtRegNama, NotifUtil.Type.ERROR, "Gagal menyimpan data diri: " + e.getMessage());
            }
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleKembaliDariRegistrasi() {
        showOverlayBubble(verifBubble);
    }

    private boolean validasiRegistrasi() {
        if (txtRegNama.getText().trim().isEmpty()) {
            NotifUtil.show(txtRegNama, NotifUtil.Type.WARNING, "Nama wajib diisi!");
            return false;
        }
        if (!txtRegNama.getText().trim().matches("^[^0-9]+$")) {
            NotifUtil.show(txtRegNama, NotifUtil.Type.WARNING, "Nama tidak boleh mengandung angka!");
            return false;
        }
        if (txtRegNoTelp.getText().trim().isEmpty() || !txtRegNoTelp.getText().trim().matches("^[0-9]+$")) {
            NotifUtil.show(txtRegNoTelp, NotifUtil.Type.WARNING, "Nomor telepon wajib diisi dan berupa angka!");
            return false;
        }
        if (txtRegNoKtp.getText().trim().isEmpty() || !txtRegNoKtp.getText().trim().matches("^[0-9]{16}$")) {
            NotifUtil.show(txtRegNoKtp, NotifUtil.Type.WARNING, "Nomor KTP wajib diisi (16 digit angka)!");
            return false;
        }
        if (dpRegTglLahir.getValue() == null) {
            NotifUtil.show(dpRegTglLahir, NotifUtil.Type.WARNING, "Tanggal lahir wajib diisi!");
            return false;
        }
        int umur = Period.between(dpRegTglLahir.getValue(), LocalDate.now()).getYears();
        if (umur < 17) {
            NotifUtil.show(dpRegTglLahir, NotifUtil.Type.WARNING, "Penyewa minimal berusia 17 tahun!");
            return false;
        }
        if (txtRegAlamat.getText().trim().isEmpty()) {
            NotifUtil.show(txtRegAlamat, NotifUtil.Type.WARNING, "Alamat wajib diisi!");
            return false;
        }
        return true;
    }

    // =========================================================
    // TAHAP 3: FORM BOOKING
    // =========================================================

    private void tampilkanFormBooking() {
        lblVillaTerpilih.setText("Villa: " + villaTerpilih.getNamaVilla()
                + " (Weekday " + RUPIAH.format(villaTerpilih.getHargaWeekday())
                + " / Weekend " + RUPIAH.format(villaTerpilih.getHargaWeekend()) + ")");

        lblPenyewaAktif.setText(penyewaAktif.getNama() + " (" + penyewaAktif.getNoTelp() + ")");

        cbMetodePembayaran.setValue(null);
        txtAlamatBooking.setText(penyewaAktif.getAlamat() == null ? "" : penyewaAktif.getAlamat());
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
        txtJumlahTamu.clear();
        txtCatatan.clear();
        lblGrandHarga.setText("");

        showOverlayBubble(bookingPane);
    }

    private void hitungGrandHarga() {
        var ci = dpCheckin.getValue();
        var co = dpCheckout.getValue();
        if (ci != null && co != null && co.isAfter(ci) && villaTerpilih != null) {
            BigDecimal weekday = villaTerpilih.getHargaWeekday();
            BigDecimal weekend = villaTerpilih.getHargaWeekend();
            BigDecimal total = BigDecimal.ZERO;

            var tgl = ci;
            while (tgl.isBefore(co)) {
                boolean isWeekend = tgl.getDayOfWeek().toString().equals("FRIDAY")
                        || tgl.getDayOfWeek().toString().equals("SATURDAY");
                total = total.add(isWeekend ? weekend : weekday);
                tgl = tgl.plusDays(1);
            }

            lblGrandHarga.setText("Estimasi Total: " + RUPIAH.format(total));
        } else {
            lblGrandHarga.setText("");
        }
    }

    @FXML
    private void handleBookingSekarang() {
        if (!validasiBooking()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertBooking(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, penyewaAktif.getIdPenyewa());
            cs.setString(2, villaTerpilih.getIdVilla());
            cs.setDate(3, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(4, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(5, Integer.parseInt(txtJumlahTamu.getText().trim()));

            String catatan = txtCatatan.getText().trim();
            if (catatan.isEmpty()) cs.setNull(6, Types.VARCHAR);
            else cs.setString(6, catatan);

            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                String idBooking = rs.getString("Id_TrxBooking");
                tampilkanHasilBooking(idBooking);
            }
        } catch (NumberFormatException e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.ERROR, "Gagal booking: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleKembaliBooking() {
        closeOverlay();
    }

    private boolean validasiBooking() {
        if (penyewaAktif == null) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Sesi penyewa tidak valid, silakan ulangi dari awal!");
            return false;
        }
        if (dpCheckin.getValue() == null || dpCheckout.getValue() == null
                || txtJumlahTamu.getText().trim().isEmpty()) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Tanggal dan Jumlah Tamu wajib diisi!");
            return false;
        }
        if (!dpCheckout.getValue().isAfter(dpCheckin.getValue())) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Tanggal Check-Out harus setelah Check-In!");
            return false;
        }
        if (!txtJumlahTamu.getText().trim().matches("^[0-9]+$")) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Jumlah Tamu harus berupa angka!");
            return false;
        }
        return true;
    }

    private void tampilkanHasilBooking(String idBooking) {
        closeOverlay();
        NotifUtil.show(mainView, NotifUtil.Type.SUCCESS,
                "Booking berhasil! Kode booking Anda: " + idBooking + " — tunjukkan ke resepsionis saat check-in.",
                8.0,
                this::resetSesiKiosk);
    }

    // =========================================================
    // TAHAP 4: RESET SESI
    // =========================================================

    private void resetSesiKiosk() {
        penyewaAktif = null;
        villaTerpilih = null;
        Session.clear();

        muatDaftarVilla();
        renderBubble(daftarVilla);
    }

    @FXML
    private void handlePenyewa() {
        // sudah jadi tampilan utama, tidak perlu load fxml lain
    }

    @FXML
    private void handleLogout() {
        MainApp.switchScene("/UIMainView/UITampilan.fxml");
    }
}