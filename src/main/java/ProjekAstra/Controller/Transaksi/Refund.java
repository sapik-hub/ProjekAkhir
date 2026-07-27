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

    // Cache info booking
    private static class BookingInfo {
        String namaPenyewa, namaVilla, statusBooking;
        BigDecimal grandHarga;
        BookingInfo(String namaPenyewa, String namaVilla, BigDecimal grandHarga, String statusBooking) {
            this.namaPenyewa = namaPenyewa != null ? namaPenyewa : "Tidak Diketahui";
            this.namaVilla = namaVilla != null ? namaVilla : "Tidak Diketahui";
            this.grandHarga = grandHarga != null ? grandHarga : BigDecimal.ZERO;
            this.statusBooking = statusBooking != null ? statusBooking : "Unknown";
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
            if (r != null) {
                populateForm(r);
            }
        });

        cbBooking.valueProperty().addListener((obs, oldVal, newVal) -> {
            tampilkanInfoBooking(newVal);
            if (newVal != null) {
                LocalDate today = LocalDate.now();
                dpTglPengajuan.setValue(today);
                dpTglRefund.setValue(today.plusDays(1));
            }
        });

        dpTglPengajuan.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dpTglRefund.setValue(newVal.plusDays(1));
            }
        });

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
                if (empty || harga == null) {
                    setText(null);
                } else {
                    setText("Rp " + RUPIAH_FORMAT.format(harga));
                }
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Pending":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "Disetujui":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "Ditolak":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        case "Selesai":
                            setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupStatusCombo() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Pending", "Disetujui", "Ditolak", "Selesai"
        ));
        cbStatus.setDisable(true);
    }

    // ✅ FIX: Hanya tampilkan booking yang eligible (tidak termasuk Dibatalkan)
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

                mapBookingInfo.put(idBooking, new BookingInfo(namaPenyewa, namaVilla, grandHarga, statusBooking));

                // ✅ HANYA tampilkan booking yang ELIGIBLE untuk refund
                // Booking Dibatalkan dan Pending TIDAK ditampilkan
                if (statusBooking != null &&
                        (statusBooking.equals("Dikonfirmasi") ||
                                statusBooking.equals("Check In") ||
                                statusBooking.equals("Check Out") ||
                                statusBooking.equals("Selesai"))) {
                    String displayText = idBooking + " - " + (namaPenyewa != null ? namaPenyewa : "Tidak Diketahui");
                    cbBooking.getItems().add(displayText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal memuat data booking: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // ✅ METHOD: Cek apakah booking eligible untuk refund
    private boolean cekBookingEligibleForRefund(String idBooking) {
        if (idBooking == null || idBooking.isEmpty()) return false;

        Koneksi k = new Koneksi();
        try {
            String sql = "SELECT Status_Booking FROM TransaksiBooking WHERE Id_TrxBooking = ?";
            PreparedStatement ps = k.conn.prepareStatement(sql);
            ps.setString(1, idBooking);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String status = rs.getString("Status_Booking");
                System.out.println("DEBUG - Cek Status Booking: " + idBooking + " = " + status);

                // ✅ Hanya status ini yang boleh direfund
                return status != null &&
                        (status.equals("Dikonfirmasi") ||
                                status.equals("Check In") ||
                                status.equals("Check Out") ||
                                status.equals("Selesai"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
        return false;
    }

    private void tampilkanInfoBooking(String comboValue) {
        String idBooking = getIdFromCombo(comboValue);
        BookingInfo info = idBooking != null ? mapBookingInfo.get(idBooking) : null;

        if (info != null) {
            txtPenyewa.setText(info.namaPenyewa);
            txtVilla.setText(info.namaVilla);
            if (info.grandHarga != null) {
                txtGrandHarga.setText("Rp " + RUPIAH_FORMAT.format(info.grandHarga));
            } else {
                txtGrandHarga.setText("Rp 0");
            }
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
            System.out.println("=== START LOADING REFUND TABLE ===");
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllRefund}");
            ResultSet rs = cs.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    String idRefund = rs.getString("Id_TrxRefund");
                    String idBooking = rs.getString("Id_TrxBooking");
                    String namaPenyewa = rs.getString("NamaPenyewa");
                    String namaVilla = rs.getString("Nama_Villa");
                    String namaKaryawan = rs.getString("Nama_Karyawan");
                    String alasan = rs.getString("Alasan_Refund");
                    BigDecimal jumlah = rs.getBigDecimal("Jumlah_Refund");
                    LocalDate tglPengajuan = rs.getDate("Tanggal_Pengajuan") != null ?
                            rs.getDate("Tanggal_Pengajuan").toLocalDate() : null;
                    LocalDate tglRefund = rs.getDate("Tanggal_Refund") != null ?
                            rs.getDate("Tanggal_Refund").toLocalDate() : null;
                    String deskripsi = rs.getString("Deskripsi");
                    String status = rs.getString("Status");

                    TransaksiRefund refund = new TransaksiRefund(
                            idRefund, idBooking, namaPenyewa, namaVilla, namaKaryawan,
                            alasan, jumlah, tglPengajuan, tglRefund, deskripsi, status
                    );
                    listRefund.add(refund);

                } catch (Exception e) {
                    System.err.println("Error pada row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Total data refund dimuat: " + rowCount);
            tableRefund.setItems(listRefund);

        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal memuat data refund: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariRefund(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tableRefund.setItems(listRefund);
            return;
        }
        ObservableList<TransaksiRefund> hasil = FXCollections.observableArrayList();
        String kw = keyword.trim().toLowerCase();
        for (TransaksiRefund r : listRefund) {
            if (r == null) continue;

            String idRefund = r.getIdTrxRefund() != null ? r.getIdTrxRefund().toLowerCase() : "";
            String idBooking = r.getIdTrxBooking() != null ? r.getIdTrxBooking().toLowerCase() : "";
            String namaPenyewa = r.getNamaPenyewa() != null ? r.getNamaPenyewa().toLowerCase() : "";
            String namaVilla = r.getNamaVilla() != null ? r.getNamaVilla().toLowerCase() : "";

            if (namaPenyewa.contains(kw) || namaVilla.contains(kw) ||
                    idRefund.contains(kw) || idBooking.contains(kw)) {
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
            String idBooking = getIdFromCombo(cbBooking.getValue());
            if (idBooking == null) {
                notif(NotifUtil.Type.WARNING, "Booking tidak valid!");
                return;
            }

            // ✅ VALIDASI: Cek apakah booking eligible untuk refund
            if (!cekBookingEligibleForRefund(idBooking)) {
                notif(NotifUtil.Type.WARNING, "Booking ini tidak bisa direfund! (Status: Dibatalkan atau tidak eligible)");
                return;
            }

            CallableStatement cs = k.conn.prepareCall("{call sp_InsertRefund(?, ?, ?, ?)}");
            cs.setString(1, idBooking);
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
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void selectRefundById(String idTrxRefund) {
        if (idTrxRefund == null) return;
        for (TransaksiRefund r : listRefund) {
            if (r != null && idTrxRefund.equals(r.getIdTrxRefund())) {
                populateForm(r);
                tableRefund.getSelectionModel().select(r);
                tableRefund.scrollTo(r);
                break;
            }
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText() == null || txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data refund yang ingin diubah terlebih dahulu!");
            return;
        }
        if (txtAlasan.getText() == null || txtAlasan.getText().trim().isEmpty()) {
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

            String statusBaru = cbStatus.getValue();
            if (statusBaru != null && !statusBaru.isEmpty()) {
                CallableStatement csStatus = k.conn.prepareCall("{call sp_UpdateStatusRefund(?, ?)}");
                csStatus.setString(1, txtId.getText());
                csStatus.setString(2, statusBaru);
                csStatus.execute();
            }

            NotifUtil.show(txtAlasan, NotifUtil.Type.SUCCESS, "Data refund berhasil diubah!",
                    () -> { setClose(); loadTable(); });
        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (txtId.getText() == null || txtId.getText().isEmpty()) {
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
                        e.printStackTrace();
                        notif(NotifUtil.Type.ERROR, "Gagal membatalkan: " + e.getMessage());
                    } finally {
                        try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
                    }
                });
    }

    @FXML
    private void handleReset() {
        setClose();
    }

    @FXML
    private void handleCetakRefund() {
        if (txtId.getText() == null || txtId.getText().isEmpty()) {
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
                sb.append("ID Refund      : ").append(rs.getString("Id_TrxRefund") != null ? rs.getString("Id_TrxRefund") : "-").append("\n");
                sb.append("Penyewa        : ").append(rs.getString("NamaPenyewa") != null ? rs.getString("NamaPenyewa") : "-").append("\n");
                sb.append("Villa          : ").append(rs.getString("Nama_Villa") != null ? rs.getString("Nama_Villa") : "-").append("\n");
                sb.append("Check-in       : ").append(rs.getDate("Tanggal_Checkin") != null ? rs.getDate("Tanggal_Checkin") : "-").append("\n");
                sb.append("Check-out      : ").append(rs.getDate("Tanggal_Checkout") != null ? rs.getDate("Tanggal_Checkout") : "-").append("\n");

                BigDecimal grandHarga = rs.getBigDecimal("Grand_Harga");
                sb.append("Grand Harga    : Rp ").append(grandHarga != null ? RUPIAH_FORMAT.format(grandHarga) : "0").append("\n");

                BigDecimal jumlahRefund = rs.getBigDecimal("Jumlah_Refund");
                sb.append("Jumlah Refund  : Rp ").append(jumlahRefund != null ? RUPIAH_FORMAT.format(jumlahRefund) : "0").append("\n");
                sb.append("Alasan         : ").append(rs.getString("Alasan_Refund") != null ? rs.getString("Alasan_Refund") : "-").append("\n");
                sb.append("Deskripsi      : ").append(rs.getString("Deskripsi") != null ? rs.getString("Deskripsi") : "-").append("\n");
                sb.append("Tgl Pengajuan  : ").append(rs.getDate("Tanggal_Pengajuan") != null ? rs.getDate("Tanggal_Pengajuan") : "-").append("\n");
                sb.append("Tgl Refund     : ").append(rs.getDate("Tanggal_Refund") != null ? rs.getDate("Tanggal_Refund") : "-").append("\n");
                sb.append("Status         : ").append(rs.getString("Status") != null ? rs.getString("Status") : "-").append("\n");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bukti Refund");
                alert.setHeaderText("Bukti Refund - " + (rs.getString("Id_TrxRefund") != null ? rs.getString("Id_TrxRefund") : ""));
                TextArea area = new TextArea(sb.toString());
                area.setEditable(false);
                area.setWrapText(true);
                area.setPrefSize(400, 350);
                alert.getDialogPane().setContent(area);
                alert.showAndWait();
            } else {
                notif(NotifUtil.Type.WARNING, "Data refund tidak ditemukan!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal mencetak bukti refund: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void populateForm(TransaksiRefund r) {
        if (r == null) {
            notif(NotifUtil.Type.WARNING, "Data refund tidak ditemukan!");
            return;
        }

        try {
            txtId.setText(r.getIdTrxRefund() != null ? r.getIdTrxRefund() : "");

            String idBooking = r.getIdTrxBooking();
            if (idBooking != null) {
                // ✅ CEK: Jika booking sudah dibatalkan, tampilkan peringatan
                if (!cekBookingEligibleForRefund(idBooking)) {
                    System.out.println("WARNING: Booking " + idBooking + " sudah tidak eligible untuk refund");
                }
                selectComboById(cbBooking, idBooking, r.getNamaPenyewa());
            } else {
                cbBooking.setValue(null);
            }

            txtPenyewa.setText(r.getNamaPenyewa() != null ? r.getNamaPenyewa() : "");
            txtVilla.setText(r.getNamaVilla() != null ? r.getNamaVilla() : "");

            if (r.getJumlahRefund() != null) {
                txtGrandHarga.setText("Rp " + RUPIAH_FORMAT.format(r.getJumlahRefund()));
            } else {
                txtGrandHarga.setText("Rp 0");
            }

            txtDiprosesOleh.setText(r.getNamaKaryawan() != null ? r.getNamaKaryawan() : "");
            txtAlasan.setText(r.getAlasanRefund() != null ? r.getAlasanRefund() : "");
            txtDeskripsi.setText(r.getDeskripsi() != null ? r.getDeskripsi() : "");

            cbStatus.setDisable(false);
            cbStatus.setValue(r.getStatus() != null ? r.getStatus() : "Pending");

            dpTglPengajuan.setValue(r.getTanggalPengajuan());
            dpTglRefund.setValue(r.getTanggalRefund());

            cbBooking.setDisable(true);

            btnSimpan.setDisable(true);
            btnUbah.setDisable(false);
            btnHapus.setDisable(false);

        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal memuat data refund: " + e.getMessage());
        }
    }

    private void selectComboById(ComboBox<String> combo, String id, String namaPenyewa) {
        if (combo == null || id == null) return;

        String target = id + " - " + (namaPenyewa != null ? namaPenyewa : "Tidak Diketahui");

        for (String item : combo.getItems()) {
            if (item != null && item.startsWith(id + " - ")) {
                combo.setValue(item);
                return;
            }
        }

        combo.getItems().add(target);
        combo.setValue(target);
    }

    private String getIdFromCombo(String comboValue) {
        if (comboValue == null || comboValue.isEmpty()) return null;
        String[] parts = comboValue.split(" - ");
        return parts.length > 0 ? parts[0] : null;
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
        cbStatus.setValue(null);

        LocalDate today = LocalDate.now();
        dpTglPengajuan.setValue(today);
        dpTglRefund.setValue(today.plusDays(1));
        dpTglPengajuan.setDisable(false);
        dpTglRefund.setDisable(false);

        String namaAktif = Session.getNamaKaryawan();
        txtDiprosesOleh.setText(namaAktif != null && !namaAktif.isEmpty() ? namaAktif : "⚠ Sesi tidak ditemukan");

        tableRefund.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);

        generateIdRefund();
    }

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
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private boolean validasiInsert() {
        if (cbBooking.getValue() == null || cbBooking.getValue().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Silakan pilih booking terlebih dahulu!");
            return false;
        }

        if (txtAlasan.getText() == null || txtAlasan.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Alasan Refund wajib diisi!");
            return false;
        }

        if (dpTglPengajuan.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Tanggal Pengajuan wajib diisi!");
            return false;
        }

        String idKaryawan = Session.getIdKaryawan();
        if (idKaryawan == null || idKaryawan.isEmpty()) {
            notif(NotifUtil.Type.ERROR, "Sesi karyawan tidak ditemukan! Silakan logout dan login ulang.");
            return false;
        }

        return true;
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtAlasan, type, msg);
    }
}