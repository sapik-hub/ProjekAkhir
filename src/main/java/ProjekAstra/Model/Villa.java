package ProjekAstra.Model;

import java.math.BigDecimal;

public class Villa {
    private String idVilla, namaPemilik, namaKategori, namaVilla, alamatVilla, status;
    private int kapasitas;
    private BigDecimal harga;

    public Villa(String idVilla, String namaPemilik, String namaKategori, String namaVilla,
                 int kapasitas, BigDecimal harga, String alamatVilla, String status) {
        this.idVilla = idVilla;
        this.namaPemilik = namaPemilik;
        this.namaKategori = namaKategori;
        this.namaVilla = namaVilla;
        this.kapasitas = kapasitas;
        this.harga = harga;
        this.alamatVilla = alamatVilla;
        this.status = status;
    }

    public String getIdVilla() { return idVilla; }
    public String getNamaPemilik() { return namaPemilik; }
    public String getNamaKategori() { return namaKategori; }
    public String getNamaVilla() { return namaVilla; }
    public int getKapasitas() { return kapasitas; }
    public BigDecimal getHarga() { return harga; }
    public String getAlamatVilla() { return alamatVilla; }
    public String getStatus() { return status; }
}
