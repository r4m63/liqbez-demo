package ru.liqbez.patient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientController {
    @GetMapping("/{id}")
    public ResponseEntity<Patient> find(@PathVariable long id) {
        return id == 1 ? ResponseEntity.ok(new Patient(1, "Барсик")) : ResponseEntity.notFound().build();
    }

    public record Patient(long id, String name) {}
}
