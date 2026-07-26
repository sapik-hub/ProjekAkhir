package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.TransaksiBooking;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;
import ProjekAstra.Util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Types;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class Booking implements Initializable {

    @FXML private TextField txtId, txtJumlahTamu, txtGrandHarga, txtCari;
    @FXML private TextArea txtCatatan;
    @FXML private ComboBox<String> cbPenyewa, cbVilla, cbStatus;
    @FXML private DatePicker dpCheckin, dpCheckout;
    @FXML private Button btnSimpan, btnKonfirmasi, btnHapus;

    @FXML private TableView<TransaksiBooking> tableBooking;
    @FXML private TableColumn<TransaksiBooking, String> colId, colPenyewa, colVilla, colStatus;
    @FXML private TableColumn<TransaksiBooking, LocalDate> colCheckin, colCheckout;
    @FXML private TableColumn<TransaksiBooking, Integer> colTamu;
    @FXML private TableColumn<TransaksiBooking, Double> colHarga;

    private final ObservableList<TransaksiBooking> listBooking = FXCollections.observableArrayList();
    private double hargaVillaTerpilih = 0;

    // Urutan sesuai CHECK constraint di tabel TransaksiBooking - jangan diubah teksnya!
    private static final String[] DAFTAR_STATUS = {
            "Pending", "Dikonfirmasi", "Check In", "Check Out", "Selesai", "Dibatalkan"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadComboPenyewa();
        loadComboVilla();
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
            hargaVillaTerpilih = getHargaVillaTerpilih(newVal);
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

        // Badge warna biar admin gampang liat mana yang masih Pending
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
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // Pakai daftar SEMUA villa (bukan cuma yang Tersedia), supaya villa yang lagi
    // dibooking tetap muncul saat mode edit. Bentrok tanggal tetap dicegah di level SP.
    private void loadComboVilla() {
        cbVilla.getItems().clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllVilla}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                cbVilla.getItems().add(rs.getString("Id_Villa") + " - " + rs.getString("Nama_Villa"));
            }
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private double getHargaVillaTerpilih(String comboValue) {
        if (comboValue == null) return 0;
        String idVilla = comboValue.split(" - ")[0];
        Koneksi k = new Koneksi();
        double harga = 0;
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetHargaVilla(?)}");
            cs.setString(1, idVilla);
            ResultSet rs = cs.executeQuery();
            if (rs.next()) harga = rs.getDouble("Harga");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mengambil harga villa: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
        return harga;
    }

    private void hitungGrandHarga() {
        LocalDate checkin = dpCheckin.getValue();
        LocalDate checkout = dpCheckout.getValue();
        if (checkin != null && checkout != null && checkout.isAfter(checkin) && hargaVillaTerpilih > 0) {
            long malam = java.time.temporal.ChronoUnit.DAYS.between(checkin, checkout);
            txtGrandHarga.setText(String.valueOf(hargaVillaTerpilih * malam));
        } else {
            txtGrandHarga.clear();
        }
    }

    private void loadTable() {
        listBooking.clear();
        Koneksi k = new Koneksi();
        try {
            CallableStatement cs = k.conn.prepareCall("{call sp_GetAllBooking}");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                listBooking.add(new TransaksiBooking(
                        rs.getString("Id_TrxBooking"),
                        rs.getString("NamaPenyewa"),
                        rs.getString("Nama_Villa"),
                        rs.getDate("Tanggal_Checkin").toLocalDate(),
                        rs.getDate("Tanggal_Checkout").toLocalDate(),
                        rs.getInt("Jumlah_Tamu"),
                        rs.getDouble("Grand_Harga"),
                        rs.getString("Status_Booking"),
                        rs.getDate("Tanggal_Booking").toLocalDate()
                ));
            }
            tableBooking.setItems(listBooking);
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal memuat data booking: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    private void cariBooking(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableBooking.setItems(listBooking);
            return;
        }
        ObservableList<TransaksiBooking> hasil = FXCollections.observableArrayList();
        for (TransaksiBooking b : listBooking) {
            if (b.getNamaPenyewa().toLowerCase().contains(keyword.toLowerCase()) ||
                    b.getIdTrsBooking().toLowerCase().contains(keyword.toLowerCase())) {
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
            cs.execute();

            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.SUCCESS, "Booking berhasil dibuat!",
                    () -> { setClose(); loadTable(); });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

    // >>> Fungsi utama admin: konfirmasi/ubah status booking (Pending -> Dikonfirmasi -> ... dst)
    // Villa otomatis ke-update statusnya lewat trigger trg_Booking_UpdateVillaStatus di DB.
    @FXML
    private void handleKonfirmasi() {
        if (txtId.getText().isEmpty()) {
            notif(NotifUtil.Type.WARNING, "Pilih data booking yang ingin dikonfirmasi terlebih dahulu!");
            return;
        }
        if (!validasi()) return;
        if (cbStatus.getValue() == null) {
            notif(NotifUtil.Type.WARNING, "Pilih status booking terlebih dahulu!");
            return;
        }

        ConfirmUtil.show(txtJumlahTamu,
                "Ubah status booking " + txtId.getText() + " menjadi \"" + cbStatus.getValue() + "\"?",
                this::prosesKonfirmasi);
    }

    private void prosesKonfirmasi() {
        Koneksi k = new Koneksi();
        try {
            // 1. Update data booking (tanggal, villa, jumlah tamu, catatan)
            CallableStatement cs = k.conn.prepareCall("{call sp_UpdateBooking(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, txtId.getText());
            cs.setString(2, getIdFromCombo(cbVilla.getValue()));
            cs.setDate(3, java.sql.Date.valueOf(dpCheckin.getValue()));
            cs.setDate(4, java.sql.Date.valueOf(dpCheckout.getValue()));
            cs.setInt(5, Integer.parseInt(txtJumlahTamu.getText().trim()));
            cs.setString(6, txtCatatan.getText().trim());
            cs.execute();

            // 2. Update status - Id_Karyawan diambil dari sesi staff yang lagi login
            CallableStatement csStatus = k.conn.prepareCall("{call sp_UpdateStatusBooking(?, ?, ?)}");
            csStatus.setString(1, txtId.getText());
            String idKaryawan = Session.getIdKaryawan();
            if (idKaryawan == null) csStatus.setNull(2, Types.VARCHAR);
            else csStatus.setString(2, idKaryawan);
            csStatus.setString(3, cbStatus.getValue());
            csStatus.execute();

            NotifUtil.show(txtJumlahTamu, NotifUtil.Type.SUCCESS, "Booking berhasil dikonfirmasi!",
                    () -> { setClose(); loadTable(); });
        } catch (NumberFormatException e) {
            notif(NotifUtil.Type.WARNING, "Jumlah tamu harus berupa angka!");
        } catch (Exception e) {
            notif(NotifUtil.Type.ERROR, "Gagal mengonfirmasi: " + e.getMessage());
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }

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
                        CallableStatement cs = k.conn.prepareCall("{call sp_UpdateStatusBooking(?, ?, ?)}");
                        cs.setString(1, txtId.getText());
                        String idKaryawan = Session.getIdKaryawan();
                        if (idKaryawan == null) cs.setNull(2, Types.VARCHAR);
                        else cs.setString(2, idKaryawan);
                        cs.setString(3, "Dibatalkan");
                        cs.execute();

                        NotifUtil.show(txtJumlahTamu, NotifUtil.Type.SUCCESS, "Booking berhasil dibatalkan!",
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
        dpCheckin.setValue(b.getTanggalCheckin());
        dpCheckout.setValue(b.getTanggalCheckOut());
        txtJumlahTamu.setText(String.valueOf(b.getJumlahTamu()));
        txtGrandHarga.setText(String.valueOf(b.getGrandHarga()));
        cbStatus.setValue(b.getStatusBooking());

        cbPenyewa.setDisable(true); // penyewa gak boleh diganti pas konfirmasi/edit
        cbStatus.setDisable(false);

        btnSimpan.setDisable(true);
        btnKonfirmasi.setDisable(false);
        btnHapus.setDisable(false);
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
        txtJumlahTamu.clear();
        txtGrandHarga.clear();
        txtCatatan.clear();
        cbPenyewa.setValue(null);
        cbVilla.setValue(null);
        cbStatus.setValue(null);
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
        cbPenyewa.setDisable(false);
        cbStatus.setDisable(true); // status cuma relevan pas mode konfirmasi/edit
        hargaVillaTerpilih = 0;
        tableBooking.getSelectionModel().clearSelection();

        btnSimpan.setDisable(false);
        btnKonfirmasi.setDisable(true);
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