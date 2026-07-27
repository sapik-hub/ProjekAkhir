package ProjekAstra.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaksiBooking {
    private String idTrsBooking;
    private String namaPenyewa;
    private String namaVilla;
    private LocalDate tanggalCheckin;
    private LocalDate tanggalCheckOut;
    private int jumlahTamu;
    private BigDecimal grandHarga;  // ✅ GANTI dari double ke BigDecimal
    private String statusBooking;
    private LocalDate tanggalBooking;

    // ✅ CONSTRUCTOR 1: Untuk load table (dengan BigDecimal)
    public TransaksiBooking(String idTrsBooking, String namaPenyewa, String namaVilla,
                            LocalDate tanggalCheckin, LocalDate tanggalCheckOut,
                            int jumlahTamu, BigDecimal grandHarga, String statusBooking,
                            LocalDate tanggalBooking) {
        this.idTrsBooking = idTrsBooking != null ? idTrsBooking : "";
        this.namaPenyewa = namaPenyewa != null ? namaPenyewa : "Tidak Diketahui";
        this.namaVilla = namaVilla != null ? namaVilla : "Tidak Diketahui";
        this.tanggalCheckin = tanggalCheckin;
        this.tanggalCheckOut = tanggalCheckOut;
        this.jumlahTamu = jumlahTamu;
        this.grandHarga = grandHarga != null ? grandHarga : BigDecimal.ZERO;
        this.statusBooking = statusBooking != null ? statusBooking : "Pending";
        this.tanggalBooking = tanggalBooking;
    }

    // ✅ CONSTRUCTOR 2: Untuk keperluan lain (jika masih ada yang pakai double)
    @Deprecated
    public TransaksiBooking(String idTrsBooking, String namaPenyewa, String namaVilla,
                            LocalDate tanggalCheckin, LocalDate tanggalCheckOut,
                            int jumlahTamu, double grandHarga, String statusBooking,
                            LocalDate tanggalBooking) {
        this(idTrsBooking, namaPenyewa, namaVilla, tanggalCheckin, tanggalCheckOut,
                jumlahTamu, BigDecimal.valueOf(grandHarga), statusBooking, tanggalBooking);
    }

    // GETTERS
    public String getIdTrsBooking() { return idTrsBooking; }
    public String getNamaPenyewa() { return namaPenyewa; }
    public String getNamaVilla() { return namaVilla; }
    public LocalDate getTanggalCheckin() { return tanggalCheckin; }
    public LocalDate getTanggalCheckOut() { return tanggalCheckOut; }
    public int getJumlahTamu() { return jumlahTamu; }
    public BigDecimal getGrandHarga() { return grandHarga; }
    public String getStatusBooking() { return statusBooking; }
    public LocalDate getTanggalBooking() { return tanggalBooking; }

    // SETTERS
    public void setIdTrsBooking(String idTrsBooking) { this.idTrsBooking = idTrsBooking; }
    public void setNamaPenyewa(String namaPenyewa) { this.namaPenyewa = namaPenyewa; }
    public void setNamaVilla(String namaVilla) { this.namaVilla = namaVilla; }
    public void setTanggalCheckin(LocalDate tanggalCheckin) { this.tanggalCheckin = tanggalCheckin; }
    public void setTanggalCheckOut(LocalDate tanggalCheckOut) { this.tanggalCheckOut = tanggalCheckOut; }
    public void setJumlahTamu(int jumlahTamu) { this.jumlahTamu = jumlahTamu; }
    public void setGrandHarga(BigDecimal grandHarga) { this.grandHarga = grandHarga; }
    public void setStatusBooking(String statusBooking) { this.statusBooking = statusBooking; }
    public void setTanggalBooking(LocalDate tanggalBooking) { this.tanggalBooking = tanggalBooking; }
}