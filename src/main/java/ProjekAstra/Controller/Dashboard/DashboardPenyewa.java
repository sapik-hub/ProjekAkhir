package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import ProjekAstra.Model.Villa;
import ProjekAstra.Model.Fasilitas;
import ProjekAstra.Model.Penyewa;
import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Util.FileUtil;
import ProjekAstra.Util.NotifUtil;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.NumberFormat;
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

    // ---- booking bubble (sudah inline, bukan fx:include lagi) ----
    @FXML private VBox bookingPane;
    @FXML private Label lblVillaTerpilih;
    @FXML private ComboBox<Penyewa> cbPenyewa;
    @FXML private ComboBox<String> cbMetodePembayaran;
    @FXML private TextArea txtAlamatBooking;
    @FXML private TextArea txtCatatan;
    @FXML private DatePicker dpCheckin;
    @FXML private DatePicker dpCheckout;
    @FXML private TextField txtJumlahTamu;
    @FXML private Label lblGrandHarga;

    // ---- result bubble ----
    @FXML private VBox resultBubble;
    @FXML private Label lblKodeBooking;

    private final List<Villa> daftarVilla = new ArrayList<>();
    private final List<Penyewa> daftarPenyewa = new ArrayList<>();
    private Villa villaTerpilih;

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        muatDaftarVilla();
        renderBubble(daftarVilla);

        muatDaftarPenyewa();
        setupComboPenyewa();
        setupComboMetodePembayaran();

        dpCheckin.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
        dpCheckout.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
    }

    private void setupComboPenyewa() {
        cbPenyewa.setConverter(new StringConverter<>() {
            @Override
            public String toString(Penyewa p) {
                return p == null ? "" : p.getNama() + " (" + p.getIdPenyewa() + ")";
            }
            @Override
            public Penyewa fromString(String s) { return null; }
        });
        cbPenyewa.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Penyewa p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNama() + " (" + p.getIdPenyewa() + ")");
            }
        });
        cbPenyewa.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                txtAlamatBooking.setText(val.getAlamat() == null ? "" : val.getAlamat());
            }
        });
    }

    private void setupComboMetodePembayaran() {
        cbMetodePembayaran.getItems().setAll("Cash", "Transfer Bank", "QRIS");
    }

    // ====== ambil semua villa dari database ======
    private void muatDaftarVilla() {
        daftarVilla.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                daftarVilla.add(new Villa(
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
        } catch (Exception e) {
            NotifUtil.show(bubbleContainer, NotifUtil.Type.ERROR, "Gagal memuat data villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // ====== ambil semua penyewa (buat combo box di pane booking) ======
    private void muatDaftarPenyewa() {
        daftarPenyewa.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllPenyewa}");
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                daftarPenyewa.add(new Penyewa(
                        rs.getString("IdPenyewa"),
                        rs.getString("Nama"),
                        rs.getString("NoTelp"),
                        rs.getString("NoKtp"),
                        rs.getDate("TglLahir") == null ? null : rs.getDate("TglLahir").toLocalDate(),
                        rs.getString("Alamat"),
                        rs.getString("Username")
                ));
            }
        } catch (Exception e) {
            NotifUtil.show(bubbleContainer, NotifUtil.Type.ERROR, "Gagal memuat data penyewa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // ====== ambil fasilitas by IdVilla dari database ======
    private List<Fasilitas> muatFasilitas(String idVilla) {
        List<Fasilitas> hasil = new ArrayList<>();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetFasilitasByVilla(?)}");
            cs.setString(1, idVilla);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                hasil.add(new Fasilitas(
                        rs.getString("IdFasilitas"),
                        rs.getString("NamaVilla"),
                        rs.getString("NamaFasilitas"),
                        rs.getInt("Jumlah"),
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

    private void renderFasilitas(List<Fasilitas> list) {
        fasilitasContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label kosong = new Label("Belum ada fasilitas");
            kosong.getStyleClass().add("detail-desc");
            fasilitasContainer.getChildren().add(kosong);
            return;
        }
        for (Fasilitas f : list) {
            String teks = f.getNamaFasilitas() + (f.getJumlah() > 1 ? " (" + f.getJumlah() + ")" : "");
            Label chip = new Label(teks);
            chip.getStyleClass().add("fasilitas-chip");
            fasilitasContainer.getChildren().add(chip);
        }
    }

    // ====== render bubble grid ======
    private void renderBubble(List<Villa> list) {
        bubbleContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label kosong = new Label("Villa tidak ditemukan 🙁");
            kosong.getStyleClass().add("bubble-empty");
            bubbleContainer.getChildren().add(kosong);
            return;
        }
        for (Villa v : list) {
            bubbleContainer.getChildren().add(buatBubble(v));
        }
    }

    private Button buatBubble(Villa v) {
        Node fotoNode = buatFotoNode(v.getFoto(), 230, 190);

        Label lblNama = new Label(v.getNamaVilla());
        lblNama.getStyleClass().add("card-nama");
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(220);

        Label lblKapasitas = new Label("👥 " + v.getKapasitas() + " orang");
        lblKapasitas.getStyleClass().add("card-kapasitas");

        Label lblHarga = new Label(RUPIAH.format(v.getHargaWeekday()) + " / malam (weekday)");
        lblHarga.getStyleClass().add("card-kapasitas");

        VBox isiCard = new VBox(8, fotoNode, lblNama, lblKapasitas, lblHarga);
        isiCard.setAlignment(Pos.CENTER);
        isiCard.setPadding(new Insets(0, 0, 18, 0));

        Button card = new Button();
        card.setGraphic(isiCard);
        card.getStyleClass().add("villa-card");
        card.setPrefWidth(250);
        card.setOnAction(e -> tampilkanDetail(v));
        return card;
    }

    // Kalau ada foto & filenya ada di disk, tampilkan ImageView. Kalau tidak, placeholder abu-abu.
    private Node buatFotoNode(String namaFoto, double width, double height) {
        if (namaFoto != null) {
            String fullPath = FileUtil.getFullPath(namaFoto);
            File file = new File(fullPath);
            if (file.exists()) {
                ImageView iv = new ImageView(new Image(file.toURI().toString()));
                iv.setFitWidth(width);
                iv.setFitHeight(height);
                iv.setPreserveRatio(false);
                return iv;
            }
        }
        Region placeholder = new Region();
        placeholder.getStyleClass().add("card-image-placeholder");
        placeholder.setPrefSize(width, height);
        placeholder.setMinSize(width, height);
        placeholder.setMaxSize(width, height);
        return placeholder;
    }

    // ====== search (nama atau kapasitas) ======
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

    // ====================================================================
    //  BUBBLE ENGINE (dipakai bareng: detail, booking, result)
    // ====================================================================

    private Node[] semuaBubble() {
        return new Node[]{ detailBubble, bookingPane, resultBubble };
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

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), bubble);
        scale.setToX(1);
        scale.setToY(1);
        FadeTransition fade = new FadeTransition(Duration.millis(220), bubble);
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
        FadeTransition fade = new FadeTransition(Duration.millis(180), current);
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

    // ====== detail bubble (layar transaksi) ======
    private void tampilkanDetail(Villa v) {
        villaTerpilih = v;

        lblDetailStatus.setText(v.getStatus());
        lblDetailNama.setText(v.getNamaVilla());
        lblDetailKategori.setText("🏷 Kategori: " + v.getNamaKategori());
        lblDetailKapasitas.setText("👥 Kapasitas: " + v.getKapasitas() + " orang");
        lblDetailHargaWeekday.setText("Weekday: " + RUPIAH.format(v.getHargaWeekday()) + " / malam");
        lblDetailHargaWeekend.setText("Weekend (Jum-Sab/Sab-Min): " + RUPIAH.format(v.getHargaWeekend()) + " / malam");
        lblDetailAlamat.setText(v.getAlamatVilla());
        lblDetailPemilik.setText(v.getNamaPemilik());

        if (imgDetailFoto != null) {
            if (v.getFoto() != null) {
                File file = new File(FileUtil.getFullPath(v.getFoto()));
                imgDetailFoto.setImage(file.exists() ? new Image(file.toURI().toString()) : null);
            } else {
                imgDetailFoto.setImage(null);
            }
        }

        renderFasilitas(muatFasilitas(v.getIdVilla()));

        showOverlayBubble(detailBubble);
    }

    @FXML
    private void handleKembaliBubble() {
        closeOverlay();
    }

    // ====== buka form booking ======
    @FXML
    private void handleBooking() {
        if (villaTerpilih == null) return;

        lblVillaTerpilih.setText("Villa: " + villaTerpilih.getNamaVilla()
                + " (Weekday " + RUPIAH.format(villaTerpilih.getHargaWeekday())
                + " / Weekend " + RUPIAH.format(villaTerpilih.getHargaWeekend()) + ")");

        cbPenyewa.getItems().setAll(daftarPenyewa);
        cbPenyewa.setValue(null);
        cbMetodePembayaran.setValue(null);
        txtAlamatBooking.clear();
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
        txtJumlahTamu.clear();
        txtCatatan.clear();
        lblGrandHarga.setText("");

        showOverlayBubble(bookingPane);
    }

    // Estimasi tampilan doang; total resmi tetap dihitung server lewat fnHitungGrandHarga saat insert
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

        Penyewa penyewa = cbPenyewa.getValue();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertBooking(?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, penyewa.getIdPenyewa());
            cs.setString(2, villaTerpilih.getIdVilla());
            cs.setDate(3, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(4, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(5, Integer.parseInt(txtJumlahTamu.getText().trim()));

            String catatan = txtCatatan.getText().trim();
            if (catatan.isEmpty()) cs.setNull(6, Types.VARCHAR);
            else cs.setString(6, catatan);

            cs.setString(7, cbMetodePembayaran.getValue());

            // StatusBooking otomatis 'Menunggu Konfirmasi' di server, tidak dikirim dari sini

            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                String idBooking = rs.getString("IdTrsBooking");
                tampilkanHasilBooking(idBooking);
            }
        } catch (NumberFormatException e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.ERROR, "Gagal bookingg: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleKembaliBooking() {
        closeOverlay();
    }

    private boolean validasiBooking() {
        if (cbPenyewa.getValue() == null) {
            NotifUtil.show(cbPenyewa, NotifUtil.Type.WARNING, "Pilih penyewa terlebih dahulu!");
            return false;
        }
        if (cbMetodePembayaran.getValue() == null) {
            NotifUtil.show(cbMetodePembayaran, NotifUtil.Type.WARNING, "Pilih metode pembayaran terlebih dahulu!");
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

    // ====== hasil booking (pengganti Alert) ======
    private void tampilkanHasilBooking(String idBooking) {
        lblKodeBooking.setText(idBooking);
        showOverlayBubble(resultBubble);
    }

    @FXML
    private void handleTutupResult() {
        closeOverlay();
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