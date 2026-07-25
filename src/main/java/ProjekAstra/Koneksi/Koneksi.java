package ProjekAstra.Koneksi;

import java.sql.*;

public class Koneksi {
    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;


    public Koneksi() {

        try {
            String url = "jdbc:sqlserver://MAULINA\\SQLEXPRESS:64358;databaseName=VillaStay;User=sa;password=mochimochi;trustServerCertificate=true";
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
            System.out.println("Connected to database su-ccessfully");
        } catch (Exception e) {
            System.out.println("Error ketika connect ke Database " + e);
        }
    }

    public static void main(String[] args) {
        Koneksi k = new Koneksi();
        System.out.println("Koneksi berhasil!");
    }
}
