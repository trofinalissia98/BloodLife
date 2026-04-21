package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.CentruDonare;
import com.bloodlife.domain.SlotOra;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.service.ProgramareService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ProgramareController {
    @FXML private ComboBox<CentruDonare> centruCombo;
    @FXML private DatePicker dataPicker;
    @FXML private FlowPane oreContainer;
    @FXML private Button btnConfirma;

    private ProgramareService service;
    private Utilizator utilizatorLogat;

    private LocalTime oraSelectata;

    public void setInitialData(ProgramareService service, Utilizator user) {
        this.service = service;
        this.utilizatorLogat = user;

        // Incarcam centrele in ComboBox
        centruCombo.getItems().addAll(service.getCentre());

        // Blocăm datele din trecut în DatePicker
        dataPicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Ascultăm când se schimbă centrul sau data pentru a reîncărca orele
        centruCombo.setOnAction(e -> actualizeazaOrele());
        dataPicker.setOnAction(e -> actualizeazaOrele());
    }

    private void actualizeazaOrele() {
        CentruDonare centru = centruCombo.getValue();
        LocalDate data = dataPicker.getValue();

        if (centru != null && data != null) {
            oreContainer.getChildren().clear();
            oraSelectata = null;
            btnConfirma.setDisable(true);

            List<SlotOra> sloturi = service.getOreDisponibile(centru, data);

            for (SlotOra slot : sloturi) {
                Button btnOra = new Button(slot.ora().toString());

                // ADAUGĂ CLASA CSS
                btnOra.getStyleClass().add("time-slot-button");

                if (!slot.disponibil()) {
                    btnOra.setDisable(true);
                    btnOra.setOpacity(0.4);
                } else {
                    btnOra.setOnAction(e -> {
                        // Ștergem stilul de "selectat" de la toate
                        oreContainer.getChildren().forEach(n -> n.getStyleClass().remove("time-slot-button-selected"));

                        // Îl adăugăm la cel curent (va trebui să definești .time-slot-button-selected în CSS)
                        btnOra.getStyleClass().add("time-slot-button-selected");

                        oraSelectata = slot.ora();
                        btnConfirma.setDisable(false);
                    });
                }
                oreContainer.getChildren().add(btnOra);
            }
        }
    }

    @FXML
    protected void handleConfirmare() {
        if (centruCombo.getValue() != null && dataPicker.getValue() != null && oraSelectata != null) {
            try {
                service.creeazaProgramare(
                        utilizatorLogat.getId(),
                        centruCombo.getValue(),
                        dataPicker.getValue(),
                        oraSelectata
                );

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succes");
                alert.setHeaderText(null);
                alert.setContentText("Programarea a fost înregistrată cu succes pentru ora " + oraSelectata + "!");
                alert.showAndWait();

                // Ne întoarcem la Dashboard
                HelloApplication.showDashboard();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Eroare la salvarea programării: " + e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    protected void handleCancel() {
        try {
            HelloApplication.showDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}