package ProjekAstra.Koneksi;

import java.sql.*;

public class Koneksi {

    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;

    public Koneksi (){
        try{
            String url = "jdbc:sqlserver://LAPTOP-B1008GMB:1433;"+
                    "databaseName=VillaStay;"+
                    "user=sa;"+
                    "password=Bharataganteng123;"+
                    "trustServerCertificate=true;";

            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
        }catch (Exception e){
            System.out.println("Error saat connect databse "+ e);
        }
    }

    public static void main(String[] args){
        Koneksi connection = new Koneksi();
        System.out.println("Connecting to database successfully");
    }
}

