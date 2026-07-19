package ProjekAstra.Model;

public class Fasilitas {
    private String idFasilitas;
    private String namaFasilitas;

    public Fasilitas() {}

    public Fasilitas(String idFasilitas, String namaFasilitas) {
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
    }

    public String getIdFasilitas() { return idFasilitas; }
    public void setIdFasilitas(String idFasilitas) { this.idFasilitas = idFasilitas; }

    public String getNamaFasilitas() { return namaFasilitas; }
    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

    @Override
    public String toString() { return namaFasilitas; } // biar tampil rapi di ComboBox
}