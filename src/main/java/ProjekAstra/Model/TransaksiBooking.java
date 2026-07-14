package ProjekAstra.Model;

import java.time.LocalDate;

public class TransaksiBooking {
    private String idTrsBooking;
    private String namaPenyewa;
    private String namaVilla;
    private LocalDate tanggalCheckin;
    private LocalDate tanggalCheckOut;
    private int jumlahTamu;
    private double grandHarga;
    private String statusBooking;
    private LocalDate tanggalBooking;

    public TransaksiBooking(String idTrsBooking, String namaPenyewa, String namaVilla,
                            LocalDate tanggalCheckin, LocalDate tanggalCheckOut,
                            int jumlahTamu, double grandHarga,
                            String statusBooking, LocalDate tanggalBooking) {
        this.idTrsBooking = idTrsBooking;
        this.namaPenyewa = namaPenyewa;
        this.namaVilla = namaVilla;
        this.tanggalCheckin = tanggalCheckin;
        this.tanggalCheckOut = tanggalCheckOut;
        this.jumlahTamu = jumlahTamu;
        this.grandHarga = grandHarga;
        this.statusBooking = statusBooking;
        this.tanggalBooking = tanggalBooking;
    }

    public String getIdTrsBooking() { return idTrsBooking; }
    public String getNamaPenyewa() { return namaPenyewa; }
    public String getNamaVilla() { return namaVilla; }
    public LocalDate getTanggalCheckin() { return tanggalCheckin; }
    public LocalDate getTanggalCheckOut() { return tanggalCheckOut; }
    public int getJumlahTamu() { return jumlahTamu; }
    public double getGrandHarga() { return grandHarga; }
    public String getStatusBooking() { return statusBooking; }
    public LocalDate getTanggalBooking() { return tanggalBooking; }
}