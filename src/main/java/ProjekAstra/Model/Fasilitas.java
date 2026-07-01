package ProjekAstra.Model;

public class Fasilitas {
    private String idFasilitas, namaVilla, namaFasilitas, deskripsi;
    private int jumlah;

    public Fasilitas(String idFasilitas, String namaVilla, String namaFasilitas, int jumlah, String deskripsi) {
        this.idFasilitas = idFasilitas;
        this.namaVilla = namaVilla;
        this.namaFasilitas = namaFasilitas;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
    }

    public String getIdFasilitas() { return idFasilitas; }
    public String getNamaVilla() { return namaVilla; }
    public String getNamaFasilitas() { return namaFasilitas; }
    public int getJumlah() { return jumlah; }
    public String getDeskripsi() { return deskripsi; }
}