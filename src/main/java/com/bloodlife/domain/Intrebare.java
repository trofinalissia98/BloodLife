package com.bloodlife.domain;

public class Intrebare {
    private Long id;
    private String text;
    private boolean raspunsAdmisibil;

    public Intrebare(Long id, String text, boolean raspunsAdmisibil) {
        this.id = id; this.text = text; this.raspunsAdmisibil = raspunsAdmisibil;
    }
    // Getter-e
    public String getText() { return text; }
    public boolean getRaspunsAdmisibil() { return raspunsAdmisibil; }
}