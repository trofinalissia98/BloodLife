package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.domain.RolUtilizator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

public class AdminController {

    @FXML private TabPane mainTabPane;
    @FXML private Button btnDashboard, btnUsers, btnAlerts;

    @FXML private TableView<Utilizator> utilizatoriTable;
    @FXML private TableColumn<Utilizator, Long> colId;
    @FXML private TableColumn<Utilizator, String> colNume;
    @FXML private TableColumn<Utilizator, String> colEmail;
    @FXML private TableColumn<Utilizator, String> colRol;

    @FXML private ComboBox<String> adminAlertGrupa;
    @FXML private ComboBox<String> adminAlertRh;
    @FXML private TextArea adminAlertMesaj;

    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String user = "postgres";
    private final String pass = "patratel98";

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        adminAlertGrupa.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));
        adminAlertRh.setItems(FXCollections.observableArrayList("+", "-"));

        incarcaUtilizatori();
    }

    @FXML
    public void showDashboardTab() {
        mainTabPane.getSelectionModel().select(0);
        updateSidebarStyle(btnDashboard);
    }

    @FXML
    public void showUsersTab() {
        mainTabPane.getSelectionModel().select(1);
        updateSidebarStyle(btnUsers);
    }

    @FXML
    public void showAlertsTab() {
        mainTabPane.getSelectionModel().select(2);
        updateSidebarStyle(btnAlerts);
    }

    private void updateSidebarStyle(Button activeBtn) {
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnUsers.getStyleClass().remove("nav-button-active");
        btnAlerts.getStyleClass().remove("nav-button-active");

        btnDashboard.getStyleClass().add("nav-button");
        btnUsers.getStyleClass().add("nav-button");
        btnAlerts.getStyleClass().add("nav-button");

        activeBtn.getStyleClass().remove("nav-button");
        activeBtn.getStyleClass().add("nav-button-active");
    }

    @FXML
    private void incarcaUtilizatori() {
        ObservableList<Utilizator> listaUtilizatori = FXCollections.observableArrayList();
        String sql = "SELECT id, nume, email, rol FROM utilizatori ORDER BY id ASC";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utilizator u = new Utilizator();
                u.setId(rs.getLong("id"));
                u.setNume(rs.getString("nume"));
                u.setEmail(rs.getString("email"));
                u.setRol(RolUtilizator.valueOf(rs.getString("rol").toUpperCase()));
                listaUtilizatori.add(u);
            }
            utilizatoriTable.setItems(listaUtilizatori);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleAddMedic() {
        Dialog<Utilizator> dialog = new Dialog<>();
        dialog.setTitle("Adăugare Medic Nou");
        dialog.setHeaderText("Introduceți datele pentru noul cont de medic:");

        ButtonType saveButtonType = new ButtonType("Salvează", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nume = new TextField();
        nume.setPromptText("Nume Complet");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField parola = new PasswordField();
        parola.setPromptText("Parolă");

        grid.add(new Label("Nume:"), 0, 0);
        grid.add(nume, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(email, 1, 1);
        grid.add(new Label("Parolă:"), 0, 2);
        grid.add(parola, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Utilizator(nume.getText(), email.getText(), BCrypt.hashpw(parola.getText(), BCrypt.gensalt()), RolUtilizator.MEDIC);
            }
            return null;
        });

        Optional<Utilizator> result = dialog.showAndWait();

        result.ifPresent(u -> {
            String sql = "INSERT INTO utilizatori (nume, email, parola, rol) VALUES (?, ?, ?, 'MEDIC')";
            try (Connection con = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, u.getNume());
                ps.setString(2, u.getEmail());
                ps.setString(3, u.getParola());
                ps.executeUpdate();
                incarcaUtilizatori();
                afiseazaAlerta("Succes", "Medicul a fost adăugat cu succes!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                afiseazaAlerta("Eroare", "Email-ul există deja sau datele sunt invalide.", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    protected void handleDeactivateUser() {
        Utilizator selectat = utilizatoriTable.getSelectionModel().getSelectedItem();
        if (selectat == null) {
            afiseazaAlerta("Atenție", "Selectați un utilizator din tabel!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare Dezactivare");
        confirm.setContentText("Sigur doriți să ștergeți/dezactivați contul lui " + selectat.getNume() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Momentan facem DELETE pentru că nu avem coloana 'activ' în schema DB a proiectului
            String sql = "DELETE FROM utilizatori WHERE id = ?";
            try (Connection con = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, selectat.getId());
                ps.executeUpdate();
                incarcaUtilizatori();
                afiseazaAlerta("Succes", "Contul a fost eliminat.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                afiseazaAlerta("Eroare", "Utilizatorul are date legate (donări/programări) și nu poate fi șters direct.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    protected void handleResetPassword() {
        Utilizator selectat = utilizatoriTable.getSelectionModel().getSelectedItem();
        if (selectat == null) {
            afiseazaAlerta("Atenție", "Selectați un utilizator din tabel!", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("parola123");
        dialog.setTitle("Resetare Parolă");
        dialog.setHeaderText("Resetare parolă pentru: " + selectat.getNume());
        dialog.setContentText("Introduceți noua parolă:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouaParola -> {
            String hash = BCrypt.hashpw(nouaParola, BCrypt.gensalt());
            String sql = "UPDATE utilizatori SET parola = ? WHERE id = ?";
            try (Connection con = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hash);
                ps.setLong(2, selectat.getId());
                ps.executeUpdate();
                afiseazaAlerta("Succes", "Parola a fost resetată la: " + nouaParola, Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

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
            ps.setString(3, "[ADMIN] " + mesaj);
            ps.executeUpdate();
            afiseazaAlerta("Succes", "Alerta globală a fost trimisă!", Alert.AlertType.INFORMATION);
            adminAlertMesaj.clear();
        } catch (SQLException e) {
            afiseazaAlerta("Eroare", "Nu s-a putut salva alerta!", Alert.AlertType.ERROR);
            e.printStackTrace();
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