package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.Penyewa;
import ProjekAstra.Util.NotifUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.NumberFormat;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class BookingKiosk {

    @FXML private Label lblVillaTerpilih, lblGrandHarga;
    @FXML private ComboBox<Penyewa> cbPenyewa;
    @FXML private TextArea txtAlamat, txtCatatan;
    @FXML private DatePicker dpCheckin, dpCheckout;
    @FXML private TextField txtJumlahTamu;

    private String idVillaTerpilih;
    private double hargaVilla;

    private Runnable onKembali;
    private Consumer<String> onBookingBerhasil;

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        dpCheckin.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
        dpCheckout.valueProperty().addListener((o, old, val) -> hitungGrandHarga());

        cbPenyewa.setConverter(new javafx.util.StringConverter<>() {
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
                txtAlamat.setText(val.getAlamat() == null ? "" : val.getAlamat());
            }
        });
    }

    public void setOnKembali(Runnable r) {
        this.onKembali = r;
    }

    public void setOnBookingBerhasil(Consumer<String> c) {
        this.onBookingBerhasil = c;
    }

    // dipanggil dari DashboardPenyewa sebelum bubble ini ditampilkan
    public void tampilkanUntukVilla(String idVilla, String namaVilla, double harga, List<Penyewa> daftarPenyewa) {
        this.idVillaTerpilih = idVilla;
        this.hargaVilla = harga;

        lblVillaTerpilih.setText("Villa: " + namaVilla + " (" + RUPIAH.format(harga) + " / malam)");

        cbPenyewa.getItems().setAll(daftarPenyewa);
        cbPenyewa.setValue(null);
        txtAlamat.clear();
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
        txtJumlahTamu.clear();
        txtCatatan.clear();
        lblGrandHarga.setText("");
    }

    private void hitungGrandHarga() {
        var ci = dpCheckin.getValue();
        var co = dpCheckout.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            long malam = ChronoUnit.DAYS.between(ci, co);
            lblGrandHarga.setText("Total: " + RUPIAH.format(hargaVilla * malam));
        } else {
            lblGrandHarga.setText("");
        }
    }

    @FXML
    private void handleBookingSekarang() {
        if (!validasi()) return;

        Penyewa penyewa = cbPenyewa.getValue();

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_KioskBooking(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, penyewa.getNama());
            cs.setString(2, penyewa.getNoTelp());
            cs.setString(3, txtAlamat.getText().trim());
            cs.setString(4, idVillaTerpilih);
            cs.setDate(5, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(6, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(7, Integer.parseInt(txtJumlahTamu.getText().trim()));

            String catatan = txtCatatan.getText().trim();
            if (catatan.isEmpty()) cs.setNull(8, Types.VARCHAR);
            else cs.setString(8, catatan);

            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                String idBooking = rs.getString("Id_trsBooking");
                if (onBookingBerhasil != null) onBookingBerhasil.accept(idBooking);
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
    private void handleKembali() {
        if (onKembali != null) onKembali.run();
    }

    private boolean validasi() {
        if (cbPenyewa.getValue() == null) {
            NotifUtil.show(cbPenyewa, NotifUtil.Type.WARNING, "Pilih penyewa terlebih dahulu!");
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
}