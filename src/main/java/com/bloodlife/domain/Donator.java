package com.bloodlife.domain;

import java.time.LocalDate;

public class Donator {
    private Long idUtilizator;
    private String grupaSanguina;
    private String rh;
    private Double greutate;
    private LocalDate dataNasterii;
    private LocalDate ultimaDonare;
    private boolean esteEligibil;

    public Donator(Long idUtilizator, String grupaSanguina, String rh, Double greutate,
                   LocalDate dataNasterii, LocalDate ultimaDonare, boolean esteEligibil) {
        this.idUtilizator = idUtilizator;
        this.grupaSanguina = grupaSanguina;
        this.rh = rh;
        this.greutate = greutate;
        this.dataNasterii = dataNasterii;
        this.ultimaDonare = ultimaDonare;
        this.esteEligibil = esteEligibil;
    }

    public Long getIdUtilizator() { return idUtilizator; }
    public String getGrupaSanguina() { return grupaSanguina; }
    public String getRh() { return rh; }
    public Double getGreutate() { return greutate; }
    public LocalDate getDataNasterii() { return dataNasterii; }
    public LocalDate getUltimaDonare() { return ultimaDonare; }
    public boolean isEsteEligibil() { return esteEligibil; }

    public void setIdUtilizator(Long idUtilizator) { this.idUtilizator = idUtilizator; }
    public void setGrupaSanguina(String grupaSanguina) { this.grupaSanguina = grupaSanguina; }
    public void setRh(String rh) { this.rh = rh; }
    public void setGreutate(Double greutate) { this.greutate = greutate; }
    public void setDataNasterii(LocalDate dataNasterii) { this.dataNasterii = dataNasterii; }
    public void setUltimaDonare(LocalDate ultimaDonare) { this.ultimaDonare = ultimaDonare; }
    public void setEsteEligibil(boolean esteEligibil) { this.esteEligibil = esteEligibil; }

    @Override
    public String toString() {
        return "Donator{" +
                "id=" + idUtilizator +
                ", grupa='" + grupaSanguina + '\'' +
                ", RH='" + rh + '\'' +
                ", eligibil=" + esteEligibil +
                '}';
    }
}