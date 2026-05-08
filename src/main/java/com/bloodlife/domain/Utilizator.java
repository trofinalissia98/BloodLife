package com.bloodlife.domain;

public class Utilizator {
    private Long id;
    private String nume;
    private String email;
    private String parola;
    private RolUtilizator rol;

    public Utilizator() {}

    public Utilizator(String nume, String email, String parola, RolUtilizator rol) {
        this.nume = nume;
        this.email = email;
        this.parola = parola;
        this.rol = rol;
    }

    public Utilizator(Long id, String nume, String email, String parola, RolUtilizator rol) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.parola = parola;
        this.rol = rol;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getParola() { return parola; }
    public void setParola(String parola) { this.parola = parola; }

    public RolUtilizator getRol() { return rol; }
    public void setRol(RolUtilizator rol) { this.rol = rol; }

    @Override
    public String toString() {
        return nume + " (" + rol + ")";
    }
}