module com.example.sewavilla {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens ProjekAstra to javafx.fxml;
    opens ProjekAstra.Controller.MainView to javafx.fxml;
    opens ProjekAstra.Controller.Login to javafx.fxml;
    opens ProjekAstra.Controller.Dashboard to javafx.fxml;
    opens ProjekAstra.Controller.Transaksi to javafx.fxml;
    opens UITransaksi to javafx.fxml;
    opens ProjekAstra.Controller.Master to javafx.fxml;
    opens ProjekAstra.Model to javafx.base;

    exports ProjekAstra;
}