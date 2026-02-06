package com.example.moneytracker.person;

import com.example.moneytracker.person.dto.CreatePersonRequest;
import com.example.moneytracker.person.dto.PersonSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public ResponseEntity<?> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        PersonSummaryResponse response = personService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<PersonSummaryResponse> listPeople() {
        return personService.listPeople();
    }

    @GetMapping("/{id}")
    public PersonSummaryResponse getPerson(@PathVariable Long id) {
        return personService.getPerson(id);
    }

    @GetMapping("/{id}/ledger")
    public PersonSummaryResponse getPersonLedger(@PathVariable Long id) {
        return personService.getPerson(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}

