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
    public void setIdFasilitas(String idFasilitas) { this.idFasilitas = idFasilitas; }

    public String getNamaFasilitas() { return namaFasilitas; }
    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return namaFasilitas;
    }
}