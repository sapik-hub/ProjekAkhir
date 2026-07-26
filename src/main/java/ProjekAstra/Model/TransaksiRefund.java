package ProjekAstra.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaksiRefund {

    private String idTrxRefund;
    private String idTrxBooking;
    private String namaPenyewa;
    private String namaVilla;
    private String namaKaryawan;
    private String alasanRefund;
    private BigDecimal jumlahRefund;
    private LocalDate tanggalPengajuan;
    private LocalDate tanggalRefund;
    private String deskripsi;
    private String status;

    public TransaksiRefund(String idTrxRefund, String idTrxBooking, String namaPenyewa, String namaVilla,
                           String namaKaryawan, String alasanRefund, BigDecimal jumlahRefund,
                           LocalDate tanggalPengajuan, LocalDate tanggalRefund,
                           String deskripsi, String status) {
        this.idTrxRefund = idTrxRefund;
        this.idTrxBooking = idTrxBooking;
        this.namaPenyewa = namaPenyewa;
        this.namaVilla = namaVilla;
        this.namaKaryawan = namaKaryawan;
        this.alasanRefund = alasanRefund;
        this.jumlahRefund = jumlahRefund;
        this.tanggalPengajuan = tanggalPengajuan;
        this.tanggalRefund = tanggalRefund;
        this.deskripsi = deskripsi;
        this.status = status;
    }

    public String getIdTrxRefund() { return idTrxRefund; }
    public String getIdTrxBooking() { return idTrxBooking; }
    public String getNamaPenyewa() { return namaPenyewa; }
    public String getNamaVilla() { return namaVilla; }
    public String getNamaKaryawan() { return namaKaryawan; }
    public String getAlasanRefund() { return alasanRefund; }
    public BigDecimal getJumlahRefund() { return jumlahRefund; }
    public LocalDate getTanggalPengajuan() { return tanggalPengajuan; }
    public LocalDate getTanggalRefund() { return tanggalRefund; }
    public String getDeskripsi() { return deskripsi; }
    public String getStatus() { return status; }
}