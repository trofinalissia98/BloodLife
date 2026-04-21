package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Intrebare;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.service.ChestionarService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestionarController {
    @FXML private VBox questionsContainer;
    @FXML private Label resultLabel;

    private ChestionarService service;
    private Utilizator utilizatorLogat;
    private Map<Intrebare, ToggleGroup> groups = new HashMap<>();

    public void setInitialData(ChestionarService service, Utilizator user) {
        this.service = service;
        this.utilizatorLogat = user;

        // Verificăm dacă VBox-ul a fost injectat de FXML
        if (questionsContainer == null) {
            System.err.println("EROARE: questionsContainer este NULL! Verifica fx:id in FXML.");
            return;
        }

        incarcaIntrebarile();
    }

    private void incarcaIntrebarile() {
        // Curățăm containerul înainte de a adăuga (prevenim dublarea)
        questionsContainer.getChildren().clear();

        List<Intrebare> lista = service.getToateIntrebarile();
        System.out.println("DEBUG: Incep generarea vizuala pentru " + lista.size() + " intrebari.");

        for (Intrebare q : lista) {
            Label lbl = new Label(q.getText());
            lbl.setWrapText(true);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            // Am adaugat textul direct in constructor - acesta va aparea langa bulina
            RadioButton rbDa = new RadioButton("DA");
            RadioButton rbNu = new RadioButton("NU");

            // Ne asiguram ca textul este negru ca sa fie vizibil pe fundalul deschis
            rbDa.setStyle("-fx-text-fill: #333; -fx-cursor: hand;");
            rbNu.setStyle("-fx-text-fill: #333; -fx-cursor: hand;");

            ToggleGroup group = new ToggleGroup();
            rbDa.setToggleGroup(group);
            rbNu.setToggleGroup(group);

            groups.put(q, group);

            // Am marit putin spatiul la 40 pentru a nu fi inghesuite textul si bulina
            HBox hb = new HBox(40, rbDa, rbNu);

            // Stilul tau "top" ramane neschimbat
            VBox questionBox = new VBox(15, lbl, hb);

            // ADAUGĂ ACEASTĂ LINIE pentru a lega stilul din CSS
            questionBox.getStyleClass().add("question-card");

            // Elimină setStyle-ul vechi din Java, deoarece acum stilul stă în CSS
            // questionBox.setStyle("...");


            // Adăugăm în containerul principal
            questionsContainer.getChildren().add(questionBox);
        }
    }

    @FXML
    protected void handleValidare() {
        Map<Intrebare, Boolean> raspunsuri = new HashMap<>();

        for (Intrebare q : groups.keySet()) {
            RadioButton selected = (RadioButton) groups.get(q).getSelectedToggle();
            if (selected == null) {
                resultLabel.setText("Te rugăm să răspunzi la toate întrebările!");
                return;
            }
            raspunsuri.put(q, selected.getText().equals("DA"));

        }

        boolean eligibil = service.valideazaChestionar(utilizatorLogat.getId(), raspunsuri);

        if (eligibil) {
            resultLabel.setText("Felicitări! Ești eligibil. Poți face o programare.");
            resultLabel.setStyle("-fx-text-fill: green;");

            // CREĂM BUTONUL
            Button btnSpreProgramare = new Button("Mergi la Programare");
            btnSpreProgramare.getStyleClass().add("button-primary");
            btnSpreProgramare.setPrefWidth(200);
            btnSpreProgramare.setPrefHeight(45);

            // SETĂM ACȚIUNEA
            btnSpreProgramare.setOnAction(e -> deschideEcranProgramare());

            // O ABORDARE SIGURĂ PENTRU ADĂUGARE:
            // Căutăm HBox-ul din scenă care conține butonul de Validare.
            // Presupunând că resultLabel este deasupra butoanelor, luăm părintele lui resultLabel.
            VBox parent = (VBox) resultLabel.getParent();

            // Verificăm dacă butonul a fost deja adăugat (să nu apară de 10 ori dacă dă click repetat)
            boolean exists = parent.getChildren().stream()
                    .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Mergi la Programare"));

            if (!exists) {
                parent.getChildren().add(btnSpreProgramare);
            }

        } else {
            resultLabel.setText("Ne pare rău, nu îndeplinești criteriile pentru a dona acum.");
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }
    @FXML
    protected void handleCancel() {
        try {
            // Ne întoarcem la dashboard (trebuie să reîncarci dashboard.fxml)
            // O metodă simplă este să ai o metodă showDashboard() în HelloApplication
            HelloApplication.showDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deschideEcranProgramare() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bloodlife/programare.fxml"));
            Parent root = loader.load();

            ProgramareController controller = loader.getController();
            controller.setInitialData(HelloApplication.getProgramareService(), utilizatorLogat);

            questionsContainer.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}