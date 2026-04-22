package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.repository.ProgramareDbRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MedicController {

    @FXML private ListView<String> donatoriAziList;
    @FXML private TextField codPungaField;
    @FXML private ComboBox<String> tipRecoltareCombo;
    @FXML private TextField cantitateField;
    @FXML private TextField tsField;
    @FXML private TextField tdField;
    @FXML private TextArea observatiiField;

    private ProgramareDbRepository repo;
    private Utilizator medicLogat;

    @FXML
    public void initialize() {
        this.repo = new ProgramareDbRepository("jdbc:postgresql://localhost:5432/bloodlife_db", "postgres", "patratel98");

        tipRecoltareCombo.setItems(FXCollections.observableArrayList("Sânge Total", "Plasmă", "Trombocite"));
        tipRecoltareCombo.setValue("Sânge Total");
        generareCodPunga();
        handleIncarcaDonatori();
    }

    public void setInitialData(Utilizator medic) {
        this.medicLogat = medic;
    }

    private void generareCodPunga() {
        codPungaField.setText("BL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    @FXML
    protected void handleIncarcaDonatori() {
        donatoriAziList.getItems().clear();
        List<String> listaReala = repo.getDonatoriInAsteptareAzi();
        if (!listaReala.isEmpty()) {
            donatoriAziList.setItems(FXCollections.observableArrayList(listaReala));
        }
    }

    @FXML
    protected void handleFinalizeaza() {
        String selectat = donatoriAziList.getSelectionModel().getSelectedItem();
        if (selectat == null) {
            afiseazaAlerta("Eroare", "Te rugăm să selectezi un donator din listă!", Alert.AlertType.ERROR);
            return;
        }

        try {
            String[] parti = selectat.split("ID: ");
            Long idProgramare = Long.parseLong(parti[1].trim());

            int ts = Integer.parseInt(tsField.getText());
            int td = Integer.parseInt(tdField.getText());

            if (ts > 180 || td > 110) {
                afiseazaAlerta("Respins", "Tensiune prea mare! Donatorul este inapt astăzi.", Alert.AlertType.WARNING);
                return;
            }

            repo.finalizeazaProcesDonare(
                    idProgramare,
                    codPungaField.getText(),
                    tipRecoltareCombo.getValue(),
                    Integer.parseInt(cantitateField.getText())
            );

            afiseazaAlerta("Succes", "Recoltare finalizată și salvată în stoc!", Alert.AlertType.INFORMATION);
            donatoriAziList.getItems().remove(selectat);
            curataFormular();
            generareCodPunga();

        } catch (Exception e) {
            afiseazaAlerta("Eroare", "Date invalide sau eroare la baza de date!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void curataFormular() {
        tsField.clear(); tdField.clear(); observatiiField.clear();
    }

    private void afiseazaAlerta(String titlu, String mesaj, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(titlu);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        HelloApplication.logout((Node) actionEvent.getSource());
    }

    @FXML
    protected void handleRespinge() {
        String selectat = donatoriAziList.getSelectionModel().getSelectedItem();
        if (selectat != null) {
            donatoriAziList.getItems().remove(selectat);
            afiseazaAlerta("Info", "Donatorul a fost scos din lista de azi.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    protected void handleGoToStoc() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("gestiune_stoc.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) codPungaField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleGoToStatistici() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("statistici_admin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) donatoriAziList.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}