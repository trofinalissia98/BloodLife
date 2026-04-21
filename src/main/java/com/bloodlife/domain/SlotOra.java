package com.bloodlife.domain;
import java.time.LocalTime;

public record SlotOra(LocalTime ora, boolean disponibil) {
    @Override
    public String toString() {
        return ora.toString();
    }
}