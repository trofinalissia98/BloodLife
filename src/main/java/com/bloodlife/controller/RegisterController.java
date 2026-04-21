package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.service.ServiceException;
import com.bloodlife.service.UtilizatorService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML private TextField numeField;
    @FXML private TextField emailField;
    @FXML private PasswordField parolaField;
    @FXML private ComboBox<String> grupaCombo;
    @FXML private ComboBox<String> rhCombo;
    @FXML private TextField greutateField;
    @FXML private DatePicker dataNasteriiPicker;
    @FXML private Label statusLabel;

    private UtilizatorService service;

    public void setService(UtilizatorService service) {
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populam ComboBox-ul pentru Grupa Sanguina
        grupaCombo.setItems(FXCollections.observableArrayList("0", "A", "B", "AB"));

        // Populam ComboBox-ul pentru RH
        rhCombo.setItems(FXCollections.observableArrayList("+", "-"));

        // Opțional: Punem o valoare default ca să nu fie goale la început
        grupaCombo.setPromptText("Selectează Grupa");
        rhCombo.setPromptText("Selectează RH");
    }

    @FXML
    protected void handleRegister() {
        // Resetăm mesajul de status
        statusLabel.setText("");
        statusLabel.setStyle("-fx-text-fill: red;");

        try {
            // 1. Preluăm datele din interfață
            String nume = numeField.getText();
            String email = emailField.getText();
            String parola = parolaField.getText();
            String grupa = grupaCombo.getValue();
            String rh = rhCombo.getValue();

            // Validare minimă înainte de parsare
            if (nume.isEmpty() || email.isEmpty() || parola.isEmpty() || grupa == null || rh == null || dataNasteriiPicker.getValue() == null) {
                throw new ServiceException("Toate câmpurile sunt obligatorii!");
            }

            Double greutate = Double.parseDouble(greutateField.getText());
            LocalDate dataNasterii = dataNasteriiPicker.getValue();

            // 2. Apelăm metoda din Service
            service.inregistrareDonator(nume, email, parola, grupa, rh, greutate, dataNasterii);

            // 3. Succes
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Cont creat cu succes! Te poți loga.");

            // Opțional: Curățăm câmpurile după înregistrare
            clearFields();

        } catch (NumberFormatException e) {
            statusLabel.setText("Greutatea trebuie să fie un număr valid!");
        } catch (ServiceException e) {
            statusLabel.setText(e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("A apărut o eroare neașteptată.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleBackToLogin() {
        try {
            HelloApplication.showLoginScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        numeField.clear();
        emailField.clear();
        parolaField.clear();
        greutateField.clear();
        grupaCombo.setValue(null);
        rhCombo.setValue(null);
        dataNasteriiPicker.setValue(null);
    }
}