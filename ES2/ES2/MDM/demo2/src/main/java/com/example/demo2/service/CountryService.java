package com.example.demo2.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo2.repository.CountryRepository;
import com.example.demo2.model.Country;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {

    @Autowired
    private CountryRepository repository;

    public Country create(Country country) {
        return repository.save(country);
    }

    // Novo método para salvar vários países de uma vez
    public List<Country> createBulk(List<Country> countries) {
        return repository.saveAll(countries);
    }

    public List<Country> getAll() {
        return repository.findAll();
    }

    public Optional<Country> getById(Long id) {
        return repository.findById(id);
    }

    public Country update(Long id, Country updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setCapital(updated.getCapital());
            existing.setPopulation(updated.getPopulation());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Country not found"));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
