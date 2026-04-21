package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.Donator;
import com.bloodlife.domain.RolUtilizator;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.service.ServiceException;
import com.bloodlife.service.UtilizatorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField parolaField;
    @FXML private Label statusLabel;

    private UtilizatorService service;

    // Metodă pentru a injecta service-ul din Main
    public void setService(UtilizatorService service) {
        this.service = service;
    }

    @FXML
    protected void handleLogin() {
        String email = emailField.getText();
        String parola = parolaField.getText();

        if (email.isEmpty() || parola.isEmpty()) {
            statusLabel.setText("Vă rugăm să completați toate câmpurile!");
            return;
        }

        try {
            // 1. Efectuăm login-ul prin service
            Utilizator user = service.login(email, parola);

            // 2. Apelăm metoda de redirecționare din HelloApplication
            // Această metodă se ocupă acum de setUtilizatorCurent și de alegerea scenei corecte (Donator/Medic)
            HelloApplication.handleUserRedirection(user);

        } catch (ServiceException e) {
            statusLabel.setText(e.getMessage());
        } catch (IOException e) {
            statusLabel.setText("Eroare critică la încărcarea interfeței!");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleGoToRegister() {
        try {
            // Aceasta metoda trebuie sa existe in HelloApplication si sa incarce register.fxml
            HelloApplication.showRegisterScene();
        } catch (Exception e) {
            statusLabel.setText("Eroare la deschiderea ferestrei de înregistrare!");
            e.printStackTrace();
        }
    }
}