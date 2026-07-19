package ProjekAstra.Model;

public class VillaFasilitas {
    private String idVillaFasilitas;
    private String idVilla;
    private String idFasilitas;
    private String namaFasilitas;
    private int jumlah;
    private String deskripsi;

    public VillaFasilitas() {}

    public VillaFasilitas(String idVillaFasilitas, String idVilla, String idFasilitas,
                          String namaFasilitas, int jumlah, String deskripsi) {
        this.idVillaFasilitas = idVillaFasilitas;
        this.idVilla = idVilla;
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
    }

    public String getIdVillaFasilitas() { return idVillaFasilitas; }
    public void setIdVillaFasilitas(String idVillaFasilitas) { this.idVillaFasilitas = idVillaFasilitas; }

    public String getIdVilla() { return idVilla; }
    public void setIdVilla(String idVilla) { this.idVilla = idVilla; }

    public String getIdFasilitas() { return idFasilitas; }
    public void setIdFasilitas(String idFasilitas) { this.idFasilitas = idFasilitas; }

    public String getNamaFasilitas() { return namaFasilitas; }
    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
}