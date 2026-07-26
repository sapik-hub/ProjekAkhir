package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.TransaksiRefund;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Refund implements Initializable {

    @FXML private TextField txtId, txtPenyewa, txtVilla, txtGrandHarga, txtDiprosesOleh, txtCari;
    @FXML private TextArea txtAlasan, txtDeskripsi;
    @FXML private ComboBox<String> cbBooking, cbStatus;
    @FXML private DatePicker dpTglPengajuan, dpTglRefund;
    @FXML private Button btnSimpan, btnUbah, btnHapus, btnCetak;

    @FXML private TableView<TransaksiRefund> tableRefund;
    @FXML private TableColumn<TransaksiRefund, String> colId, colBooking, colPenyewa, colVilla,
            colKaryawan, colAlasan, colStatus;
    @FXML private TableColumn<TransaksiRefund, LocalDate> colTglPengajuan, colTglRefund;
    @FXML private TableColumn<TransaksiRefund, BigDecimal> colJumlah;

    private final ObservableList<TransaksiRefund> listRefund = FXCollections.observableArrayList();

    // >>> cache info booking (Id -> Penyewa/Villa/GrandHarga), biar gak query ulang tiap kali combo dipilih
    private static class BookingInfo {
        String namaPenyewa, namaVilla;
        BigDecimal grandHarga;
        BookingInfo(String namaPenyewa, String namaVilla, BigDecimal grandHarga) {
            this.namaPenyewa = namaPenyewa;
            this.namaVilla = namaVilla;
            this.grandHarga = grandHarga;
        }
    }
    private final Map<String, BookingInfo> mapBookingInfo = new HashMap<>();

    private static final DecimalFormat RUPIAH_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        RUPIAH_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupStatusCombo();
        loadComboBooking();
        loadTable();
        setClose();

        tableRefund.setOnMouseClicked(e -> {
            TransaksiRefund r = tableRefund.getSelectionModel().getSelectedItem();
            if (r != null) populateForm(r);
        });

        cbBooking.valueProperty().addListener((obs, oldVal, newVal) -> tampilkanInfoBooking(newVal));

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariRefund(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTrxRefund"));
        colBooking.setCellValueFactory(new PropertyValueFactory<>("idTrxBooking"));
        colPenyewa.setCellValueFactory(new PropertyValueFactory<>("namaPenyewa"));
        colVilla.setCellValueFactory(new PropertyValueFactory<>("namaVilla"));
        colKaryawan.setCellValueFactory(new PropertyValueFactory<>("namaKaryawan"));
        colAlasan.setCellValueFactory(new PropertyValueFactory<>("alasanRefund"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahRefund"));
        colTglPengajuan.setCellValueFactory(new PropertyValueFactory<>("tanggalPengajuan"));
        colTglRefund.setCellValueFactory(new PropertyValueFactory<>("tanggalRefund"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colJumlah.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal harga, boolean empty) {
                super.updateItem(harga, empty);
                setText(empty || harga == null ? null : "Rp " + RUPIAH_FORMAT.format(harga));
            }
        });
    }

    private void setupStatusCombo() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Pending", "Disetujui", "Ditolak", "Selesai"
        ));
    }

    // >>> ambil semua booking, filter yang eligible buat direfund (sesuai fnCekBisaRefund), cache semuanya
    private void loadComboBooking() {
        cbBooking.getItems().clear();
        mapBookingInfo.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllBooking}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                String idBooking = rs.getString("Id_TrxBooking");
                String namaPenyewa = rs.getString("NamaPenyewa");
                String namaVilla = rs.getString("Nama_Villa");
                BigDecimal grandHarga = rs.getBigDecimal("Grand_Harga");
                String statusBooking = rs.getString("Status_Booking");

                mapBookingInfo.put(idBooking, new BookingInfo(namaPenyewa, namaVilla, grandHarga));

                if (statusBooking != null && (statusBooking.equals("Dikonfirmasi") ||
                        statusBooking.equals("Check In") || statusBooking.equals("Check Out") ||
                        statusBooking.equals("Selesai"))) {
                    cbBooking.getItems().add(idBooking + " - " + namaPenyewa);
                }
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data booking: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void tampilkanInfoBooking(String comboValue) {
        String idBooking = getIdFromCombo(comboValue);
        BookingInfo info = idBooking != null ? mapBookingInfo.get(idBooking) : null;

        if (info != null) {
            txtPenyewa.setText(info.namaPenyewa);
            txtVilla.setText(info.namaVilla);
            txtGrandHarga.setText(info.grandHarga != null ? "Rp " + RUPIAH_FORMAT.format(info.grandHarga) : "");
        } else {
            txtPenyewa.clear();
            txtVilla.clear();
            txtGrandHarga.clear();
        }
    }

    private void loadTable() {
        listRefund.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllRefund}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                listRefund.add(new TransaksiRefund(
                        rs.getString("Id_TrxRefund"),
                        rs.getString("Id_TrxBooking"),
                        rs.getString("NamaPenyewa"),
                        rs.getString("Nama_Villa"),
                        rs.getString("Nama_Karyawan"),
                        rs.getString("Alasan_Refund"),
                        rs.getBigDecimal("Jumlah_Refund"),
                        rs.getDate("Tanggal_Pengajuan") != null ? rs.getDate("Tanggal_Pengajuan").toLocalDate() : null,
                        rs.getDate("Tanggal_Refund") != null ? rs.getDate("Tanggal_Refund").toLocalDate() : null,
                        rs.getString("Deskripsi"),
                        rs.getString("Status")
                ));
            }
            tableRefund.setItems(listRefund);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data refund: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariRefund(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableRefund.setItems(listRefund);
            return;
        }
        ObservableList<TransaksiRefund> hasil = FXCollections.observableArrayList();
        String kw = keyword.toLowerCase();
        for (TransaksiRefund r : listRefund) {
            if (r.getNamaPenyewa().toLowerCase().contains(kw) ||
                    r.getNamaVilla().toLowerCase().contains(kw) ||
                    r.getIdTrxRefund().toLowerCase().contains(kw) ||
                    r.getIdTrxBooking().toLowerCase().contains(kw)) {
                hasil.add(r);
            }
        }
        tableRefund.setItems(hasil);
    }

    @FXML
    private void handleSimpan() {
        if (!validasiInsert()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertRefund(?, ?, ?, ?)}");
            cs.setString(1, getIdFromCombo(cbBooking.getValue()));
            cs.setString(2, Session.getIdKaryawan());
            cs.setString(3, txtAlasan.getText().trim());
            cs.setString(4, txtDeskripsi.getText().trim());
            ResultSet rs = cs.executeQuery();

            String idBaru = null;
            if (rs.next()) {
                idBaru = rs.getString("Id_TrxRefund");
            }
            final String idHasil = idBaru;

            NotifUtil.show(txtAlasan, NotifUtil.Type.SUCCESS, "Pengajuan refund berhasil dibuat!",
                    () -> {
                        loadTable();
                        if (idHasil != null) selectRefundById(idHasil);
                        else setClose();
                    });
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void selectRefundById(String idTrxRefund) {
        for (TransaksiRefund r : listRefund) {
            if (r.getIdTrxRefund().equals(idTrxRefund)) {
                populateForm(r);
                tableRefund.getSelectionModel().select(r);
                break;
            }
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data refund yang ingin diubah terlebih dahulu!");
            return;
        }
        if (txtAlasan.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Alasan Refund wajib diisi!");
            return;
        }

        ConfirmUtil.show(txtAlasan,
                "Simpan perubahan data refund " + txtId.getText() + "?",
                this::prosesUbah);
    }

    private void prosesUbah() {
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateRefund(?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, txtAlasan.getText().trim());
            cs.setString(3, txtDeskripsi.getText().trim());
            cs.execute();

            if (cbStatus.getValue() != null) {
                CallableStatement csStatus = k.conn.prepareCall("{call sp_UpdateStatusRefund(?, ?)}");
                csStatus.setString(1, txtId.getText());
                csStatus.setString(2, cbStatus.getValue());
                csStatus.execute();
            }

            NotifUtil.show(txtAlasan, NotifUtil.Type.SUCCESS, "Data refund berhasil diubah!",
                    () -> { setClose(); loadTable(); });
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data refund yang ingin dibatalkan terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtAlasan,
                "Yakin ingin membatalkan pengajuan refund " + txtId.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        CallableStatement cs = k.conn.prepareCall("{call sp_UpdateStatusRefund(?, ?)}");
                        cs.setString(1, txtId.getText());
                        cs.setString(2, "Ditolak");
                        cs.execute();

                        NotifUtil.show(txtAlasan, NotifUtil.Type.SUCCESS, "Pengajuan refund berhasil dibatalkan!",
                                () -> { setClose(); loadTable(); });
                    } catch (Exception e) {
                        notif(NotifUtil.Type.ERROR, "Gagal membatalkan: " + e.getMessage());
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
    private void handleCetakRefund() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data refund yang ingin dicetak terlebih dahulu!");
            return;
        }

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_CetakRefund(?)}");
            cs.setString(1, txtId.getText());
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("=== BUKTI REFUND ===\n\n");
                sb.append("ID Refund      : ").append(rs.getString("Id_TrxRefund")).append("\n");
                sb.append("Penyewa        : ").append(rs.getString("NamaPenyewa")).append("\n");
                sb.append("Villa          : ").append(rs.getString("Nama_Villa")).append("\n");
                sb.append("Check-in       : ").append(rs.getDate("Tanggal_Checkin")).append("\n");
                sb.append("Check-out      : ").append(rs.getDate("Tanggal_Checkout")).append("\n");
                sb.append("Grand Harga    : Rp ").append(RUPIAH_FORMAT.format(rs.getBigDecimal("Grand_Harga"))).append("\n");
                sb.append("Jumlah Refund  : Rp ").append(RUPIAH_FORMAT.format(rs.getBigDecimal("Jumlah_Refund"))).append("\n");
                sb.append("Alasan         : ").append(rs.getString("Alasan_Refund")).append("\n");
                sb.append("Deskripsi      : ").append(rs.getString("Deskripsi") == null ? "-" : rs.getString("Deskripsi")).append("\n");
                sb.append("Tgl Pengajuan  : ").append(rs.getDate("Tanggal_Pengajuan")).append("\n");
                sb.append("Tgl Refund     : ").append(rs.getDate("Tanggal_Refund") == null ? "-" : rs.getDate("Tanggal_Refund")).append("\n");
                sb.append("Status         : ").append(rs.getString("Status")).append("\n");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bukti Refund");
                alert.setHeaderText("Bukti Refund - " + rs.getString("Id_TrxRefund"));
                TextArea area = new TextArea(sb.toString());
                area.setEditable(false);
                area.setWrapText(true);
                area.setPrefSize(400, 350);
                alert.getDialogPane().setContent(area);
                alert.showAndWait();
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mencetak bukti refund: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void populateForm(TransaksiRefund r) {
        txtId.setText(r.getIdTrxRefund());
        selectComboById(cbBooking, r.getIdTrxBooking(), r.getNamaPenyewa());

        txtPenyewa.setText(r.getNamaPenyewa());
        txtVilla.setText(r.getNamaVilla());
        txtGrandHarga.setText(r.getJumlahRefund() != null ? "Rp " + RUPIAH_FORMAT.format(r.getJumlahRefund()) : "");

        // >>> Diproses Oleh tetap nampilin siapa yang ORIGINAL mengajukan refund ini (dari data tersimpan),
        // bukan karyawan yang sedang login sekarang - biar histori gak berubah pas cuma dibuka/edit.
        txtDiprosesOleh.setText(r.getNamaKaryawan());

        txtAlasan.setText(r.getAlasanRefund());
        txtDeskripsi.setText(r.getDeskripsi());

        cbStatus.setDisable(false);
        cbStatus.setValue(r.getStatus());

        dpTglPengajuan.setValue(r.getTanggalPengajuan());
        dpTglRefund.setValue(r.getTanggalRefund());

        cbBooking.setDisable(true); // booking gak boleh diganti pas edit

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
    }

    // >>> pastikan combo bisa nampilin value walau item itu udah gak eligible lagi (misal booking-nya udah ganti status)
    private void selectComboById(ComboBox<String> combo, String id, String namaPenyewa) {
        for (String item : combo.getItems()) {
            if (item.startsWith(id + " - ")) {
                combo.setValue(item);
                return;
            }
        }
        combo.setValue(id + " - " + namaPenyewa);
    }

    private String getIdFromCombo(String comboValue) {
        if (comboValue == null) return null;
        return comboValue.split(" - ")[0];
    }

    private void setClose() {
        txtPenyewa.clear();
        txtVilla.clear();
        txtGrandHarga.clear();
        txtAlasan.clear();
        txtDeskripsi.clear();

        cbBooking.setValue(null);
        cbBooking.setDisable(false);

        cbStatus.setDisable(true);
        cbStatus.setValue("Pending"); // >>> status transaksi default sesuai SQL

        dpTglPengajuan.setValue(LocalDate.now()); // >>> preview: refund baru otomatis tanggal hari ini
        dpTglPengajuan.setDisable(true);
        dpTglRefund.setValue(null);
        dpTglRefund.setDisable(true);

        // >>> FIX: "Diproses Oleh" otomatis terisi nama karyawan/superadmin yang sedang login.
        // Kalau kosong, berarti Session.setKaryawan(...) belum dipanggil pas proses login -
        // munculin placeholder yang jelas biar ketahuan, bukan field kosong tanpa penjelasan.
        String namaAktif = Session.getNamaKaryawan();
        txtDiprosesOleh.setText(namaAktif != null && !namaAktif.isEmpty() ? namaAktif : "⚠ Sesi tidak ditemukan");

        tableRefund.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);

        generateIdRefund(); // >>> preview ID refund berikutnya
    }

    // >>> preview ID refund berikutnya, sama pola kayak generateIdVilla() di CrudVilla
    private void generateIdRefund() {
        Koneksi k = new Koneksi();
        try {
            String sql = "SELECT dbo.fnNextIdTrxRefund() AS IdRefund";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("IdRefund"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private boolean validasiInsert() {
        if (cbBooking.getValue() == null || txtAlasan.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Booking dan Alasan Refund wajib diisi!");
            return false;
        }
        // >>> FIX: cegah insert gagal diam-diam karena Id_Karyawan NULL (kolom NOT NULL + ada FK).
        // Kasih pesan jelas ke user daripada nunggu SQL Server nolak dengan error mentah.
        if (Session.getIdKaryawan() == null || Session.getIdKaryawan().isEmpty()) {
            notif(NotifUtil.Type.ERROR, "Sesi karyawan tidak ditemukan! Silakan logout dan login ulang.");
            return false;
        }
        return true;
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtAlasan, type, msg);
    }
}