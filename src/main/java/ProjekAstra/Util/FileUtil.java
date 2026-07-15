package ProjekAstra.Util;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtil {

    // Folder tetap di home directory user, jadi gak peduli app dijalanin dari mana
    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator +
                    "ProjekAstra" + File.separator +
                    "uploads" + File.separator +
                    "villa" + File.separator;

    // Munculin dialog pilih file dari penyimpanan laptop (bisa browse ke mana aja)
    public static File pilihGambar(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Villa");

        // Default folder pas dialog dibuka, biar user gak mulai dari C:\ terus
        File defaultDir = new File(System.getProperty("user.home") + File.separator + "Pictures");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg")
        );
        return fileChooser.showOpenDialog(ownerWindow);
    }

    // Copy file yang dipilih user ke folder penyimpanan aplikasi
    public static String simpanFoto(File sourceFile) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Folder upload dibuat: " + uploadPath.toAbsolutePath());
            }

            String extension = sourceFile.getName()
                    .substring(sourceFile.getName().lastIndexOf("."));
            String namaFileBaru = UUID.randomUUID().toString() + extension;

            Path target = uploadPath.resolve(namaFileBaru);
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Foto tersimpan di: " + target.toAbsolutePath());
            return namaFileBaru; // ini yang disimpen ke kolom Foto di database

        } catch (IOException e) {
            System.out.println("Gagal simpan foto: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Ambil full path absolut buat ditampilin di ImageView
    public static String getFullPath(String namaFile) {
        if (namaFile == null || namaFile.isEmpty()) return null;
        return Paths.get(UPLOAD_DIR, namaFile).toAbsolutePath().toString();
    }

    // Optional: hapus foto lama pas villa di-update dengan foto baru, atau villa dihapus
    public static void hapusFoto(String namaFile) {
        if (namaFile == null || namaFile.isEmpty()) return;
        try {
            Path target = Paths.get(UPLOAD_DIR, namaFile);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            System.out.println("Gagal hapus foto lama: " + e.getMessage());
        }
    }
}