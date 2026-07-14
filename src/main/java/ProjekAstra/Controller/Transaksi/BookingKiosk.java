package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Util.NotifUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class BookingKiosk {

    @FXML private Label lblVillaTerpilih, lblGrandHarga;
    @FXML private TextField txtNama, txtNoTelp, txtJumlahTamu;
    @FXML private TextArea txtAlamat, txtCatatan;
    @FXML private DatePicker dpCheckin, dpCheckout;

    private String idVillaTerpilih;
    private String namaVillaTerpilih;
    private double hargaVilla;

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // dipanggil dari DashboardPenyewa sebelum scene ditampilkan
    public void setVillaTerpilih(String idVilla, String namaVilla, double harga) {
        this.idVillaTerpilih = idVilla;
        this.namaVillaTerpilih = namaVilla;
        this.hargaVilla = harga;
        lblVillaTerpilih.setText("Villa: " + namaVilla + " (" + RUPIAH.format(harga) + " / malam)");

        dpCheckin.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
        dpCheckout.valueProperty().addListener((o, old, val) -> hitungGrandHarga());
    }

    private void hitungGrandHarga() {
        LocalDate ci = dpCheckin.getValue();
        LocalDate co = dpCheckout.getValue();
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

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_KioskBooking(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtNama.getText().trim());
            cs.setString(2, txtNoTelp.getText().trim());
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
                tampilkanKodeBooking(idBooking);
            }
        } catch (NumberFormatException e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.ERROR, "Gagal booking: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void tampilkanKodeBooking(String idBooking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Berhasil!");
        alert.setHeaderText("Catat kode booking Anda:");
        alert.setContentText(idBooking + "\n\nTunjukkan kode ini ke resepsionis saat check-in.");
        alert.showAndWait();
        handleKembali();
    }

    @FXML
    private void handleKembali() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/UIMainView/DashboardPenyewa.fxml") // sesuaikan path aslinya
            );
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) txtNama.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            NotifUtil.show(txtNama, NotifUtil.Type.ERROR, "Gagal kembali: " + e.getMessage());
        }
    }

    private boolean validasi() {
        if (txtNama.getText().trim().isEmpty() || txtNoTelp.getText().trim().isEmpty() ||
                dpCheckin.getValue() == null || dpCheckout.getValue() == null ||
                txtJumlahTamu.getText().trim().isEmpty()) {
            NotifUtil.show(txtNama, NotifUtil.Type.WARNING, "Nama, No Telp, Tanggal, dan Jumlah Tamu wajib diisi!");
            return false;
        }
        if (!dpCheckout.getValue().isAfter(dpCheckin.getValue())) {
            NotifUtil.show(txtNama, NotifUtil.Type.WARNING, "Tanggal Check-Out harus setelah Check-In!");
            return false;
        }
        if (!txtJumlahTamu.getText().trim().matches("^[0-9]+$")) {
            NotifUtil.show(txtNama, NotifUtil.Type.WARNING, "Jumlah Tamu harus berupa angka!");
            return false;
        }
        return true;
    }
}