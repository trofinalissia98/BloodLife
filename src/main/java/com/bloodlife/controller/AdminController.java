package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.domain.RolUtilizator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class AdminController {

    // --- Elemente Tabel Utilizatori ---
    @FXML private TableView<Utilizator> utilizatoriTable;
    @FXML private TableColumn<Utilizator, Long> colId;
    @FXML private TableColumn<Utilizator, String> colNume;
    @FXML private TableColumn<Utilizator, String> colEmail;
    @FXML private TableColumn<Utilizator, String> colRol;

    // --- Elemente Alerte Globale ---
    @FXML private ComboBox<String> adminAlertGrupa;
    @FXML private ComboBox<String> adminAlertRh;
    @FXML private TextArea adminAlertMesaj;

    // Configurare DB
    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String user = "postgres";
    private final String pass = "patratel98";

    @FXML
    public void initialize() {
        // 1. Configurare coloane TableView
        // Atenție: PropertyValueFactory caută getterele din clasa Utilizator (getId, getNume, etc.)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        // 2. Populare ComboBox-uri pentru Alerte
        adminAlertGrupa.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));
        adminAlertRh.setItems(FXCollections.observableArrayList("+", "-"));

        // 3. Încărcare automată a datelor
        incarcaUtilizatori();
    }

    /**
     * Încarcă toți utilizatorii din baza de date în tabel
     */
    @FXML
    private void incarcaUtilizatori() {
        ObservableList<Utilizator> listaUtilizatori = FXCollections.observableArrayList();
        String sql = "SELECT id, nume, email, rol FROM utilizatori ORDER BY id ASC";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Presupunem că ai un constructor în Utilizator sau folosim setteri
                Utilizator u = new Utilizator();
                u.setId(rs.getLong("id"));
                u.setNume(rs.getString("nume"));
                u.setEmail(rs.getString("email"));

                // Conversie String din DB la Enum-ul tău RolUtilizator
                String rolDb = rs.getString("rol");
                u.setRol(RolUtilizator.valueOf(rolDb.toUpperCase()));

                listaUtilizatori.add(u);
            }
            utilizatoriTable.setItems(listaUtilizatori);

        } catch (SQLException e) {
            afiseazaAlerta("Eroare DB", "Nu s-au putut încărca utilizatorii!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Logica de emitere alertă (UC-07 pentru Admin)
     */
    @FXML
    protected void handleAdminAlerta() {
        String grupa = adminAlertGrupa.getValue();
        String rh = adminAlertRh.getValue();
        String mesaj = adminAlertMesaj.getText();

        if (grupa == null || rh == null || mesaj.trim().isEmpty()) {
            afiseazaAlerta("Atenție", "Completează toate câmpurile alertei!", Alert.AlertType.WARNING);
            return;
        }

        String sql = "INSERT INTO alerte_stoc (grupa_target, rh_target, mesaj, activ) VALUES (?, ?, ?, TRUE)";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, grupa);
            ps.setString(2, rh);
            ps.setString(3, "[ADMIN] " + mesaj); // Adăugăm un tag să știe donatorul că e de la Admin

            ps.executeUpdate();

            afiseazaAlerta("Succes", "Alerta globală a fost trimisă!", Alert.AlertType.INFORMATION);
            adminAlertMesaj.clear();

        } catch (SQLException e) {
            afiseazaAlerta("Eroare", "Nu s-a putut salva alerta!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Șterge utilizatorul selectat din tabel și din DB
     */
    @FXML
    private void handleDeleteUser() {
        Utilizator selectat = utilizatoriTable.getSelectionModel().getSelectedItem();

        if (selectat == null) {
            afiseazaAlerta("Selecție invalidă", "Selectează un utilizator din tabel!", Alert.AlertType.WARNING);
            return;
        }

        // Prevenim adminul să se șteargă pe sine (opțional)
        if (selectat.getRol() == RolUtilizator.ADMIN) {
            afiseazaAlerta("Interzis", "Nu poți șterge un alt administrator!", Alert.AlertType.ERROR);
            return;
        }

        String sql = "DELETE FROM utilizatori WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, selectat.getId());
            ps.executeUpdate();

            utilizatoriTable.getItems().remove(selectat);
            afiseazaAlerta("Succes", "Utilizatorul a fost eliminat.", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            afiseazaAlerta("Eroare", "Utilizatorul are date legate (programări) și nu poate fi șters direct!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        HelloApplication.logout((Node) event.getSource());
    }

    private void afiseazaAlerta(String titlu, String mesaj, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(titlu);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}