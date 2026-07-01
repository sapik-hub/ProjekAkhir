package ProjekAstra.Koneksi;

import java.sql.*;

public class Koneksi {
    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;


    public Koneksi() {

        try {
            String url =
                    "jdbc:sqlserver://LAPTOP-CH8RQ547\\SQLEXPRESS;" +
                            "databaseName=VillaNesia;" +
                            "user=sa;" +
                            "password=popo;" +
                            "encrypt=false;" +
                            "trustServerCertificate=true;";

            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();

        } catch (Exception e) {
            System.out.println("Koneksi gagal!"+ e);
        }

    }

    public static void main(String[] args) {
        Koneksi k = new Koneksi();
        System.out.println("Koneksi berhasil!");
    }
}
