package ProjekAstra.Controller.Dashboard;

import ProjekAstra.MainApp;
import ProjekAstra.Model.Villa;
import ProjekAstra.Model.Fasilitas;
import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Util.NotifUtil;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;

public class DashboardPenyewa {

    @FXML private Label lblJudul;

    @FXML private VBox mainView;
    @FXML private TextField txtCari;
    @FXML private FlowPane bubbleContainer;

    @FXML private StackPane overlayPane;
    @FXML private VBox detailBubble;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailNama;
    @FXML private Label lblDetailKategori;
    @FXML private Label lblDetailKapasitas;
    @FXML private Label lblDetailHarga;
    @FXML private Label lblDetailAlamat;
    @FXML private Label lblDetailPemilik;
    @FXML private FlowPane fasilitasContainer;

    private final List<Villa> daftarVilla = new ArrayList<>();
    private Villa villaTerpilih;

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        muatDaftarVilla();
        renderBubble(daftarVilla);
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
                        rs.getBigDecimal("Harga"),
                        rs.getString("AlamatVilla"),
                        rs.getString("Status")
                ));
            }
        } catch (Exception e) {
            NotifUtil.show(bubbleContainer, NotifUtil.Type.ERROR, "Gagal memuat data villa: " + e.getMessage());
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
        Region placeholderFoto = new Region();
        placeholderFoto.getStyleClass().add("card-image-placeholder");
        placeholderFoto.setPrefSize(230, 190);
        placeholderFoto.setMinSize(230, 190);
        placeholderFoto.setMaxSize(230, 190);

        Label lblNama = new Label(v.getNamaVilla());
        lblNama.getStyleClass().add("card-nama");
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(220);

        Label lblKapasitas = new Label("👥 " + v.getKapasitas() + " orang");
        lblKapasitas.getStyleClass().add("card-kapasitas");

        VBox isiCard = new VBox(8, placeholderFoto, lblNama, lblKapasitas);
        isiCard.setAlignment(Pos.CENTER);
        isiCard.setPadding(new Insets(0, 0, 18, 0));

        Button card = new Button();
        card.setGraphic(isiCard);
        card.getStyleClass().add("villa-card");
        card.setPrefWidth(250);
        card.setOnAction(e -> tampilkanDetail(v));
        return card;
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

    // ====== detail bubble (layar transaksi) ======
    private void tampilkanDetail(Villa v) {
        villaTerpilih = v;

        lblDetailStatus.setText(v.getStatus());
        lblDetailNama.setText(v.getNamaVilla());
        lblDetailKategori.setText("🏷 Kategori: " + v.getNamaKategori());
        lblDetailKapasitas.setText("👥 Kapasitas: " + v.getKapasitas() + " orang");
        lblDetailHarga.setText(RUPIAH.format(v.getHarga()) + " / malam");
        lblDetailAlamat.setText(v.getAlamatVilla());
        lblDetailPemilik.setText(v.getNamaPemilik());

        renderFasilitas(muatFasilitas(v.getIdVilla()));

        overlayPane.setVisible(true);
        overlayPane.setManaged(true);

        detailBubble.setScaleX(0.7);
        detailBubble.setScaleY(0.7);
        detailBubble.setOpacity(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), detailBubble);
        scale.setToX(1); scale.setToY(1);
        FadeTransition fade = new FadeTransition(Duration.millis(220), detailBubble);
        fade.setToValue(1);
        scale.play();
        fade.play();
    }

    @FXML
    private void handleKembaliBubble() {
        FadeTransition fade = new FadeTransition(Duration.millis(180), detailBubble);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            overlayPane.setVisible(false);
            overlayPane.setManaged(false);
            mainView.setVisible(true);
        });
        fade.play();
    }

    @FXML
    private void handleBooking() {
        if (villaTerpilih == null) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/UITransaksi/BookingKiosk.fxml")
            );
            javafx.scene.Parent root = loader.load();

            ProjekAstra.Controller.Transaksi.BookingKiosk controllerKiosk = loader.getController();
            controllerKiosk.setVillaTerpilih(
                    villaTerpilih.getIdVilla(),
                    villaTerpilih.getNamaVilla(),
                    villaTerpilih.getHarga().doubleValue()
            );

            javafx.stage.Stage stage = (javafx.stage.Stage) mainView.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            NotifUtil.show(detailBubble, NotifUtil.Type.ERROR, "Gagal membuka halaman booking: " + e.getMessage());
        }
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