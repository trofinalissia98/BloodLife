package com.bloodlife.repository;

import com.bloodlife.domain.Utilizator;

public interface IUtilizatorRepository extends Repository<Long, Utilizator> {
    // Metodă specifică doar utilizatorilor (pentru Login)
    Utilizator cautaDupaEmail(String email);
}