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
import java.util.List;
import java.util.Map;

public class DashboardController {
    // Label-urile vechi + cele noi pentru cardul de programare
    @FXML private Label welcomeLabel, grupaLabel, statusLabel, greutateLabel, urgentaLabel;
    @FXML private Label ziuaLabel, centruLabel, oraLabel; // Adăugate pentru noul design
    @FXML private VBox programareActivaBox; // Cardul cu detalii
    @FXML private VBox noProgramareLabel;   // Mesajul de "GOL" (l-am numit VBox pentru că în FXML i-am pus iconiță și buton)
    // --- ADAUGĂ ACEST CÂMP PENTRU A REȚINE UTILIZATORUL ---


    // --- SESIUNE ---
    private Utilizator utilizatorLogat;

    // --- CONFIGURARE BAZA DE DATE ---
    private final String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
    private final String user = "postgres";
    private final String pass = "patratel98";
    // --- Elemente Chatbot ---
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

            // Verificăm alertele (metoda făcută anterior)
            verificaAlerteUrgenta(donator);
        }

        try {
            ProgramareDbRepository progRepo = new ProgramareDbRepository(
                    "jdbc:postgresql://localhost:5432/bloodlife_db", "postgres", "patratel98");

            // 1. Căutăm dacă există o programare VIITOARE (activă)
            String sqlViitoare = "SELECT p.data_programare, p.ora_programare, c.nume as centru " +
                    "FROM programari p JOIN centre_donare c ON p.id_centru = c.id " +
                    "WHERE p.id_donator = ? AND p.status = 'PROGRAMAT' " +
                    "ORDER BY p.data_programare ASC LIMIT 1";

            // 2. Căutăm ultima donare REUȘITĂ (pentru a calcula limita de timp)
            String sqlUltima = "SELECT data_programare FROM programari " +
                    "WHERE id_donator = ? AND status = 'FINALIZATA' " +
                    "ORDER BY data_programare DESC LIMIT 1";

            try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bloodlife_db", "postgres", "patratel98")) {

                // LOGICA PENTRU PROGRAMAREA ACTIVĂ
                PreparedStatement psV = con.prepareStatement(sqlViitoare);
                psV.setLong(1, user.getId());
                ResultSet rsV = psV.executeQuery();

                if (rsV.next()) {
                    // 1. Luăm datele din DB
                    String dataSql = rsV.getString("data_programare");
                    String centru = rsV.getString("centru");
                    String ora = rsV.getString("ora_programare");

                    // 2. Extragem ziua (din 2026-04-25 luăm 25)
                    String[] partiData = dataSql.split("-");
                    String ziua = partiData[partiData.length - 1];

                    // 3. ACTUALIZĂM NOILE LABEL-URI (Șterge linia cu infoDonareLabel!)
                    ziuaLabel.setText(ziua);
                    centruLabel.setText(centru);
                    oraLabel.setText("🕒 " + ora);

                    // 4. Gestionăm vizibilitatea
                    programareActivaBox.setVisible(true);
                    programareActivaBox.setManaged(true);
                    noProgramareLabel.setVisible(false);
                    noProgramareLabel.setManaged(false);
                } else {
                    programareActivaBox.setVisible(false);
                    programareActivaBox.setManaged(false);
                    noProgramareLabel.setVisible(true);
                    noProgramareLabel.setManaged(true);
                    // Dacă nu are programări viitoare, verificăm când poate dona din nou
                    PreparedStatement psU = con.prepareStatement(sqlUltima);
                    psU.setLong(1, user.getId());
                    ResultSet rsU = psU.executeQuery();

                    if (rsU.next()) {
                        java.sql.Date ultimaData = rsU.getDate("data_programare");
                        // Calculăm data de revenire (ex: +90 zile / 3 luni)
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(ultimaData);
                        cal.add(java.util.Calendar.MONTH, 3);
                        java.util.Date dataRevenire = cal.getTime();
                        java.util.Date azi = new java.util.Date();

                        if (azi.before(dataRevenire)) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                            centruLabel.setText("⏳ Perioadă de recuperare. Poți dona din nou după: " + sdf.format(dataRevenire));
                            centruLabel.setStyle("-fx-text-fill: #e67e22;");
                            // Opțional: dezactivăm butonul de programare nouă
                            // btnProgramare.setDisable(true);
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
        // Auto-scroll inteligent: când VBox-ul de mesaje crește, scroll-ul coboară
        if (messageContainer != null && chatScroll != null) {
            messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                chatScroll.setVvalue(1.0);
            });
        }
    }

    @FXML
    protected void handleProgramareNoua() {
        System.out.println("--- DEBUG: Metoda handleProgramareNoua a fost apelată! ---");

        if (programareActivaBox == null) {
            System.out.println("--- DEBUG: programareActivaBox este NULL! Verifică fx:id în FXML. ---");
            return;
        }

        if (programareActivaBox.isVisible()) {
            System.out.println("--- DEBUG: Metoda se oprește pentru că programareActivaBox este vizibil! ---");
            afiseazaAlerta("Programare existentă", "Ai deja o programare activă.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("--- DEBUG: Trecem la încărcarea FXML-ului... ---");

        try {
            String fxmlPath = "/com/bloodlife/chestionar.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // AQUA ESTE CHEIA: Trebuie să trimiți serviciul și utilizatorul!
            ChestionarController controller = loader.getController();
            // Folosim HelloApplication.getChestionarService() pentru a lua instanța corectă a serviciului
            controller.setInitialData(HelloApplication.getChestionarService(), utilizatorLogat);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodă de ajutor pentru alerte (dacă nu o ai deja în acest controller)
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
    protected void handleLogout(javafx.event.ActionEvent event) {
        HelloApplication.logout((javafx.scene.Node) event.getSource());
    }

    private void verificaAlerteUrgenta(Donator d) {
        String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
        String sql = "SELECT mesaj FROM alerte_stoc WHERE grupa_target = ? AND rh_target = ? AND activ = TRUE ORDER BY data_emiterii DESC LIMIT 1";

        try (java.sql.Connection con = java.sql.DriverManager.getConnection(url, "postgres", "patratel98");
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, d.getGrupaSanguina());
            ps.setString(2, d.getRh());

            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Dacă găsim o alertă activă
                urgentaLabel.setText("🚨 URGENȚĂ: " + rs.getString("mesaj"));
                urgentaLabel.setVisible(true);
                urgentaLabel.setManaged(true); // Face label-ul să ocupe spațiu doar când e vizibil

                // Stil de alertă (Roșu aprins cu scris alb)
                urgentaLabel.setStyle("-fx-background-color: #d63031; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 14px;");
            } else {
                urgentaLabel.setVisible(false);
                urgentaLabel.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("Eroare la verificarea alertelor: " + e.getMessage());
        }
    }

    private long getZileRamasePanaLaDonare() {
        String sql = "SELECT data_programare FROM programari WHERE id_donator = ? AND status = 'FINALIZATA' ORDER BY data_programare DESC LIMIT 1";
        try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bloodlife_db", "postgres", "patratel98");
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, utilizatorLogat.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                java.sql.Date ultimaDonare = rs.getDate("data_programare");

                // Calculăm data minimă pentru următoarea donare (ultima + 90 zile)
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(ultimaDonare);
                cal.add(java.util.Calendar.DAY_OF_YEAR, 90); // 3 luni standard

                long dataViitoareMs = cal.getTimeInMillis();
                long aziMs = System.currentTimeMillis();

                if (aziMs < dataViitoareMs) {
                    // Returnăm diferența în zile
                    return (dataViitoareMs - aziMs) / (1000 * 60 * 60 * 24);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Poate dona (0 zile rămase)
    }

    @FXML
    private void handleAnuleazaProgramare() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmare Anulare");
        alert.setHeaderText("Sigur dorești să anulezi programarea?");
        alert.setContentText("Această acțiune va elibera locul pentru un alt donator.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM programari WHERE id_donator = ? AND status = 'PROGRAMAT'";

            try (Connection con = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setLong(1, utilizatorLogat.getId());
                ps.executeUpdate();

                // Resetăm interfața
                programareActivaBox.setVisible(false);
                programareActivaBox.setManaged(false);
                centruLabel.setText("Programarea a fost anulată. Poți face una nouă.");

                // Reîncărcăm datele inițiale ca să se reactiveze butonul de programare nouă
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
            try (Connection con = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, oraNoua);
                ps.setLong(2, utilizatorLogat.getId());
                ps.executeUpdate();

                // Refresh dashboard
                setInitialData(utilizatorLogat, null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleToggleChat() {
        System.out.println("--- DEBUG: Buton Chat Apăsat! ---"); // Verifică dacă apare asta în consola de jos

        if (chatWindow == null) {
            System.out.println("EROARE: chatWindow este NULL! Verifică fx:id în FXML.");
            return;
        }

        boolean isVisible = chatWindow.isVisible();
        chatWindow.setVisible(!isVisible);
        chatWindow.setManaged(!isVisible);

        System.out.println("Status fereastră: " + (!isVisible));
    }

    @FXML
    private void handleSendMessage() {
        String text = chatInputField.getText().trim();
        if (text.isEmpty()) return;

        // 1. Adaugă mesajul utilizatorului în UI
        addMessageToChat("Tu", text, "user-msg");
        chatInputField.clear();

        // 2. Trimite către AI într-un thread separat (ca să nu blocheze aplicația)
        new Thread(() -> {
            try {
                String response = chatService.getAiResponse(text);

                // Revenim pe thread-ul principal (JavaFX Thread) pentru a actualiza UI-ul
                javafx.application.Platform.runLater(() -> {
                    addMessageToChat("Bloodie", response, "bot-msg");
                });
                // În handleSendMessage, în interiorul thread-ului:
            } catch (Exception e) {
                e.printStackTrace(); // <--- ACEASTĂ LINIE îți va spune în consola IntelliJ exact ce lipsește
                javafx.application.Platform.runLater(() -> {
                    addMessageToChat("Eroare", "Detalii: " + e.getMessage(), "bot-msg");
                });
            }
        }).start();
    }

    private void addMessageToChat(String sender, String message, String styleClass) {
        Label label = new Label(sender + ": " + message);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.getStyleClass().add(styleClass);

        messageContainer.getChildren().add(label);

        // Scroll automat la ultimul mesaj
        Platform.runLater(() -> {
            // messageContainer este VBox-ul din interiorul ScrollPane-ului
            // chatScroll este ScrollPane-ul tău
            chatScroll.setVvalue(1.0); // 1.0 înseamnă maxim jos (100%)
        });
    }

}