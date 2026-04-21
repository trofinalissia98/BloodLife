package com.bloodlife.domain;

public class CentruDonare {
    private Long id;
    private String nume;
    private String adresa;
    private int capacitatePeOra;

    public CentruDonare(Long id, String nume, String adresa, int capacitatePeOra) {
        this.id = id;
        this.nume = nume;
        this.adresa = adresa;
        this.capacitatePeOra = capacitatePeOra;
    }

    // Getters
    public Long getId() { return id; }
    public String getNume() { return nume; }
    public String getAdresa() { return adresa; }
    public int getCapacitatePeOra() { return capacitatePeOra; }

    @Override
    public String toString() { return nume; } // Important pentru ComboBox
}