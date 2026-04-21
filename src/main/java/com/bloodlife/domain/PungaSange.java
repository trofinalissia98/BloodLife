package com.bloodlife.domain;

public class PungaSange {
    private String cod;
    private String tip;
    private String grupa;
    private String rh;
    private int cantitate;
    private String status;
    private String data;

    public PungaSange(String cod, String tip, String grupa, String rh, int cantitate, String status, String data) {
        this.cod = cod; this.tip = tip; this.grupa = grupa; this.rh = rh;
        this.cantitate = cantitate; this.status = status; this.data = data;
    }

    // Getters necesari pentru TableView
    public String getCod() { return cod; }
    public String getTip() { return tip; }
    public String getGrupa() { return grupa; }
    public String getRh() { return rh; }
    public int getCantitate() { return cantitate; }
    public String getStatus() { return status; }
    public String getData() { return data; }
}