package ProjekAstra.Model;

import java.math.BigDecimal;

public class Villa {
    private String idVilla;
    private String namaKategori;
    private String namaVilla;
    private int kapasitas;
    private BigDecimal hargaWeekday;
    private BigDecimal hargaWeekend;
    private String alamatVilla;
    private String foto;
    private String status;

    public Villa(String idVilla, String namaKategori, String namaVilla, int kapasitas,
                 BigDecimal hargaWeekday, BigDecimal hargaWeekend, String alamatVilla,
                 String foto, String status) {
        this.idVilla = idVilla;
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
    public void setIdVilla(String idVilla) { this.idVilla = idVilla; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getNamaVilla() { return namaVilla; }
    public void setNamaVilla(String namaVilla) { this.namaVilla = namaVilla; }

    public int getKapasitas() { return kapasitas; }
    public void setKapasitas(int kapasitas) { this.kapasitas = kapasitas; }

    public BigDecimal getHargaWeekday() { return hargaWeekday; }
    public void setHargaWeekday(BigDecimal hargaWeekday) { this.hargaWeekday = hargaWeekday; }

    public BigDecimal getHargaWeekend() { return hargaWeekend; }
    public void setHargaWeekend(BigDecimal hargaWeekend) { this.hargaWeekend = hargaWeekend; }

    public String getAlamatVilla() { return alamatVilla; }
    public void setAlamatVilla(String alamatVilla) { this.alamatVilla = alamatVilla; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}