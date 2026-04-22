package com.bloodlife;

import com.bloodlife.controller.*;
import com.bloodlife.domain.Donator;
import com.bloodlife.domain.RolUtilizator;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.repository.*;
import com.bloodlife.service.ChestionarService;
import com.bloodlife.service.ProgramareService;
import com.bloodlife.service.UtilizatorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    private static UtilizatorService utilizatorService;
    private static ChestionarService chestionarService;
    private static ProgramareService programareService;

    private static Utilizator utilizatorCurent;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        String url = "jdbc:postgresql://localhost:5432/bloodlife_db";
        String user = "postgres";
        String pass = "patratel98";

        IUtilizatorRepository uRepo = new UtilizatorDbRepository(url, user, pass);
        IDonatorRepository dRepo = new DonatorDbRepository(url, user, pass);
        ChestionarDbRepository chestionarRepo = new ChestionarDbRepository(url, user, pass);
        ProgramareDbRepository progRepo = new ProgramareDbRepository(url, user, pass);

        programareService = new ProgramareService(progRepo, dRepo);
        chestionarService = new ChestionarService(chestionarRepo);
        utilizatorService = new UtilizatorService(uRepo, dRepo);

        showLoginScene();
    }

    public static void showLoginScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);

        LoginController controller = fxmlLoader.getController();
        controller.setService(utilizatorService);

        primaryStage.setTitle("BloodLife - Autentificare");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * REDIRECȚIONARE INTELIGENTĂ ÎN FUNCȚIE DE ROL
     */
    public static void showMainInterface() throws IOException {
        if (utilizatorCurent == null) {
            showLoginScene();
            return;
        }

        if (utilizatorCurent.getRol() == RolUtilizator.MEDIC) {
            showMedicPanel();
        } else if (utilizatorCurent.getRol() == RolUtilizator.ADMIN) {
            showAdminPanel();
        } else {
            showDashboard();
        }
    }

    public static void handleUserRedirection(Utilizator user) throws IOException {
        setUtilizatorCurent(user);
        showMainInterface();
    }

    public static void showDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dashboard.fxml"));
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        Donator donatorData = utilizatorService.getDonatorData(utilizatorCurent.getId());
        controller.setInitialData(utilizatorCurent, donatorData);

        primaryStage.getScene().setRoot(root);
        primaryStage.setTitle("BloodLife - Dashboard Donator");
    }

    public static void showMedicPanel() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("validare_medicala.fxml"));
        Parent root = loader.load();

        MedicController controller = loader.getController();
        controller.setInitialData(utilizatorCurent);

        primaryStage.getScene().setRoot(root);
        primaryStage.setTitle("BloodLife Pro - Panou Medic");
        primaryStage.centerOnScreen();
    }

    public static void showAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dashboard_admin.fxml"));
            Parent root = loader.load();

            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("BloodLife Admin - Control Panel");
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showRegisterScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 600);
        RegisterController controller = fxmlLoader.getController();
        controller.setService(utilizatorService);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void logout(Node node) {
        utilizatorCurent = null;
        try {
            showLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ChestionarService getChestionarService() { return chestionarService; }
    public static UtilizatorService getUtilizatorService() { return utilizatorService; }
    public static ProgramareService getProgramareService() { return programareService; }
    public static Utilizator getUtilizatorCurent() { return utilizatorCurent; }
    public static void setUtilizatorCurent(Utilizator user) { utilizatorCurent = user; }

    public static void main(String[] args) {
        launch();
    }
}