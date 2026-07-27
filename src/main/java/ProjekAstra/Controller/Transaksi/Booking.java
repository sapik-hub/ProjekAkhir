package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.TransaksiBooking;
import ProjekAstra.Model.VillaInfo;
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
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Booking implements Initializable {

    @FXML private TextField txtId, txtJumlahTamu, txtGrandHarga, txtCari;
    @FXML private TextArea txtCatatan;
    @FXML private ComboBox<String> cbPenyewa, cbVilla, cbStatus;
    @FXML private DatePicker dpCheckin, dpCheckout;
    @FXML private Button btnSimpan, btnUbah, btnHapus;

    @FXML private TableView<TransaksiBooking> tableBooking;
    @FXML private TableColumn<TransaksiBooking, String> colId, colPenyewa, colVilla, colStatus;
    @FXML private TableColumn<TransaksiBooking, LocalDate> colCheckin, colCheckout;
    @FXML private TableColumn<TransaksiBooking, Integer> colTamu;
    @FXML private TableColumn<TransaksiBooking, BigDecimal> colHarga;

    private final ObservableList<TransaksiBooking> listBooking = FXCollections.observableArrayList();

    // Cache info villa
    private final Map<String, VillaInfo> mapVillaInfo = new HashMap<>();
    private VillaInfo villaTerpilih = null;

    // Formatter untuk Rupiah
    private static final DecimalFormat RUPIAH_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setCurrencySymbol("Rp ");
        RUPIAH_FORMAT = new DecimalFormat("Rp #,##0", symbols);
    }

    private static final String[] DAFTAR_STATUS = {
            "Pending", "Dikonfirmasi", "Check In", "Check Out", "Selesai", "Dibatalkan"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadComboPenyewa();
        loadComboVillaWithHarga();
        setupStatusCombo();
        loadTable();
        setClose();

        tableBooking.setOnMouseClicked(e -> {
            TransaksiBooking b = tableBooking.getSelectionModel().getSelectedItem();
            if (b != null) populateForm(b);
        });

        dpCheckin.valueProperty().addListener((obs, oldVal, newVal) -> hitungGrandHarga());
        dpCheckout.valueProperty().addListener((obs, oldVal, newVal) -> hitungGrandHarga());

        cbVilla.valueProperty().addListener((obs, oldVal, newVal) -> {
            villaTerpilih = getVillaInfo(newVal);
            hitungGrandHarga();
        });

        txtCari.textProperty().addListener((obs, oldVal, newVal) -> cariBooking(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTrsBooking"));
        colPenyewa.setCellValueFactory(new PropertyValueFactory<>("namaPenyewa"));
        colVilla.setCellValueFactory(new PropertyValueFactory<>("namaVilla"));
        colCheckin.setCellValueFactory(new PropertyValueFactory<>("tanggalCheckin"));
        colCheckout.setCellValueFactory(new PropertyValueFactory<>("tanggalCheckOut"));
        colTamu.setCellValueFactory(new PropertyValueFactory<>("jumlahTamu"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("grandHarga"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusBooking"));

        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal harga, boolean empty) {
                super.updateItem(harga, empty);
                if (empty || harga == null) {
                    setText(null);
                } else {
                    setText(RUPIAH_FORMAT.format(harga));
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
                    return;
                }
                setText(status);
                setStyle("-fx-font-weight: bold; -fx-text-fill: " + warnaStatus(status) + ";");
            }
        });
    }

    private String warnaStatus(String status) {
        return switch (status) {
            case "Pending" -> "#E0972C";
            case "Dikonfirmasi" -> "#4A90D9";
            case "Check In" -> "#2FA88C";
            case "Check Out" -> "#8E6FCE";
            case "Selesai" -> "#4CAF7D";
            case "Dibatalkan" -> "#D9534F";
            default -> "#4A5568";
        };
    }

    private void setupStatusCombo() {
        cbStatus.setItems(FXCollections.observableArrayList(DAFTAR_STATUS));
        cbStatus.setDisable(true);
    }

    private void loadComboPenyewa() {
        cbPenyewa.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllPenyewa}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbPenyewa.getItems().add(rs.getString("Id_Penyewa") + " - " + rs.getString("Nama"));
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data penyewa: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void loadComboVillaWithHarga() {
        cbVilla.getItems().clear();
        mapVillaInfo.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                String idVilla = rs.getString("Id_Villa");
                String namaVilla = rs.getString("Nama_Villa");
                BigDecimal hargaWeekday = rs.getBigDecimal("HargaWeekday");
                BigDecimal hargaWeekend = rs.getBigDecimal("HargaWeekend");

                if (hargaWeekday == null) hargaWeekday = BigDecimal.ZERO;
                if (hargaWeekend == null) hargaWeekend = BigDecimal.ZERO;

                mapVillaInfo.put(idVilla, new VillaInfo(idVilla, namaVilla, hargaWeekday, hargaWeekend));
                cbVilla.getItems().add(idVilla + " - " + namaVilla);
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data villa: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private VillaInfo getVillaInfo(String comboValue) {
        if (comboValue == null) return null;
        String idVilla = comboValue.split(" - ")[0];
        return mapVillaInfo.get(idVilla);
    }

    private void hitungGrandHarga() {
        LocalDate checkin = dpCheckin.getValue();
        LocalDate checkout = dpCheckout.getValue();

        if (checkin == null || checkout == null || !checkout.isAfter(checkin)) {
            txtGrandHarga.clear();
            return;
        }

        if (villaTerpilih == null) {
            txtGrandHarga.clear();
            return;
        }

        BigDecimal hargaWeekday = villaTerpilih.getHargaWeekday();
        BigDecimal hargaWeekend = villaTerpilih.getHargaWeekend();

        if (hargaWeekday == null || hargaWeekend == null) {
            txtGrandHarga.clear();
            return;
        }

        if (hargaWeekday.compareTo(BigDecimal.ZERO) <= 0) {
            txtGrandHarga.clear();
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate current = checkin;

        while (current.isBefore(checkout)) {
            java.time.DayOfWeek day = current.getDayOfWeek();
            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
                total = total.add(hargaWeekend);
            } else {
                total = total.add(hargaWeekday);
            }
            current = current.plusDays(1);
        }

        txtGrandHarga.setText(RUPIAH_FORMAT.format(total));
    }

    private void loadTable() {
        listBooking.clear();
        Koneksi k = new Koneksi();
        try {
            System.out.println("=== LOAD BOOKING TABLE START ===");

            String sql = "SELECT " +
                    "    b.Id_TrxBooking, " +
                    "    ISNULL(p.Nama, 'Tidak Diketahui') AS NamaPenyewa, " +
                    "    ISNULL(v.Nama_Villa, 'Tidak Diketahui') AS Nama_Villa, " +
                    "    b.Tanggal_Checkin, " +
                    "    b.Tanggal_Checkout, " +
                    "    b.Jumlah_Tamu, " +
                    "    ISNULL(b.Grand_Harga, 0) AS Grand_Harga, " +
                    "    b.Status_Booking, " +
                    "    b.Tanggal_Booking " +
                    "FROM TransaksiBooking b " +
                    "LEFT JOIN Penyewa p ON b.Id_Penyewa = p.Id_Penyewa " +
                    "LEFT JOIN Villa v ON b.Id_Villa = v.Id_Villa " +
                    "ORDER BY b.Id_TrxBooking ASC";

            PreparedStatement ps = k.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                BigDecimal grandHarga = rs.getBigDecimal("Grand_Harga");
                if (grandHarga == null) grandHarga = BigDecimal.ZERO;

                TransaksiBooking booking = new TransaksiBooking(
                        rs.getString("Id_TrxBooking"),
                        rs.getString("NamaPenyewa"),
                        rs.getString("Nama_Villa"),
                        rs.getDate("Tanggal_Checkin") != null ? rs.getDate("Tanggal_Checkin").toLocalDate() : null,
                        rs.getDate("Tanggal_Checkout") != null ? rs.getDate("Tanggal_Checkout").toLocalDate() : null,
                        rs.getInt("Jumlah_Tamu"),
                        grandHarga,
                        rs.getString("Status_Booking"),
                        rs.getDate("Tanggal_Booking") != null ? rs.getDate("Tanggal_Booking").toLocalDate() : null
                );
                listBooking.add(booking);

                System.out.println("DEBUG - Row " + rowCount + ": " + rs.getString("Id_TrxBooking"));
            }

            System.out.println("Total data booking dimuat: " + rowCount);
            tableBooking.setItems(listBooking);

        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal memuat data booking: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
            System.out.println("=== LOAD BOOKING TABLE END ===");
        }
    }

    private void cariBooking(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableBooking.setItems(listBooking);
            return;
        }

        ObservableList<TransaksiBooking> hasil = FXCollections.observableArrayList();
        String kw = keyword.toLowerCase();

        for (TransaksiBooking b : listBooking) {
            if (b == null) continue;

            String id = b.getIdTrsBooking() != null ? b.getIdTrsBooking().toLowerCase() : "";
            String penyewa = b.getNamaPenyewa() != null ? b.getNamaPenyewa().toLowerCase() : "";
            String villa = b.getNamaVilla() != null ? b.getNamaVilla().toLowerCase() : "";
            String status = b.getStatusBooking() != null ? b.getStatusBooking().toLowerCase() : "";

            if (id.contains(kw) || penyewa.contains(kw) || villa.contains(kw) || status.contains(kw)) {
                hasil.add(b);
            }
        }

        tableBooking.setItems(hasil);
    }

    @FXML
    private void handleSimpan() {
        if (!validasi()) return;

        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_InsertBooking(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, getIdFromCombo(cbPenyewa.getValue()));
            cs.setString(2, getIdFromCombo(cbVilla.getValue()));
            cs.setDate(3, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(4, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(5, Integer.parseInt(txtJumlahTamu.getText().trim()));
            cs.setString(6, txtCatatan.getText().trim());

            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                BigDecimal grandHarga = rs.getBigDecimal("Grand_Harga");
                String idBooking = rs.getString("Id_TrxBooking");

                if (grandHarga == null) grandHarga = BigDecimal.ZERO;

                notif(NotifUtil.Type.SUCCESS,
                        "Booking berhasil dibuat!\nID: " + idBooking +
                                "\nTotal: " + RUPIAH_FORMAT.format(grandHarga));

                setClose();
                loadTable();
            }
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data booking yang ingin diubah terlebih dahulu!");
            return;
        }
        if (!validasi()) return;
        if (cbStatus.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Pilih status booking terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtJumlahTamu,
                "Ubah data booking " + txtId.getText() + " menjadi status \"" + cbStatus.getValue() + "\"?",
                this::prosesUbah);
    }

    private void prosesUbah() {
        Koneksi k = new Koneksi();
        try {
            // 1. Update data booking
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateBooking(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbVilla.getValue()));
            cs.setDate(3, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(4, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(5, Integer.parseInt(txtJumlahTamu.getText().trim()));
            cs.setString(6, txtCatatan.getText().trim());
            cs.execute();

            // 2. Update status
            CallableStatement csStatus = k.conn.prepareCall("{call sp_UpdateStatusBooking(?, ?, ?)}");
            csStatus.setString(1, txtId.getText());
            String idKaryawan = Session.getIdKaryawan();
            if (idKaryawan == null || idKaryawan.isEmpty()) {
                csStatus.setNull(2, Types.VARCHAR);
            } else {
                csStatus.setString(2, idKaryawan);
            }
            csStatus.setString(3, cbStatus.getValue());
            csStatus.execute();

            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.SUCCESS, "Booking berhasil diubah!",
                    () -> { setClose(); loadTable(); });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            e.printStackTrace();
            notif(NotifUtil.Type.ERROR, "Gagal mengubah: " + e.getMessage());
        } finally {
            try { if (k.conn != null) k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===========================================================
    // ✅ PERBAIKAN: handleHapus - csStatus diganti dengan cs
    // ===========================================================
    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data booking yang ingin dibatalkan terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtJumlahTamu,
                "Yakin ingin membatalkan booking " + txtId.getText() + "?",
                () -> {
                    Koneksi k = new Koneksi();
                    try {
                        // ✅ PERBAIKAN: Gunakan satu CallableStatement, nama variabel cs
                        CallableStatement cs = k.conn.prepareCall("{call sp_UpdateStatusBooking(?, ?, ?)}");
                        cs.setString(1, txtId.getText());

                        String idKaryawan = Session.getIdKaryawan();
                        if (idKaryawan == null || idKaryawan.isEmpty()) {
                            cs.setNull(2, Types.VARCHAR);
                        } else {
                            cs.setString(2, idKaryawan);
                        }

                        cs.setString(3, "Dibatalkan");
                        cs.execute();

                        NotifUtil.show(txtJumlahTamu, NotifUtil.Type.SUCCESS, "Booking berhasil dibatalkan!",
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
    private void handleCetakStruk() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data booking yang ingin dicetak terlebih dahulu!");
            return;
        }
        notif(NotifUtil.Type.SUCCESS, "Fitur cetak struk siap dihubungkan ke sp_CetakStrukBooking.");
    }

    private void populateForm(TransaksiBooking b) {
        txtId.setText(b.getIdTrsBooking());
        selectComboByName(cbPenyewa, b.getNamaPenyewa());
        selectComboByName(cbVilla, b.getNamaVilla());

        villaTerpilih = getVillaInfo(cbVilla.getValue());

        dpCheckin.setValue(b.getTanggalCheckin());
        dpCheckout.setValue(b.getTanggalCheckOut());
        txtJumlahTamu.setText(String.valueOf(b.getJumlahTamu()));

        if (b.getGrandHarga() != null) {
            txtGrandHarga.setText(RUPIAH_FORMAT.format(b.getGrandHarga()));
        } else {
            txtGrandHarga.clear();
        }

        cbStatus.setValue(b.getStatusBooking());

        cbPenyewa.setDisable(true);
        cbStatus.setDisable(false);

        btnSimpan.setDisable(true);
        btnUbah.setDisable(false);
        btnHapus.setDisable(false);
    }

    private void selectComboByName(ComboBox<String> combo, String nama) {
        if (nama == null) return;
        for (String item : combo.getItems()) {
            if (item != null && item.endsWith(" - " + nama)) {
                combo.setValue(item);
                return;
            }
        }
    }

    private String getIdFromCombo(String comboValue) {
        if (comboValue == null) return null;
        String[] parts = comboValue.split(" - ");
        return parts.length > 0 ? parts[0] : null;
    }

    private void setClose() {
        txtId.clear();
        txtJumlahTamu.clear();
        txtGrandHarga.clear();
        txtCatatan.clear();
        cbPenyewa.setValue(null);
        cbVilla.setValue(null);
        cbStatus.setValue(null);
        cbStatus.setDisable(true);
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
        cbPenyewa.setDisable(false);
        villaTerpilih = null;
        tableBooking.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnUbah.setDisable(true);
        btnHapus.setDisable(true);
    }

    private boolean validasi() {
        if (cbPenyewa.getValue() == null || cbVilla.getValue() == null ||
                dpCheckin.getValue() == null || dpCheckout.getValue() == null ||
                txtJumlahTamu.getText().trim().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Penyewa, Villa, Tanggal, dan Jumlah Tamu wajib diisi!");
            return false;
        }

        if (!dpCheckout.getValue().isAfter(dpCheckin.getValue())) {
            notif(NotifUtil.Type.WARNING, "Tanggal Check-Out harus setelah Check-In!");
            return false;
        }

        if (!txtJumlahTamu.getText().trim().matches("^[0-9]+$")) {
            notif(NotifUtil.Type.WARNING, "Jumlah Tamu harus berupa angka!");
            return false;
        }

        return true;
    }

    private void notif(NotifUtil.Type type, String msg) {
        NotifUtil.show(txtJumlahTamu, type, msg);
    }
}