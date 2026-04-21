package com.bloodlife.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatisticiController {
    @FXML private PieChart grupePieChart;
    @FXML private BarChart<String, Number> donariBarChart;
    @FXML private BarChart<String, Number> centreBarChart;

    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String user = "postgres";
    private final String pass = "patratel98";

    @FXML
    public void initialize() {
        incarcaStatisticiGrupe();
        incarcaStatisticiDonariLuna();
        incarcaStatisticiCentre();
    }

    private void incarcaStatisticiGrupe() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String sql = "SELECT grupa_sanguina, COUNT(*) as total FROM stoc_detaliat GROUP BY grupa_sanguina";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("grupa_sanguina"), rs.getInt("total")));
            }
            grupePieChart.setData(pieData);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incarcaStatisticiDonariLuna() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Donări");

        String sql = "SELECT to_char(data_programare, 'Mon') as luna, COUNT(*) as total " +
                "FROM programari WHERE status = 'FINALIZATA' " +
                "GROUP BY luna ORDER BY MIN(data_programare)";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("luna"), rs.getInt("total")));
            }
            donariBarChart.getData().add(series);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incarcaStatisticiCentre() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Unități Colectate");

        String sql = "SELECT c.nume, COUNT(ps.id) as total " +
                "FROM centre_donare c " +
                "LEFT JOIN programari p ON c.id = p.id_centru " +
                "LEFT JOIN pungi_sange ps ON p.id = ps.id_programare " +
                "GROUP BY c.nume";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("nume"), rs.getInt("total")));
            }
            centreBarChart.getData().add(series);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleExportRaport() {
        try {
            // 1. Destinația fișierului
            String fileName = "Raport_Statistici_BloodLife_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // 2. Adăugare Titlu
            Font fontTitlu = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph titlu = new Paragraph("Raport Managerial BloodLife", fontTitlu);
            titlu.setAlignment(Element.ALIGN_CENTER);
            document.add(titlu);

            document.add(new Paragraph("Data generării: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            document.add(new Paragraph(" ")); // Spațiu gol

            // 3. Conversie PieChart în Imagine pentru PDF
            WritableImage imagePie = grupePieChart.snapshot(new SnapshotParameters(), null);
            File filePie = new File("temp_pie.png");
            ImageIO.write(SwingFXUtils.fromFXImage(imagePie, null), "png", filePie);

            Image pdfImgPie = Image.getInstance(filePie.getAbsolutePath());
            pdfImgPie.scaleToFit(500, 300);
            pdfImgPie.setAlignment(Element.ALIGN_CENTER);
            document.add(new Paragraph("1. Distribuția Grupelor de Sânge în Stoc"));
            document.add(pdfImgPie);

            // 4. Conversie BarChart în Imagine pentru PDF
            WritableImage imageBar = donariBarChart.snapshot(new SnapshotParameters(), null);
            File fileBar = new File("temp_bar.png");
            ImageIO.write(SwingFXUtils.fromFXImage(imageBar, null), "png", fileBar);

            Image pdfImgBar = Image.getInstance(fileBar.getAbsolutePath());
            pdfImgBar.scaleToFit(500, 300);
            pdfImgBar.setAlignment(Element.ALIGN_CENTER);
            document.add(new Paragraph("2. Evoluția Donărilor pe Luni"));
            document.add(pdfImgBar);

            document.close();

            // 5. Curățare fișiere temporare
            filePie.delete();
            fileBar.delete();

            // 6. Notificare utilizator
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Reușit");
            alert.setHeaderText(null);
            alert.setContentText("Raportul a fost salvat cu succes: " + fileName);
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare Export");
            alert.setContentText("Nu s-a putut genera PDF-ul: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}