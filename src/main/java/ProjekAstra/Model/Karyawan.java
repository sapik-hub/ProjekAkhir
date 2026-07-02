package ProjekAstra.Model;

import java.time.LocalDate;

public class Karyawan {
    private String idKaryawan;
    private String nama;
    private String noTelp;
    private String alamat;
    private int umur;
    private String username;
    private LocalDate tanggalMasuk;
    private String status;
    private String role;

    public Karyawan(String idKaryawan, String nama, String noTelp, String alamat,
                    int umur, String username, LocalDate tanggalMasuk,
                    String status, String role) {
        this.idKaryawan = idKaryawan;
        this.nama = nama;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.umur = umur;
        this.username = username;
        this.tanggalMasuk = tanggalMasuk;
        this.status = status;
        this.role = role;
    }

    public String getIdKaryawan() { return idKaryawan; }
    public String getNama() { return nama; }
    public String getNoTelp() { return noTelp; }
    public String getAlamat() { return alamat; }
    public int getUmur() { return umur; }
    public String getUsername() { return username; }
    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public String getStatus() { return status; }
    public String getRole() { return role; }
}