package ProjekAstra.Model;

import java.math.BigDecimal;

public class VillaInfo {
    private String idVilla;
    private String namaVilla;
    private BigDecimal hargaWeekday;
    private BigDecimal hargaWeekend;

    public VillaInfo(String idVilla, String namaVilla, BigDecimal hargaWeekday, BigDecimal hargaWeekend) {
        this.idVilla = idVilla != null ? idVilla : "";
        this.namaVilla = namaVilla != null ? namaVilla : "Tidak Diketahui";
        this.hargaWeekday = hargaWeekday != null ? hargaWeekday : BigDecimal.ZERO;
        this.hargaWeekend = hargaWeekend != null ? hargaWeekend : BigDecimal.ZERO;
    }

    public String getIdVilla() { return idVilla; }
    public String getNamaVilla() { return namaVilla; }
    public BigDecimal getHargaWeekday() { return hargaWeekday; }
    public BigDecimal getHargaWeekend() { return hargaWeekend; }

    public void setIdVilla(String idVilla) { this.idVilla = idVilla; }
    public void setNamaVilla(String namaVilla) { this.namaVilla = namaVilla; }
    public void setHargaWeekday(BigDecimal hargaWeekday) { this.hargaWeekday = hargaWeekday; }
    public void setHargaWeekend(BigDecimal hargaWeekend) { this.hargaWeekend = hargaWeekend; }
}