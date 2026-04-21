package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.PungaSange;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class StocController {
    @FXML private TableView<PungaSange> stocTable;
    @FXML private TableColumn<PungaSange, String> colCod, colTip, colGrupa, colRh, colStatus, colData;
    @FXML private TableColumn<PungaSange, Integer> colCantitate;
    @FXML private ComboBox<String> filterTipCombo, filterGrupaCombo;

    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String user = "postgres";
    private final String pass = "patratel98";

    @FXML
    public void initialize() {
        colCod.setCellValueFactory(new PropertyValueFactory<>("cod"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colGrupa.setCellValueFactory(new PropertyValueFactory<>("grupa"));
        colRh.setCellValueFactory(new PropertyValueFactory<>("rh"));
        colCantitate.setCellValueFactory(new PropertyValueFactory<>("cantitate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));

        filterTipCombo.setItems(FXCollections.observableArrayList("Sânge Total", "Plasmă", "Trombocite"));
        filterGrupaCombo.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));
        colCod.setCellValueFactory(new PropertyValueFactory<>("cod"));
        // ... restul coloanelor ...

        // 2. Populăm ComboBox-urile pentru FILTRARE (cele de sus)
        filterTipCombo.setItems(FXCollections.observableArrayList("Sânge Total", "Plasmă", "Trombocite"));
        filterGrupaCombo.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));

        // 3. !!! ASTA LIPSEA: Populăm ComboBox-urile pentru EMITERE ALERTĂ (cele de jos) !!!
        alertGrupaCombo.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));
        alertRhCombo.setItems(FXCollections.observableArrayList("+", "-"));

        // 4. Încărcăm datele inițiale în tabel

        incarcaDatele();
    }

    // --- LOGICA DE UPDATE STATUS ---
    private void updateStatusPunga(String noulStatus) {
        PungaSange selectata = stocTable.getSelectionModel().getSelectedItem();

        if (selectata == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Te rugăm să selectezi o pungă din tabel!");
            alert.show();
            return;
        }

        String sql = "UPDATE pungi_sange SET status_punga = ? WHERE cod_unitate = ?";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, noulStatus);
            ps.setString(2, selectata.getCod());
            ps.executeUpdate();

            // Reîncărcăm datele ca să vedem schimbarea
            handleFilter();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMarkExpirat() { updateStatusPunga("EXPIRAT"); }
    @FXML private void handleMarkRezervat() { updateStatusPunga("RESERVAT"); }
    @FXML private void handleMarkUtilizat() { updateStatusPunga("UTILIZAT"); }

    @FXML
    public void incarcaDatele() {
        filterTipCombo.setValue(null);
        filterGrupaCombo.setValue(null);
        handleFilter();
    }

    @FXML
    private void handleFilter() {
        String tipSelectat = filterTipCombo.getValue();
        String grupaSelectata = filterGrupaCombo.getValue();
        ObservableList<PungaSange> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("SELECT * FROM stoc_detaliat WHERE 1=1");
        if (tipSelectat != null) sql.append(" AND tip_recoltare = '").append(tipSelectat).append("'");
        if (grupaSelectata != null) sql.append(" AND grupa_sanguina = '").append(grupaSelectata).append("'");
        sql.append(" ORDER BY data_recoltare DESC");

        try (Connection con = DriverManager.getConnection(url, user, pass);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {

            while (rs.next()) {
                lista.add(new PungaSange(
                        rs.getString("cod_unitate"), rs.getString("tip_recoltare"),
                        rs.getString("grupa_sanguina"), rs.getString("rh"),
                        rs.getInt("cantitate_ml"), rs.getString("status_punga"),
                        rs.getTimestamp("data_recoltare").toString()
                ));
            }
            stocTable.setItems(lista);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleInapoi() {
        try { HelloApplication.showMedicPanel(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private ComboBox<String> alertGrupaCombo, alertRhCombo;
    @FXML private TextField alertMesajField;

// Adaugă în initialize():
// alertRhCombo.setItems(FXCollections.observableArrayList("+", "-"));

    @FXML
    private void handleTrimiteAlerta() {
        String grupa = alertGrupaCombo.getValue();
        String rh = alertRhCombo.getValue();
        String mesaj = alertMesajField.getText();

        if (grupa == null || rh == null || mesaj.isEmpty()) {
            afiseazaAlerta("Câmpuri incomplete", "Selectați grupa, RH-ul și scrieți un mesaj!", Alert.AlertType.ERROR);
            return;
        }

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // 1. Salvăm alerta în tabelul alerte_stoc
            String sqlInsert = "INSERT INTO alerte_stoc (grupa_target, rh_target, mesaj) VALUES (?, ?, ?)";
            PreparedStatement psInsert = con.prepareStatement(sqlInsert);
            psInsert.setString(1, grupa);
            psInsert.setString(2, rh);
            psInsert.setString(3, mesaj);
            psInsert.executeUpdate();

            // 2. Numărăm donatorii care vor primi notificarea (pentru raportul de trimitere)
            String sqlCount = "SELECT COUNT(*) FROM donatori WHERE grupa_sanguina = ? AND rh = ?";
            PreparedStatement psCount = con.prepareStatement(sqlCount);
            psCount.setString(1, grupa);
            psCount.setString(2, rh);
            ResultSet rs = psCount.executeQuery();

            int nrDonatori = 0;
            if (rs.next()) nrDonatori = rs.getInt(1);

            // 3. Afișăm raportul succesului
            afiseazaAlerta("Alertă Trimisă",
                    "Sistemul a emis alerta pentru grupa " + grupa + rh + ".\n" +
                            "Notificări trimise către: " + nrDonatori + " donatori compatibili.",
                    Alert.AlertType.INFORMATION);

            alertMesajField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void afiseazaAlerta(String titlu, String mesaj, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(titlu);
        alert.setHeaderText(null); // Păstrăm header-ul curat
        alert.setContentText(mesaj);

        // Putem adăuga și un pic de stil direct pe fereastra de alertă
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/bloodlife/style.css").toExternalForm());
        dialogPane.getStyleClass().add("my-alert");

        alert.showAndWait();
    }
}