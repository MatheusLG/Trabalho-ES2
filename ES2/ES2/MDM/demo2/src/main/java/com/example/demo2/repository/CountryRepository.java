package com.example.demo2.repository;

import java.util.List;

import com.example.demo2.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    // Buscar países pelo nome exato
    List<Country> findByName(String name);

    // Buscar países que contenham uma string no nome (case insensitive)
    List<Country> findByNameContainingIgnoreCase(String partialName);

    // Buscar países por capital
    List<Country> findByCapital(String capital);

    // Buscar países com população maior que um valor
    List<Country> findByPopulationGreaterThan(Long population);

    // Buscar países com população entre dois valores
    List<Country> findByPopulationBetween(Long min, Long max);
}
