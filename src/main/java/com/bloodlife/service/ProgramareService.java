package com.bloodlife.service;

import com.bloodlife.domain.CentruDonare;
import com.bloodlife.domain.Donator;
import com.bloodlife.domain.SlotOra;
import com.bloodlife.repository.IDonatorRepository;
import com.bloodlife.repository.ProgramareDbRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ProgramareService {
    private ProgramareDbRepository repo;
    private IDonatorRepository donatorRepo;

    public ProgramareService(ProgramareDbRepository repo, IDonatorRepository donatorRepo) {
        this.repo = repo;
        this.donatorRepo = donatorRepo;
    }

    public List<CentruDonare> getCentre() {
        return repo.getToateCentrele();
    }

    public List<SlotOra> getOreDisponibile(CentruDonare centru, LocalDate data) {
        List<SlotOra> sloturi = new ArrayList<>();
        LocalTime oraStart = LocalTime.of(8, 0);
        LocalTime oraSfarsit = LocalTime.of(16, 0);

        while (oraStart.isBefore(oraSfarsit)) {
            int ocupate = repo.getNrProgramariPeSlot(centru.getId(), data, oraStart);
            boolean disponibil = ocupate < centru.getCapacitatePeOra();
            sloturi.add(new SlotOra(oraStart, disponibil));
            oraStart = oraStart.plusMinutes(30);
        }
        return sloturi;
    }

    public void creeazaProgramare(Long idDonator, CentruDonare centru, LocalDate data, LocalTime ora) throws Exception {
        // Verificare restrictie 30 de zile
        Donator donator = donatorRepo.cautaDupaId(idDonator);
        if (donator != null && donator.getUltimaDonare() != null) {
            long zileTrecute = ChronoUnit.DAYS.between(donator.getUltimaDonare(), LocalDate.now());
            if (zileTrecute < 30) {
                throw new Exception("Nu puteți programa o nouă donare. Au trecut doar " + zileTrecute + 
                    " zile de la ultima donare. Trebuie să așteptați minim 30 de zile.");
            }
        }

        repo.salveazaProgramare(idDonator, centru.getId(), data, ora);
    }
}