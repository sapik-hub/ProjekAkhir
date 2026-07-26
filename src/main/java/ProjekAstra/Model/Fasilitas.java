package ProjekAstra.Model;

public class Fasilitas {
    private String idFasilitas;
    private String namaFasilitas;
    private int jumlah;
    private String deskripsi;
    private String status;

    public Fasilitas(String idFasilitas, String namaFasilitas, int jumlah, String deskripsi, String status) {
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.status = status;
    }

    public String getIdFasilitas() { return idFasilitas; }
    public String getNamaFasilitas() { return namaFasilitas; }
    public int getJumlah() { return jumlah; }
    public String getDeskripsi() { return deskripsi; }
    public String getStatus() { return status; }

    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
}