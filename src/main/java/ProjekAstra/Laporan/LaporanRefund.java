package ProjekAstra.Laporan;

import ProjekAstra.Koneksi.Koneksi;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.SwingUtilities;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class LaporanRefund implements Initializable {

    @FXML private StackPane paneReport;
    @FXML private SwingNode swingNode;
    @FXML private Label lblStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tampilkanLaporan();
    }

    private void tampilkanLaporan() {
        Koneksi k = new Koneksi();
        try {
            InputStream jrxmlStream = getClass().getResourceAsStream("/ProjekAstra/Laporan/LaporanRefund.jrxml");
            if (jrxmlStream == null) {
                Platform.runLater(() -> lblStatus.setText("File LaporanRefund.jrxml tidak ditemukan di classpath!"));
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            Map<String, Object> parameters = new HashMap<>();

            Connection conn = k.conn;
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            // >>> TAMBAHIN DI SINI buat debug
            System.out.println("Jumlah halaman laporan: " + jasperPrint.getPages().size());

            SwingUtilities.invokeLater(() -> {
                JRViewer viewer = new JRViewer(jasperPrint);
                swingNode.setContent(viewer);

                Platform.runLater(() -> lblStatus.setText("Laporan Transaksi Refund"));
            });

        } catch (Exception e) {
            e.printStackTrace();
            String pesanError = e.getMessage();
            Platform.runLater(() -> lblStatus.setText("Gagal menampilkan laporan: " + pesanError));
        } finally {
            try { k.conn.close(); } catch (Exception ignored) {}
        }
    }
}