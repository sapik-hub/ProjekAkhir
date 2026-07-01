package ProjekAstra.Model;

import java.time.LocalDate;

public class Penyewa {
    private String idPenyewa, nama, noTelp, alamat, username;
    private int umur;
    private LocalDate tglLahir;

    public Penyewa(String idPenyewa, String nama, String noTelp, int umur,
                   LocalDate tglLahir, String alamat, String username) {
        this.idPenyewa = idPenyewa;
        this.nama = nama;
        this.noTelp = noTelp;
        this.umur = umur;
        this.tglLahir = tglLahir;
        this.alamat = alamat;
        this.username = username;
    }

    public String getIdPenyewa() { return idPenyewa; }
    public String getNama() { return nama; }
    public String getNoTelp() { return noTelp; }
    public int getUmur() { return umur; }
    public LocalDate getTglLahir() { return tglLahir; }
    public String getAlamat() { return alamat; }
    public String getUsername() { return username; }
}