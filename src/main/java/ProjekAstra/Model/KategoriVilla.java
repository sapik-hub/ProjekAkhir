package ProjekAstra.Model;

public class KategoriVilla {
    private String idKategori, namaKategori, deskripsi;

    public KategoriVilla(String idKategori, String namaKategori, String deskripsi) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }

    public String getIdKategori() { return idKategori; }
    public String getNamaKategori() { return namaKategori; }
    public String getDeskripsi() { return deskripsi; }
}