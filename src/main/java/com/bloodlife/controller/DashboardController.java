package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Donator;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.repository.ProgramareDbRepository;
import com.bloodlife.service.ChatService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class DashboardController {
    @FXML private Label welcomeLabel, grupaLabel, statusLabel, greutateLabel, urgentaLabel;
    @FXML private Label ziuaLabel, centruLabel, oraLabel;
    @FXML private VBox programareActivaBox;
    @FXML private VBox noProgramareLabel;

    private Utilizator utilizatorLogat;

    // Redenumite pentru a evita shadowing
    private final String dbUrl = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String dbUser = "postgres";
    private final String dbPass = "patratel98";

    @FXML private VBox chatWindow, messageContainer;
    @FXML private TextField chatInputField;
    @FXML private ScrollPane chatScroll;
    private ChatService chatService = new ChatService();


    public void setInitialData(Utilizator user, Donator donator) {
        this.utilizatorLogat = user;
        welcomeLabel.setText("Bună, " + user.getNume() + "!");

        if (donator != null) {
            grupaLabel.setText(donator.getGrupaSanguina() + donator.getRh());
            greutateLabel.setText(donator.getGreutate() + " kg");
            verificaAlerteUrgenta(donator);
        }

        try {
            String sqlViitoare = "SELECT p.data_programare, p.ora_programare, c.nume as centru " +
                    "FROM programari p JOIN centre_donare c ON p.id_centru = c.id " +
                    "WHERE p.id_donator = ? AND p.status = 'PROGRAMAT' " +
                    "ORDER BY p.data_programare ASC LIMIT 1";

            String sqlUltima = "SELECT data_programare FROM programari " +
                    "WHERE id_donator = ? AND status = 'FINALIZATA' " +
                    "ORDER BY data_programare DESC LIMIT 1";

            // Corectat: Folosim dbUrl, dbUser, dbPass
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                PreparedStatement psV = con.prepareStatement(sqlViitoare);
                psV.setLong(1, user.getId());
                ResultSet rsV = psV.executeQuery();

                if (rsV.next()) {
                    String dataSql = rsV.getString("data_programare");
                    String centru = rsV.getString("centru");
                    String ora = rsV.getString("ora_programare");

                    String[] partiData = dataSql.split("-");
                    String ziua = partiData[partiData.length - 1];

                    ziuaLabel.setText(ziua);
                    centruLabel.setText(centru);
                    oraLabel.setText("🕒 " + ora);

                    programareActivaBox.setVisible(true);
                    programareActivaBox.setManaged(true);
                    noProgramareLabel.setVisible(false);
                    noProgramareLabel.setManaged(false);
                } else {
                    programareActivaBox.setVisible(false);
                    programareActivaBox.setManaged(false);
                    noProgramareLabel.setVisible(true);
                    noProgramareLabel.setManaged(true);

                    PreparedStatement psU = con.prepareStatement(sqlUltima);
                    psU.setLong(1, user.getId());
                    ResultSet rsU = psU.executeQuery();

                    if (rsU.next()) {
                        java.sql.Date ultimaData = rsU.getDate("data_programare");
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(ultimaData);
                        cal.add(java.util.Calendar.MONTH, 3);
                        java.util.Date dataRevenire = cal.getTime();
                        java.util.Date azi = new java.util.Date();

                        if (azi.before(dataRevenire)) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                            centruLabel.setText("⏳ Perioadă de recuperare. Poți dona din nou după: " + sdf.format(dataRevenire));
                            centruLabel.setStyle("-fx-text-fill: #e67e22;");
                        } else {
                            centruLabel.setText("✅ Ești gata pentru o nouă donare! Programează-te acum.");
                            centruLabel.setStyle("-fx-text-fill: #27ae60;");
                        }
                    } else {
                       centruLabel.setText("Ești la prima donare? Salvează o viață azi!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        if (messageContainer != null && chatScroll != null) {
            messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> chatScroll.setVvalue(1.0));
            });
        }
    }

    @FXML
    protected void handleProgramareNoua() {
        if (programareActivaBox != null && programareActivaBox.isVisible()) {
            afiseazaAlerta("Programare existentă", "Ai deja o programare activă.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bloodlife/chestionar.fxml"));
            Parent root = loader.load();
            ChestionarController controller = loader.getController();
            controller.setInitialData(HelloApplication.getChestionarService(), utilizatorLogat);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afiseazaAlerta(String titlu, String mesaj, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(titlu);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    @FXML
    protected void handleIstoric() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bloodlife/istoric.fxml"));
            Parent root = loader.load();
            IstoricController controller = loader.getController();
            controller.setInitialData(utilizatorLogat);
            welcomeLabel.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    protected void handleVeziCentre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bloodlife/centre.fxml"));
            Parent root = loader.load();
            welcomeLabel.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    protected void handleLogout(javafx.event.ActionEvent event) {
        HelloApplication.logout((javafx.scene.Node) event.getSource());
    }

    private void verificaAlerteUrgenta(Donator d) {
        String sql = "SELECT mesaj FROM alerte_stoc WHERE grupa_target = ? AND rh_target = ? AND activ = TRUE ORDER BY data_emiterii DESC LIMIT 1";

        try (java.sql.Connection con = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPass);
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, d.getGrupaSanguina());
            ps.setString(2, d.getRh());

            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                urgentaLabel.setText("🚨 URGENȚĂ: " + rs.getString("mesaj"));
                urgentaLabel.setVisible(true);
                urgentaLabel.setManaged(true);
                urgentaLabel.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-padding: 15; -fx-font-weight: bold; -fx-background-radius: 10; -fx-font-size: 14px;");
            } else {
                urgentaLabel.setVisible(false);
                urgentaLabel.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("Eroare la verificarea alertelor: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnuleazaProgramare() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmare Anulare");
        alert.setHeaderText("Sigur dorești să anulezi programarea?");
        alert.setContentText("Această acțiune va elibera locul pentru un alt donator.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM programari WHERE id_donator = ? AND status = 'PROGRAMAT'";
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, utilizatorLogat.getId());
                ps.executeUpdate();
                programareActivaBox.setVisible(false);
                programareActivaBox.setManaged(false);
                centruLabel.setText("Programarea a fost anulată. Poți face una nouă.");
                setInitialData(utilizatorLogat, null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleModificaProgramare() {
        TextInputDialog dialog = new TextInputDialog("10:00");
        dialog.setTitle("Modificare Oră");
        dialog.setHeaderText("Alege o oră nouă pentru vizita ta:");
        dialog.setContentText("Ora (HH:mm):");

        dialog.showAndWait().ifPresent(oraNoua -> {
            String sql = "UPDATE programari SET ora_programare = ? WHERE id_donator = ? AND status = 'PROGRAMAT'";
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, oraNoua);
                ps.setLong(2, utilizatorLogat.getId());
                ps.executeUpdate();
                setInitialData(utilizatorLogat, null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleToggleChat() {
        if (chatWindow == null) return;
        boolean isVisible = chatWindow.isVisible();
        chatWindow.setVisible(!isVisible);
        chatWindow.setManaged(!isVisible);
    }

    @FXML
    private void handleSendMessage() {
        String text = chatInputField.getText().trim();
        if (text.isEmpty()) return;

        addMessageToChat("Tu", text, "user-msg");
        chatInputField.clear();

        new Thread(() -> {
            try {
                String response = chatService.getAiResponse(text);
                Platform.runLater(() -> addMessageToChat("Bloodie", response, "bot-msg"));
            } catch (Exception e) {
                Platform.runLater(() -> addMessageToChat("Eroare", "Detalii: " + e.getMessage(), "bot-msg"));
            }
        }).start();
    }

    private void addMessageToChat(String sender, String message, String styleClass) {
        Label label = new Label(sender + ": " + message);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.getStyleClass().add(styleClass);

        messageContainer.getChildren().add(label);
    }

}