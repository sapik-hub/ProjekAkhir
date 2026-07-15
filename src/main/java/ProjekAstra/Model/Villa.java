package ProjekAstra.Model;

import java.math.BigDecimal;

public class Villa {
    private String idVilla, namaPemilik, namaKategori, namaVilla, alamatVilla, foto, status;
    private int kapasitas;
    private BigDecimal hargaWeekday;
    private BigDecimal hargaWeekend;

    public Villa(String idVilla, String namaPemilik, String namaKategori, String namaVilla,
                 int kapasitas, BigDecimal hargaWeekday, BigDecimal hargaWeekend,
                 String alamatVilla, String foto, String status) {
        this.idVilla = idVilla;
        this.namaPemilik = namaPemilik;
        this.namaKategori = namaKategori;
        this.namaVilla = namaVilla;
        this.kapasitas = kapasitas;
        this.hargaWeekday = hargaWeekday;
        this.hargaWeekend = hargaWeekend;
        this.alamatVilla = alamatVilla;
        this.foto = foto;
        this.status = status;
    }

    public String getIdVilla() { return idVilla; }
    public String getNamaPemilik() { return namaPemilik; }
    public String getNamaKategori() { return namaKategori; }
    public String getNamaVilla() { return namaVilla; }
    public int getKapasitas() { return kapasitas; }
    public BigDecimal getHargaWeekday() { return hargaWeekday; }
    public BigDecimal getHargaWeekend() { return hargaWeekend; }
    public String getAlamatVilla() { return alamatVilla; }
    public String getFoto() { return foto; }
    public String getStatus() { return status; }
}