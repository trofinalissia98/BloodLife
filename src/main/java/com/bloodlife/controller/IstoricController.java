package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Utilizator;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;

public class IstoricController {
    @FXML private TableView<DonareInfo> tabelIstoric;
    @FXML private TableColumn<DonareInfo, String> colData, colTip, colCantitate, colCentru, colActiune;

    private Utilizator utilizatorLogat;
    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";

    public void setInitialData(Utilizator user) {
        this.utilizatorLogat = user;
        configurareTabel();
        incarcaDatele();
    }

    private void configurareTabel() {
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colCantitate.setCellValueFactory(new PropertyValueFactory<>("cantitate"));
        colCentru.setCellValueFactory(new PropertyValueFactory<>("centru"));

        // Buton de download în celulă
        colActiune.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("📥 Descarcă PDF");
            {
                btn.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    DonareInfo data = getTableView().getItems().get(getIndex());
                    handleDownloadPDF(data);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }

    private void incarcaDatele() {
        ObservableList<DonareInfo> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT p.data_programare, p.status, ps.tip_recoltare, ps.cantitate_ml, c.nume as centru
            FROM programari p
            LEFT JOIN pungi_sange ps ON p.id = ps.id_programare
            JOIN centre_donare c ON p.id_centru = c.id
            WHERE p.id_donator = ? AND p.status = 'FINALIZATA'
            ORDER BY p.data_programare DESC
            """;

        try (Connection con = DriverManager.getConnection(url, "postgres", "patratel98");
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, utilizatorLogat.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new DonareInfo(
                        rs.getDate("data_programare").toString(),
                        rs.getString("tip_recoltare") != null ? rs.getString("tip_recoltare") : "Sânge Total",
                        rs.getInt("cantitate_ml") + " ml",
                        rs.getString("centru")
                ));
            }
            tabelIstoric.setItems(lista);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void handleDownloadPDF(DonareInfo info) {
        Document document = new Document();
        String numeFisier = "Analize_BloodLife_" + info.getData().replace("-", "") + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(numeFisier));
            document.open();

            // 1. Antet Medical
            Font fontTitlu = new Font(Font.HELVETICA, 18, Font.BOLD, java.awt.Color.RED);
            Paragraph titlu = new Paragraph("BLOODLIFE - BULETIN DE ANALIZE", fontTitlu);
            titlu.setAlignment(Element.ALIGN_CENTER);
            document.add(titlu);
            document.add(new Paragraph(" "));

            // 2. Informații Donator și Recoltare
            document.add(new Paragraph("Donator: " + utilizatorLogat.getNume()));
            document.add(new Paragraph("Data Recoltării: " + info.getData()));
            document.add(new Paragraph("Centru: " + info.getCentru()));
            document.add(new Paragraph("Tip Probă: " + info.getTip()));
            document.add(new Paragraph("-------------------------------------------------------------------------------------------------------------------------"));
            document.add(new Paragraph(" "));

            // 3. Tabel Rezultate Analize
            PdfPTable table = new PdfPTable(3); // 3 coloane
            table.setWidthPercentage(100);

            // Header Tabel
            addTableHeader(table, "Analiză");
            addTableHeader(table, "Rezultat");
            addTableHeader(table, "Valori Referință");

            // Date Fictive (Standard Medical)
            addRows(table, "Hemoglobină", "14.5 g/dL", "13.0 - 17.0");
            addRows(table, "Glicemie", "92 mg/dL", "70 - 105");
            addRows(table, "VDRL / HIV", "NEGATIV", "NEGATIV");
            addRows(table, "Ag HBs (Hepatita B)", "NEGATIV", "NEGATIV");
            addRows(table, "Anti-HCV (Hepatita C)", "NEGATIV", "NEGATIV");
            addRows(table, "ALT (Transaminază)", "22 U/L", "< 41");

            document.add(table);

            // 4. Semnătura și Parafa
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph parafa = new Paragraph("Document generat automat de sistemul BloodLife.\nNu necesită semnătură olografă.");
            parafa.setAlignment(Element.ALIGN_RIGHT);
            parafa.setFont(new Font(Font.HELVETICA, 8, Font.ITALIC));
            document.add(parafa);

            document.close();

            // 5. Deschide PDF-ul automat după generare
            File file = new File(numeFisier);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            afiseazaAlerta("Eroare Export", "Nu s-a putut genera PDF-ul: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Metode ajutătoare pentru Tabelul PDF
    private void addTableHeader(PdfPTable table, String columnTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(columnTitle, new Font(Font.HELVETICA, 12, Font.BOLD)));
        header.setPadding(5);
        table.addCell(header);
    }

    private void addRows(PdfPTable table, String analiza, String rezultat, String referinta) {
        table.addCell(analiza);
        table.addCell(rezultat);
        table.addCell(referinta);
    }

    private void afiseazaAlerta(String titlu, String mesaj, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(titlu);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            HelloApplication.showDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Clasă Helper pentru rândurile tabelului
    public static class DonareInfo {
        private final String data, tip, cantitate, centru;
        public DonareInfo(String d, String t, String c, String l) {
            this.data = d; this.tip = t; this.cantitate = c; this.centru = l;
        }
        public String getData() { return data; }
        public String getTip() { return tip; }
        public String getCantitate() { return cantitate; }
        public String getCentru() { return centru; }
    }
}