package com.example.moneytracker.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findAllByUser_Id(Long userId);

    Optional<Person> findByIdAndUser_Id(Long id, Long userId);
}

