package ProjekAstra.Model;

public class DetailFasilitas {
    private String idVilla;
    private String idFasilitas;
    private String namaFasilitas;
    private int qty;
    private String deskripsi;

    // Constructor lengkap - dipakai buat data relasi Villa-Fasilitas (misal di tabel detail)
    public DetailFasilitas(String idVilla, String idFasilitas, String namaFasilitas, int qty, String deskripsi) {
        this.idVilla = idVilla;
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
        this.qty = qty;
        this.deskripsi = deskripsi;
    }

    // Constructor ringkas - dipakai buat isi ComboBox pilihan master fasilitas
    public DetailFasilitas(String idFasilitas, String namaFasilitas) {
        this(null, idFasilitas, namaFasilitas, 0, null);
    }

    public String getIdVilla() { return idVilla; }
    public void setIdVilla(String idVilla) { this.idVilla = idVilla; }

    public String getIdFasilitas() { return idFasilitas; }
    public void setIdFasilitas(String idFasilitas) { this.idFasilitas = idFasilitas; }

    public String getNamaFasilitas() { return namaFasilitas; }
    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() {
        return namaFasilitas;
    }
}