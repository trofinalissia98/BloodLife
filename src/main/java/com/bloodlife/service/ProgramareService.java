package com.bloodlife.service;

import com.bloodlife.domain.CentruDonare;
import com.bloodlife.domain.SlotOra;
import com.bloodlife.repository.ProgramareDbRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProgramareService {
    private ProgramareDbRepository repo;

    public ProgramareService(ProgramareDbRepository repo) {
        this.repo = repo;
    }

    public List<CentruDonare> getCentre() {
        return repo.getToateCentrele();
    }

    /**
     * Generează toate orele posibile și verifică disponibilitatea lor
     */
    public List<SlotOra> getOreDisponibile(CentruDonare centru, LocalDate data) {
        List<SlotOra> sloturi = new ArrayList<>();

        // Definim programul centrului: 08:00 - 16:00, din 30 în 30 de minute
        LocalTime oraStart = LocalTime.of(8, 0);
        LocalTime oraSfarsit = LocalTime.of(16, 0);

        while (oraStart.isBefore(oraSfarsit)) {
            // Verificăm în DB câte programări sunt deja la această oră
            int ocupate = repo.getNrProgramariPeSlot(centru.getId(), data, oraStart);

            // Dacă sunt mai puține decât capacitatea (ex: 5), ora e disponibilă
            boolean disponibil = ocupate < centru.getCapacitatePeOra();

            sloturi.add(new SlotOra(oraStart, disponibil));

            // Trecem la următorul slot (după 30 de minute)
            oraStart = oraStart.plusMinutes(30);
        }

        return sloturi;
    }

    public void creeazaProgramare(Long idDonator, CentruDonare centru, LocalDate data, LocalTime ora) {
        // Aici am putea adăuga și o verificare finală de siguranță
        repo.salveazaProgramare(idDonator, centru.getId(), data, ora);
    }
}