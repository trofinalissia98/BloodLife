package com.bloodlife.repository;

import java.util.List;

public interface Repository<ID, T> {
    void adauga(T entitate);
    void sterge(ID id);
    void actualizeaza(T entitate);
    T cautaDupaId(ID id);
    List<T> extrageToate();
}