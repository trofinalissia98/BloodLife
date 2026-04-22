package com.bloodlife.controller;

import com.bloodlife.HelloApplication;
import com.bloodlife.domain.CentruDonare;
import com.bloodlife.repository.ProgramareDbRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CentreController {
    @FXML private TableView<CentruDonare> tabelCentre;
    @FXML private TableColumn<CentruDonare, String> colNume, colAdresa;
    @FXML private TableColumn<CentruDonare, Integer> colCapacitate;

    private ProgramareDbRepository progRepo;

    @FXML
    public void initialize() {
        this.progRepo = new ProgramareDbRepository("jdbc:postgresql://localhost:5432/bloodlife_db", "postgres", "patratel98");
        
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colAdresa.setCellValueFactory(new PropertyValueFactory<>("adresa"));
        colCapacitate.setCellValueFactory(new PropertyValueFactory<>("capacitatePeOra"));
        
        incarcaCentre();
    }

    private void incarcaCentre() {
        try {
            List<CentruDonare> centre = progRepo.getToateCentrele();
            tabelCentre.setItems(FXCollections.observableArrayList(centre));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            // FIX: Folosim showMainInterface() pentru a reveni la interfața potrivită rolului
            HelloApplication.showMainInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}