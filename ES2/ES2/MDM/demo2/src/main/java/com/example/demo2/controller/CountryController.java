package com.example.demo2.controller;

import com.example.demo2.model.Country;
import com.example.demo2.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryService service;

    @PostMapping
    public Country create(@RequestBody Country country) {
        return service.create(country);
    }

    @PostMapping("/bulk")
    public List<Country> createBulk(@RequestBody List<Country> countries) {
        return service.createBulk(countries);
    }

    @GetMapping
    public List<Country> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Country getById(@PathVariable Long id) {
        return service.getById(id).orElseThrow();
    }

    @PatchMapping("/{id}")
    public Country update(@PathVariable Long id, @RequestBody Country country) {
        return service.update(id, country);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
