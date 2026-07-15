package ProjekAstra.Model;

import java.time.LocalDate;

public class TransaksiRefund {

    private String idRefund;
    private String namaKaryawan;
    private String idBooking;
    private String alasanRefund;
    private double jumlahRefund;
    private LocalDate tanggalPengajuan;
    private LocalDate tanggalRefund;
    private String deskripsi;
    private String status;

    public TransaksiRefund(String idRefund,
                           String namaKaryawan,
                           String idBooking,
                           String alasanRefund,
                           double jumlahRefund,
                           LocalDate tanggalPengajuan,
                           LocalDate tanggalRefund,
                           String deskripsi,
                           String status) {

        this.idRefund = idRefund;
        this.namaKaryawan = namaKaryawan;
        this.idBooking = idBooking;
        this.alasanRefund = alasanRefund;
        this.jumlahRefund = jumlahRefund;
        this.tanggalPengajuan = tanggalPengajuan;
        this.tanggalRefund = tanggalRefund;
        this.deskripsi = deskripsi;
        this.status = status;
    }

    public String getIdRefund() {
        return idRefund;
    }

    public void setIdRefund(String idRefund) {
        this.idRefund = idRefund;
    }

    public String getNamaKaryawan() {
        return namaKaryawan;
    }

    public void setNamaKaryawan(String namaKaryawan) {
        this.namaKaryawan = namaKaryawan;
    }

    public String getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    public String getAlasanRefund() {
        return alasanRefund;
    }

    public void setAlasanRefund(String alasanRefund) {
        this.alasanRefund = alasanRefund;
    }

    public double getJumlahRefund() {
        return jumlahRefund;
    }

    public void setJumlahRefund(double jumlahRefund) {
        this.jumlahRefund = jumlahRefund;
    }

    public LocalDate getTanggalPengajuan() {
        return tanggalPengajuan;
    }

    public void setTanggalPengajuan(LocalDate tanggalPengajuan) {
        this.tanggalPengajuan = tanggalPengajuan;
    }

    public LocalDate getTanggalRefund() {
        return tanggalRefund;
    }

    public void setTanggalRefund(LocalDate tanggalRefund) {
        this.tanggalRefund = tanggalRefund;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}