package ProjekAstra.Model;


import java.time.LocalDate;

public class Karyawan {
    private String idKaryawan, nama, noTelp, alamat, username, status;
    private int umur;
    private LocalDate tanggalMasuk;

    public Karyawan(String idKaryawan, String nama, String noTelp, String alamat,
                    int umur, String username, LocalDate tanggalMasuk, String status) {
        this.idKaryawan = idKaryawan;
        this.nama = nama;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.umur = umur;
        this.username = username;
        this.tanggalMasuk = tanggalMasuk;
        this.status = status;
    }

    public String getIdKaryawan() { return idKaryawan; }
    public String getNama() { return nama; }
    public String getNoTelp() { return noTelp; }
    public String getAlamat() { return alamat; }
    public int getUmur() { return umur; }
    public String getUsername() { return username; }
    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public String getStatus() { return status; }
}
