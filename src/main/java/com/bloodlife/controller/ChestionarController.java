package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Intrebare;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.service.ChestionarService;
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
        if (questionsContainer != null) {
            incarcaIntrebarile();
        }
    }

    private void incarcaIntrebarile() {
        questionsContainer.getChildren().clear();
        List<Intrebare> lista = service.getToateIntrebarile();

        for (Intrebare q : lista) {
            Label lbl = new Label(q.getText());
            lbl.setWrapText(true);
            // Am eliminat forțarea culorii din Java pentru a lăsa CSS-ul să gestioneze contrastul
            lbl.setStyle("-fx-font-weight: bold;");

            RadioButton rbDa = new RadioButton("DA");
            RadioButton rbNu = new RadioButton("NU");

            ToggleGroup group = new ToggleGroup();
            rbDa.setToggleGroup(group);
            rbNu.setToggleGroup(group);
            groups.put(q, group);

            HBox hb = new HBox(40, rbDa, rbNu);
            VBox questionBox = new VBox(15, lbl, hb);
            questionBox.getStyleClass().add("question-card");
            questionBox.setPadding(new javafx.geometry.Insets(20));

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
            resultLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

            Button btnSpreProgramare = new Button("Mergi la Programare");
            btnSpreProgramare.getStyleClass().add("button-primary");
            btnSpreProgramare.setPrefWidth(250);
            btnSpreProgramare.setPrefHeight(50);
            btnSpreProgramare.setOnAction(e -> deschideEcranProgramare());

            VBox parent = (VBox) resultLabel.getParent();
            boolean exists = parent.getChildren().stream()
                    .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Mergi la Programare"));
            if (!exists) {
                parent.getChildren().add(btnSpreProgramare);
            }
        } else {
            resultLabel.setText("Ne pare rău, nu îndeplinești criteriile pentru a dona acum.");
            resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    @FXML
    protected void handleCancel() {
        try { HelloApplication.showDashboard(); } catch (Exception e) { e.printStackTrace(); }
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