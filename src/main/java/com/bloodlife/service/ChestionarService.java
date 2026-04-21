package com.bloodlife.service;

import com.bloodlife.domain.Intrebare;
import com.bloodlife.repository.ChestionarDbRepository;
import java.util.List;
import java.util.Map;

public class ChestionarService {
    private ChestionarDbRepository repo;

    public ChestionarService(ChestionarDbRepository repo) {
        this.repo = repo;
    }

    public List<Intrebare> getToateIntrebarile() {
        return repo.extrageIntrebari();
    }

    public boolean valideazaChestionar(Long idDonator, Map<Intrebare, Boolean> raspunsuriUtilizator) {
        boolean esteEligibil = true;

        for (Map.Entry<Intrebare, Boolean> entry : raspunsuriUtilizator.entrySet()) {
            Intrebare q = entry.getKey();
            Boolean raspunsDat = entry.getValue();

            // Dacă răspunsul dat nu coincide cu cel admisibil, e descalificat
            if (raspunsDat != q.getRaspunsAdmisibil()) {
                esteEligibil = false;
                break;
            }
        }

        repo.salveazaRezultat(idDonator, esteEligibil);
        return esteEligibil;
    }
}