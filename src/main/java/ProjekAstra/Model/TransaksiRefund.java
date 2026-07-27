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

    // Constructor dengan semua parameter (untuk load table)
    public TransaksiRefund(String idTrxRefund, String idTrxBooking, String namaPenyewa,
                           String namaVilla, String namaKaryawan, String alasanRefund,
                           BigDecimal jumlahRefund, LocalDate tanggalPengajuan,
                           LocalDate tanggalRefund, String deskripsi, String status) {
        this.idTrxRefund = idTrxRefund != null ? idTrxRefund : "";
        this.idTrxBooking = idTrxBooking != null ? idTrxBooking : "";
        this.namaPenyewa = namaPenyewa != null ? namaPenyewa : "Tidak Diketahui";
        this.namaVilla = namaVilla != null ? namaVilla : "Tidak Diketahui";
        this.namaKaryawan = namaKaryawan != null ? namaKaryawan : "Tidak Diketahui";
        this.alasanRefund = alasanRefund != null ? alasanRefund : "-";
        this.jumlahRefund = jumlahRefund != null ? jumlahRefund : BigDecimal.ZERO;
        this.tanggalPengajuan = tanggalPengajuan;
        this.tanggalRefund = tanggalRefund;
        this.deskripsi = deskripsi != null ? deskripsi : "";
        this.status = status != null ? status : "Pending";
    }

    // GETTERS
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

    // SETTERS (jika diperlukan)
    public void setIdTrxRefund(String idTrxRefund) { this.idTrxRefund = idTrxRefund; }
    public void setIdTrxBooking(String idTrxBooking) { this.idTrxBooking = idTrxBooking; }
    public void setNamaPenyewa(String namaPenyewa) { this.namaPenyewa = namaPenyewa; }
    public void setNamaVilla(String namaVilla) { this.namaVilla = namaVilla; }
    public void setNamaKaryawan(String namaKaryawan) { this.namaKaryawan = namaKaryawan; }
    public void setAlasanRefund(String alasanRefund) { this.alasanRefund = alasanRefund; }
    public void setJumlahRefund(BigDecimal jumlahRefund) { this.jumlahRefund = jumlahRefund; }
    public void setTanggalPengajuan(LocalDate tanggalPengajuan) { this.tanggalPengajuan = tanggalPengajuan; }
    public void setTanggalRefund(LocalDate tanggalRefund) { this.tanggalRefund = tanggalRefund; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setStatus(String status) { this.status = status; }
}