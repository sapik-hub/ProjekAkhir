package ProjekAstra.Model;

public class Pemilik {
    private String idPemilik, nama, noTelp, email, alamat, username;

    public Pemilik(String idPemilik, String nama, String noTelp, String email, String alamat, String username) {
        this.idPemilik = idPemilik;
        this.nama = nama;
        this.noTelp = noTelp;
        this.email = email;
        this.alamat = alamat;
        this.username = username;
    }

    public String getIdPemilik() { return idPemilik; }
    public String getNama() { return nama; }
    public String getNoTelp() { return noTelp; }
    public String getEmail() { return email; }
    public String getAlamat() { return alamat; }
    public String getUsername() { return username; }
}
