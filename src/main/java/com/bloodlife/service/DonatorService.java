package com.bloodlife.service;

import com.bloodlife.domain.Donator;
import com.bloodlife.repository.IDonatorRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DonatorService {
    private final IDonatorRepository repo;

    public DonatorService(IDonatorRepository repo) { this.repo = repo; }

    public boolean verificaSiActualizeazaEligibilitate(Long idDonator) {
        Donator donator = repo.cautaDupaId(idDonator);
        if (donator == null) return false;

        boolean eligibil = true;

        // Regula 1: Greutatea (conform documentului tău)
        if (donator.getGreutate() < 50) eligibil = false;

        // Regula 2: Intervalul de timp (ex: 90 de zile între donări)
        if (donator.getUltimaDonare() != null) {
            long zileTrecute = ChronoUnit.DAYS.between(donator.getUltimaDonare(), LocalDate.now());
            if (zileTrecute < 90) eligibil = false;
        }

        donator.setEsteEligibil(eligibil);
        repo.actualizeaza(donator);
        return eligibil;
    }
}