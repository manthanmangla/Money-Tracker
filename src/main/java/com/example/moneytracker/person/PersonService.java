package com.example.moneytracker.person;

import com.example.moneytracker.model.TransactionType;
import com.example.moneytracker.person.dto.CreatePersonRequest;
import com.example.moneytracker.person.dto.PersonSummaryResponse;
import com.example.moneytracker.security.CurrentUser;
import com.example.moneytracker.transaction.TransactionRepository;
import com.example.moneytracker.user.User;
import com.example.moneytracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.example.moneytracker.security.ResourceForbiddenException;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public PersonService(PersonRepository personRepository,
                         TransactionRepository transactionRepository,
                         UserRepository userRepository,
                         CurrentUser currentUser) {
        this.personRepository = personRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    private Long requireCurrentUserId() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            throw new NoSuchElementException("No authenticated user");
        }
        return userId;
    }

    private User getCurrentUserEntity() {
        Long userId = requireCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Transactional
    public PersonSummaryResponse createPerson(CreatePersonRequest request) {
        User user = getCurrentUserEntity();

        Person person = new Person();
        person.setUser(user);
        person.setName(request.name());
        person.setPhone(request.phone());
        person.setNotes(request.notes());

        Person saved = personRepository.save(person);
        return toSummary(saved);
    }

    @Transactional(readOnly = true)
    public List<PersonSummaryResponse> listPeople() {
        Long userId = requireCurrentUserId();
        return personRepository.findAllByUser_Id(userId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PersonSummaryResponse getPerson(Long id) {
        Long userId = requireCurrentUserId();
        Person person = personRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Person not found"));
        return toSummary(person);
    }

    @Transactional
    public void deletePerson(Long id) {
        Long userId = requireCurrentUserId();
        Person person = personRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Person not found"));
        if (transactionRepository.existsByPerson_Id(person.getId())) {
            throw new IllegalStateException("Cannot delete person with existing transactions");
        }
        personRepository.delete(person);
    }

    private PersonSummaryResponse toSummary(Person person) {
        Long userId = person.getUser().getId();
        Long personId = person.getId();

        BigDecimal totalReceived = transactionRepository
                .sumAmountByUserAndPersonAndType(userId, personId, TransactionType.RECEIVED);
        BigDecimal totalGiven = transactionRepository
                .sumAmountByUserAndPersonAndType(userId, personId, TransactionType.GIVEN);

        BigDecimal net = totalReceived.subtract(totalGiven);
        String status;
        int cmp = net.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            status = "THEY_OWE_ME";
        } else if (cmp < 0) {
            status = "I_OWE_THEM";
        } else {
            status = "SETTLED";
        }

        return new PersonSummaryResponse(
                person.getId(),
                person.getName(),
                person.getPhone(),
                person.getNotes(),
                totalReceived,
                totalGiven,
                net,
                status
        );
    }
}
