package ProjekAstra.Model;

import java.time.LocalDate;

public class Penyewa {
    private String idPenyewa, nama, noTelp, nikKtp, alamat, username;
    private LocalDate tglLahir;

    public Penyewa(String idPenyewa, String nama, String noTelp, String nikKtp,
                   LocalDate tglLahir, String alamat, String username) {
        this.idPenyewa = idPenyewa;
        this.nama = nama;
        this.noTelp = noTelp;
        this.nikKtp = nikKtp;
        this.tglLahir = tglLahir;
        this.alamat = alamat;
        this.username = username;
    }

    public String getIdPenyewa() { return idPenyewa; }
    public String getNama() { return nama; }
    public String getNoTelp() { return noTelp; }
    public String getNikKtp() { return nikKtp; }
    public LocalDate getTglLahir() { return tglLahir; }
    public String getAlamat() { return alamat; }
    public String getUsername() { return username; }
}