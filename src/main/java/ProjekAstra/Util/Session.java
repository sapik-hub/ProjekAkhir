package ProjekAstra.Util;

public class Session {

    private static String idPemilik;
    private static String namaPemilik;

    private static String idKaryawan;
    private static String namaKaryawan;

    private static String idPenyewa;
    private static String namaPenyewa;

    private Session() {}

    // ===== PEMILIK =====
    public static void setPemilik(String id, String nama) {
        idPemilik = id;
        namaPemilik = nama;
    }

    public static String getIdPemilik() {
        return idPemilik;
    }

    public static String getNamaPemilik() {
        return namaPemilik;
    }

    // ===== KARYAWAN =====
    public static void setKaryawan(String id, String nama) {
        idKaryawan = id;
        namaKaryawan = nama;
    }

    public static String getIdKaryawan() {
        return idKaryawan;
    }

    public static String getNamaKaryawan() {
        return namaKaryawan;
    }

    // ===== PENYEWA =====
    public static void setPenyewa(String id, String nama) {
        idPenyewa = id;
        namaPenyewa = nama;
    }

    public static String getIdPenyewa() {
        return idPenyewa;
    }

    public static String getNamaPenyewa() {
        return namaPenyewa;
    }

    // ===== UTIL =====
    public static void clear() {
        idPemilik = null;
        namaPemilik = null;
        idKaryawan = null;
        namaKaryawan = null;
        idPenyewa = null;
        namaPenyewa = null;
    }
}