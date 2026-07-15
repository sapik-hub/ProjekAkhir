package ProjekAstra.Controller.Transaksi;

import ProjekAstra.Koneksi.Koneksi;
import ProjekAstra.Model.TransaksiRefund;
import ProjekAstra.Util.ConfirmUtil;
import ProjekAstra.Util.NotifUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;


public class Refund implements Initializable {


    @FXML private TextField txtId, txtJumlahRefund, txtCari;

    @FXML private TextArea txtAlasan, txtDeskripsi;

    @FXML private ComboBox<String> cbBooking, cbKaryawan, cbStatus;

    @FXML private DatePicker dpPengajuan, dpRefund;


    @FXML private Button btnSimpan, btnUbah, btnHapus;


    @FXML private TableView<TransaksiRefund> tableRefund;


    @FXML private TableColumn<TransaksiRefund, String> colId;
    @FXML private TableColumn<TransaksiRefund, String> colBooking;
    @FXML private TableColumn<TransaksiRefund, String> colKaryawan;
    @FXML private TableColumn<TransaksiRefund, Double> colJumlah;
    @FXML private TableColumn<TransaksiRefund, LocalDate> colPengajuan;
    @FXML private TableColumn<TransaksiRefund, LocalDate> colRefund;
    @FXML private TableColumn<TransaksiRefund, String> colStatus;



    private final ObservableList<TransaksiRefund> listRefund =
            FXCollections.observableArrayList();



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupTable();

        loadComboBooking();

        loadComboKaryawan();

        setupStatusCombo();

        loadTable();

        setClose();


        tableRefund.setOnMouseClicked(e -> {

            TransaksiRefund r =
                    tableRefund.getSelectionModel()
                            .getSelectedItem();

            if (r != null) {
                populateForm(r);
            }

        });


        txtCari.textProperty()
                .addListener((obs, oldVal, newVal) ->
                        cariRefund(newVal));


    }



    private void setupTable() {


        colId.setCellValueFactory(
                new PropertyValueFactory<>("idRefund")
        );


        colBooking.setCellValueFactory(
                new PropertyValueFactory<>("idBooking")
        );


        colKaryawan.setCellValueFactory(
                new PropertyValueFactory<>("namaKaryawan")
        );


        colJumlah.setCellValueFactory(
                new PropertyValueFactory<>("jumlahRefund")
        );


        colPengajuan.setCellValueFactory(
                new PropertyValueFactory<>("tanggalPengajuan")
        );


        colRefund.setCellValueFactory(
                new PropertyValueFactory<>("tanggalRefund")
        );


        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );


    }




    private void setupStatusCombo() {


        cbStatus.setItems(
                FXCollections.observableArrayList(

                        "Menunggu Persetujuan",
                        "Disetujui",
                        "Ditolak",
                        "Selesai"

                )
        );


    }




    private void loadComboBooking() {


        cbBooking.getItems().clear();


        Koneksi k = new Koneksi();


        try {


            CallableStatement cs =
                    k.conn.prepareCall(
                            "{call sp_GetBookingRefund}"
                    );


            ResultSet rs = cs.executeQuery();



            while (rs.next()) {


                cbBooking.getItems().add(

                        rs.getString("Id_trsBooking")
                                + " - "
                                + rs.getString("NamaPenyewa")

                );


            }



        } catch (Exception e) {


            notif(
                    NotifUtil.Type.ERROR,
                    "Gagal memuat data booking: "
                            + e.getMessage()
            );


        } finally {


            try {

                k.conn.close();

            } catch (Exception ignored) {}

        }


    }






    private void loadComboKaryawan() {


        cbKaryawan.getItems().clear();


        Koneksi k = new Koneksi();


        try {


            CallableStatement cs =
                    k.conn.prepareCall(
                            "{call sp_GetAllKaryawan}"
                    );


            ResultSet rs =
                    cs.executeQuery();



            while (rs.next()) {


                cbKaryawan.getItems().add(

                        rs.getString("Id_Karyawan")
                                + " - "
                                + rs.getString("Nama")

                );


            }



        } catch (Exception e) {


            notif(
                    NotifUtil.Type.ERROR,
                    "Gagal memuat data karyawan: "
                            + e.getMessage()
            );


        } finally {


            try {

                k.conn.close();

            } catch (Exception ignored) {}


        }


    }






    private void loadTable() {


        listRefund.clear();


        Koneksi k = new Koneksi();


        try {


            CallableStatement cs =
                    k.conn.prepareCall(
                            "{call sp_GetAllRefund}"
                    );



            ResultSet rs =
                    cs.executeQuery();




            while (rs.next()) {



                listRefund.add(

                        new TransaksiRefund(

                                rs.getString("Id_trsPengembalian_dana"),

                                rs.getString("NamaKaryawan"),

                                rs.getString("Id_trsBooking"),

                                rs.getString("Alasan_Refund"),

                                rs.getDouble("Jumlah_Refund"),

                                rs.getDate("Tanggal_Pengajuan")
                                        .toLocalDate(),

                                rs.getDate("Tanggal_Refund") == null
                                        ? null
                                        : rs.getDate("Tanggal_Refund")
                                        .toLocalDate(),

                                rs.getString("Deskripsi"),

                                rs.getString("Status")

                        )

                );


            }



            tableRefund.setItems(listRefund);



        } catch (Exception e) {



            notif(
                    NotifUtil.Type.ERROR,
                    "Gagal memuat data refund: "
                            + e.getMessage()
            );


        } finally {


            try {

                k.conn.close();

            } catch (Exception ignored) {}

        }


    }
    private void cariRefund(String keyword) {


        if (keyword == null || keyword.isEmpty()) {

            tableRefund.setItems(listRefund);

            return;
        }



        ObservableList<TransaksiRefund> hasil =
                FXCollections.observableArrayList();



        for (TransaksiRefund r : listRefund) {


            if (r.getIdRefund().toLowerCase()
                    .contains(keyword.toLowerCase())

                    ||

                    r.getIdBooking().toLowerCase()
                            .contains(keyword.toLowerCase())) {


                hasil.add(r);

            }


        }


        tableRefund.setItems(hasil);


    }






    @FXML
    private void handleSimpan() {


        if (!validasi()) return;



        Koneksi k = new Koneksi();


        try {


            CallableStatement cs =
                    k.conn.prepareCall(
                            "{call sp_InsertRefund(?, ?, ?, ?, ?, ?)}"
                    );



            cs.setString(
                    1,
                    getIdFromCombo(cbKaryawan.getValue())
            );


            cs.setString(
                    2,
                    getIdFromCombo(cbBooking.getValue())
            );


            cs.setString(
                    3,
                    txtAlasan.getText().trim()
            );


            cs.setDouble(
                    4,
                    Double.parseDouble(
                            txtJumlahRefund.getText().trim()
                    )
            );


            cs.setString(
                    5,
                    txtDeskripsi.getText().trim()
            );


            cs.setDate(
                    6,
                    java.sql.Date.valueOf(
                            dpPengajuan.getValue()
                    )
            );



            cs.execute();



            NotifUtil.show(
                    txtJumlahRefund,
                    NotifUtil.Type.SUCCESS,
                    "Refund berhasil diajukan!",
                    () -> {

                        setClose();

                        loadTable();

                    }
            );



        } catch (NumberFormatException e) {


            notif(
                    NotifUtil.Type.WARNING,
                    "Jumlah refund harus berupa angka!"
            );



        } catch (Exception e) {


            notif(
                    NotifUtil.Type.ERROR,
                    "Gagal menyimpan refund: "
                            + e.getMessage()
            );


        } finally {


            try {

                k.conn.close();

            } catch (Exception ignored) {}

        }


    }







    @FXML
    private void handleUbah() {


        if (txtId.getText().isEmpty()) {


            notif(
                    NotifUtil.Type.WARNING,
                    "Pilih data refund yang ingin diubah terlebih dahulu!"
            );


            return;

        }



        if (!validasi()) return;




        Koneksi k = new Koneksi();



        try {


            CallableStatement cs =
                    k.conn.prepareCall(
                            "{call sp_UpdateRefund(?, ?, ?, ?, ?, ?)}"
                    );



            cs.setString(
                    1,
                    txtId.getText()
            );


            cs.setString(
                    2,
                    txtAlasan.getText().trim()
            );


            cs.setDouble(
                    3,
                    Double.parseDouble(
                            txtJumlahRefund.getText().trim()
                    )
            );


            cs.setDate(
                    4,
                    java.sql.Date.valueOf(
                            dpRefund.getValue()
                    )
            );


            cs.setString(
                    5,
                    txtDeskripsi.getText().trim()
            );


            cs.setString(
                    6,
                    cbStatus.getValue()
            );



            cs.execute();




            NotifUtil.show(
                    txtJumlahRefund,
                    NotifUtil.Type.SUCCESS,
                    "Refund berhasil diubah!",
                    () -> {

                        setClose();

                        loadTable();

                    }
            );



        } catch (NumberFormatException e) {


            notif(
                    NotifUtil.Type.WARNING,
                    "Jumlah refund harus berupa angka!"
            );


        } catch (Exception e) {


            notif(
                    NotifUtil.Type.ERROR,
                    "Gagal mengubah refund: "
                            + e.getMessage()
            );



        } finally {


            try {

                k.conn.close();

            } catch (Exception ignored) {}


        }


    }







    @FXML
    private void handleHapus() {


        if (txtId.getText().isEmpty()) {


            notif(
                    NotifUtil.Type.WARNING,
                    "Pilih data refund yang ingin dibatalkan terlebih dahulu!"
            );


            return;

        }




        ConfirmUtil.show(

                txtJumlahRefund,

                "Yakin ingin membatalkan refund "
                        + txtId.getText()
                        + "?",


                () -> {


                    Koneksi k = new Koneksi();



                    try {



                        CallableStatement cs =
                                k.conn.prepareCall(
                                        "{call sp_UpdateStatusRefund(?, ?)}"
                                );



                        cs.setString(
                                1,
                                txtId.getText()
                        );



                        cs.setString(
                                2,
                                "Ditolak"
                        );



                        cs.execute();





                        NotifUtil.show(

                                txtJumlahRefund,

                                NotifUtil.Type.SUCCESS,

                                "Refund berhasil dibatalkan!",

                                () -> {

                                    setClose();

                                    loadTable();

                                }

                        );



                    } catch (Exception e) {


                        notif(
                                NotifUtil.Type.ERROR,
                                "Gagal membatalkan refund: "
                                        + e.getMessage()
                        );


                    } finally {


                        try {

                            k.conn.close();

                        } catch (Exception ignored) {}

                    }



                }

        );


    }








    @FXML
    private void handleReset() {


        setClose();


    }







    @FXML
    private void handleCetakStruk() {


        if (txtId.getText().isEmpty()) {


            notif(
                    NotifUtil.Type.WARNING,
                    "Pilih data refund yang ingin dicetak terlebih dahulu!"
            );


            return;

        }



        notif(
                NotifUtil.Type.SUCCESS,
                "Fitur cetak struk siap dihubungkan ke sp_CetakStrukRefund."
        );


    }







    private void populateForm(TransaksiRefund r) {



        txtId.setText(
                r.getIdRefund()
        );


        selectComboById(
                cbBooking,
                r.getIdBooking()
        );


        txtAlasan.setText(
                r.getAlasanRefund()
        );


        txtJumlahRefund.setText(
                String.valueOf(
                        r.getJumlahRefund()
                )
        );


        dpPengajuan.setValue(
                r.getTanggalPengajuan()
        );


        dpRefund.setValue(
                r.getTanggalRefund()
        );


        txtDeskripsi.setText(
                r.getDeskripsi()
        );


        cbStatus.setValue(
                r.getStatus()
        );



        btnSimpan.setDisable(true);

        btnUbah.setDisable(false);

        btnHapus.setDisable(false);



    }







    private void selectComboById(
            ComboBox<String> combo,
            String id
    ) {



        for (String item : combo.getItems()) {


            if (item.startsWith(id)) {


                combo.setValue(item);


                return;

            }


        }


    }







    private String getIdFromCombo(
            String comboValue
    ) {


        if (comboValue == null)

            return null;



        return comboValue.split(" - ")[0];


    }








    private void setClose() {


        txtId.clear();

        txtJumlahRefund.clear();

        txtAlasan.clear();

        txtDeskripsi.clear();



        cbBooking.setValue(null);

        cbKaryawan.setValue(null);

        cbStatus.setValue(null);



        dpPengajuan.setValue(null);

        dpRefund.setValue(null);



        tableRefund.getSelectionModel()
                .clearSelection();




        btnSimpan.setDisable(false);

        btnUbah.setDisable(true);

        btnHapus.setDisable(true);


    }








    private boolean validasi() {



        if (

                cbBooking.getValue() == null ||

                        cbKaryawan.getValue() == null ||

                        txtAlasan.getText().trim().isEmpty() ||

                        txtJumlahRefund.getText().trim().isEmpty() ||

                        dpPengajuan.getValue() == null

        ) {



            notif(

                    NotifUtil.Type.WARNING,

                    "Booking, Karyawan, Alasan, Jumlah Refund, dan Tanggal wajib diisi!"

            );


            return false;


        }






        if (!txtJumlahRefund.getText()
                .trim()
                .matches("^[0-9]+(\\.[0-9]+)?$")) {



            notif(

                    NotifUtil.Type.WARNING,

                    "Jumlah refund harus berupa angka!"

            );


            return false;


        }



        return true;


    }







    private void notif(
            NotifUtil.Type type,
            String msg
    ) {


        NotifUtil.show(
                txtJumlahRefund,
                type,
                msg
        );


    }


}