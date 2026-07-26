package ProjekAstra.Model;

import java.time.LocalDate;

public class Karyawan {
    private String idKaryawan;
    private String nama;
    private String noTelp;
    private String alamat;
    private int umur;
    private String username;
    private LocalDate tanggalLahir;  // <-- TAMBAH
    private LocalDate tanggalMasuk;
    private String status;
    private String role;

    // Constructor lengkap dengan Tanggal Lahir
    public Karyawan(String idKaryawan, String nama, String noTelp, String alamat, int umur,
                    String username, LocalDate tanggalLahir, LocalDate tanggalMasuk,
                    String status, String role) {
        this.idKaryawan = idKaryawan;
        this.nama = nama;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.umur = umur;
        this.username = username;
        this.tanggalLahir = tanggalLahir;
        this.tanggalMasuk = tanggalMasuk;
        this.status = status;
        this.role = role;
    }

    // Getters & Setters
    public String getIdKaryawan() { return idKaryawan; }
    public void setIdKaryawan(String idKaryawan) { this.idKaryawan = idKaryawan; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public int getUmur() { return umur; }
    public void setUmur(int umur) { this.umur = umur; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }

    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public void setTanggalMasuk(LocalDate tanggalMasuk) { this.tanggalMasuk = tanggalMasuk; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}