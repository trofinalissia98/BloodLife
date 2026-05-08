package com.bloodlife.repository;

import com.bloodlife.domain.Utilizator;

public interface IUtilizatorRepository extends Repository<Long, Utilizator> {
    Utilizator cautaDupaEmail(String email);
}