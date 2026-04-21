package com.bloodlife.repository;

import com.bloodlife.domain.Donator;

public interface IDonatorRepository extends Repository<Long, Donator> {
    // Putem adăuga metode specifice, de exemplu pentru a vedea eligibilitatea
  //  boolean verificaEligibilitate(Long idDonator);
}