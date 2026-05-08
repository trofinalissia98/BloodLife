package com.bloodlife.service;

import com.bloodlife.domain.Donator;
import com.bloodlife.domain.RolUtilizator;
import com.bloodlife.domain.Utilizator;
import com.bloodlife.repository.IDonatorRepository;
import com.bloodlife.repository.IUtilizatorRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;

public class UtilizatorService {
    private final IUtilizatorRepository utilizatorRepo;
    private final IDonatorRepository donatorRepo; // Avem nevoie de ambele repo-uri

    public UtilizatorService(IUtilizatorRepository utilizatorRepo, IDonatorRepository donatorRepo) {
        this.utilizatorRepo = utilizatorRepo;
        this.donatorRepo = donatorRepo;
    }

    public void inregistrareDonator(String nume, String email, String parolaRaw,
                                    String grupa, String rh, Double greutate, LocalDate dataNasterii) {

        if (email == null || !email.contains("@")) throw new ServiceException("Email invalid!");
        if (greutate < 50) throw new ServiceException("Greutatea minimă trebuie să fie de 50kg!");
        if (utilizatorRepo.cautaDupaEmail(email) != null) throw new ServiceException("Email deja existent!");

        String parolaHash = BCrypt.hashpw(parolaRaw, BCrypt.gensalt());

        Utilizator userNou = new Utilizator(nume, email, parolaHash, RolUtilizator.DONATOR);
        utilizatorRepo.adauga(userNou);

        Utilizator userSalvat = utilizatorRepo.cautaDupaEmail(email);

        Donator donatorNou = new Donator(
                userSalvat.getId(),
                grupa,
                rh,
                greutate,
                dataNasterii,
                null,
                true
        );
        donatorRepo.adauga(donatorNou);
    }

    public Utilizator login(String email, String parolaRaw) {
        Utilizator user = utilizatorRepo.cautaDupaEmail(email);

        if (user == null) {
            System.out.println("DEBUG: Utilizatorul cu email-ul " + email + " nu exista in baza de date.");
            throw new ServiceException("Utilizatorul nu a fost găsit!");
        }

        System.out.println("DEBUG: Utilizator gasit: " + user.getEmail());
        System.out.println("DEBUG: Hash din DB: [" + user.getParola() + "]");

        boolean match = BCrypt.checkpw(parolaRaw, user.getParola());
        System.out.println("DEBUG: Rezultat comparare BCrypt: " + match);

        if (!match) {
            throw new ServiceException("Date de autentificare invalide!");
        }
        return user;
    }

    public Donator getDonatorData(Long idUtilizator) {
        Donator donator = donatorRepo.cautaDupaId(idUtilizator);

        if (donator == null) {
            return null;
        }

        return donator;
    }
}