package com.example.moneytracker.transaction;

import com.example.moneytracker.model.TransactionType;
import com.example.moneytracker.model.WalletType;
import com.example.moneytracker.person.Person;
import com.example.moneytracker.person.PersonRepository;
import com.example.moneytracker.security.CurrentUser;
import com.example.moneytracker.security.ResourceForbiddenException;
import com.example.moneytracker.transaction.dto.CreateTransactionRequest;
import com.example.moneytracker.transaction.dto.TransactionResponse;
import com.example.moneytracker.user.User;
import com.example.moneytracker.user.UserRepository;
import com.example.moneytracker.wallet.Wallet;
import com.example.moneytracker.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              PersonRepository personRepository,
                              UserRepository userRepository,
                              CurrentUser currentUser) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.personRepository = personRepository;
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

    private Wallet findOwnedWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));
        if (!wallet.getUser().getId().equals(userId)) {
            throw new ResourceForbiddenException("Wallet does not belong to current user");
        }
        return wallet;
    }

    private Person findOwnedPerson(Long personId, Long userId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Person not found: " + personId));
        if (!person.getUser().getId().equals(userId)) {
            throw new ResourceForbiddenException("Person does not belong to current user");
        }
        return person;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        User user = getCurrentUserEntity();
        Long userId = user.getId();

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        TransactionType type = request.transactionType();
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(request.amount());
        tx.setTransactionType(type);
        tx.setDescription(request.description());
        tx.setDate(request.date() != null ? request.date() : Instant.now());

        switch (type) {
            case RECEIVED -> handleReceived(request, tx, userId);
            case GIVEN -> handleGiven(request, tx, userId);
            case EXPENSE -> handleExpense(request, tx, userId);
            case INCOME -> handleIncome(request, tx, userId);
            case TRANSFER -> handleTransfer(request, tx, userId);
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        Transaction saved = transactionRepository.save(tx);
        log.info("Created transaction {} of type {} for user {}", saved.getId(), saved.getTransactionType(), userId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(
            WalletType walletType,
            TransactionType type,
            LocalDate from,
            LocalDate to
    ) {
        Long userId = requireCurrentUserId();

        // Use sentinel values instead of null to avoid PostgreSQL "could not determine data type of parameter" error
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.parse("2099-12-31T23:59:59Z");

        List<Transaction> txs = transactionRepository.searchForUser(
                userId,
                type,
                walletType,
                fromInstant,
                toInstant
        );

        return txs.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse reverseTransaction(Long transactionId) {
        User user = getCurrentUserEntity();
        Long userId = user.getId();

        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (!original.getUser().getId().equals(userId)) {
            throw new ResourceForbiddenException("Transaction does not belong to current user");
        }
        if (original.isReversal()) {
            throw new IllegalArgumentException("Reversal transactions cannot be reversed");
        }
        if (original.getReversedBy() != null) {
            throw new IllegalArgumentException("Transaction is already reversed");
        }

        TransactionType reversalType = switch (original.getTransactionType()) {
            case RECEIVED -> TransactionType.GIVEN;
            case GIVEN -> TransactionType.RECEIVED;
            case EXPENSE -> TransactionType.INCOME;
            case INCOME -> TransactionType.EXPENSE;
            case TRANSFER -> TransactionType.TRANSFER;
        };

        Transaction reversal = new Transaction();
        reversal.setUser(user);
        reversal.setAmount(original.getAmount());
        reversal.setTransactionType(reversalType);
        String baseDescription = original.getDescription() != null ? original.getDescription() : "";
        String reversalDescription = ("REVERSAL of #" + original.getId() +
                (baseDescription.isBlank() ? "" : " - " + baseDescription));
        reversal.setDescription(reversalDescription);
        reversal.setDate(Instant.now());
        reversal.setReversal(true);

        switch (original.getTransactionType()) {
            case RECEIVED -> reverseReceived(original, reversal);
            case GIVEN -> reverseGiven(original, reversal);
            case EXPENSE -> reverseExpense(original, reversal);
            case INCOME -> reverseIncome(original, reversal);
            case TRANSFER -> reverseTransfer(original, reversal);
        }

        Transaction savedReversal = transactionRepository.save(reversal);
        original.setReversedBy(savedReversal);

        log.info("Reversed transaction {} with new transaction {} for user {}", original.getId(), savedReversal.getId(), userId);

        return toResponse(savedReversal);
    }

    private void reverseReceived(Transaction original, Transaction reversal) {
        Person person = original.getPerson();
        Wallet toWallet = original.getToWallet();
        if (person == null || toWallet == null) {
            throw new IllegalStateException("Invalid RECEIVED transaction structure");
        }

        reversal.setPerson(person);
        reversal.setFromWallet(toWallet);

        BigDecimal newBalance = toWallet.getBalance().subtract(original.getAmount());
        toWallet.setBalance(newBalance);
    }

    private void reverseGiven(Transaction original, Transaction reversal) {
        Person person = original.getPerson();
        Wallet fromWallet = original.getFromWallet();
        if (person == null || fromWallet == null) {
            throw new IllegalStateException("Invalid GIVEN transaction structure");
        }

        reversal.setPerson(person);
        reversal.setToWallet(fromWallet);

        BigDecimal newBalance = fromWallet.getBalance().add(original.getAmount());
        fromWallet.setBalance(newBalance);
    }

    private void reverseExpense(Transaction original, Transaction reversal) {
        Wallet fromWallet = original.getFromWallet();
        if (fromWallet == null) {
            throw new IllegalStateException("Invalid EXPENSE transaction structure");
        }

        reversal.setToWallet(fromWallet);

        BigDecimal newBalance = fromWallet.getBalance().add(original.getAmount());
        fromWallet.setBalance(newBalance);
    }

    private void reverseIncome(Transaction original, Transaction reversal) {
        Wallet toWallet = original.getToWallet();
        if (toWallet == null) {
            throw new IllegalStateException("Invalid INCOME transaction structure");
        }

        reversal.setFromWallet(toWallet);

        BigDecimal newBalance = toWallet.getBalance().subtract(original.getAmount());
        toWallet.setBalance(newBalance);
    }

    private void reverseTransfer(Transaction original, Transaction reversal) {
        Wallet fromWallet = original.getFromWallet();
        Wallet toWallet = original.getToWallet();
        if (fromWallet == null || toWallet == null) {
            throw new IllegalStateException("Invalid TRANSFER transaction structure");
        }

        // Swap wallets for reversal
        reversal.setFromWallet(toWallet);
        reversal.setToWallet(fromWallet);

        BigDecimal amount = original.getAmount();

        // Undo original: original was fromWallet -= amount, toWallet += amount
        toWallet.setBalance(toWallet.getBalance().subtract(amount));
        fromWallet.setBalance(fromWallet.getBalance().add(amount));
    }

    private void handleReceived(CreateTransactionRequest request, Transaction tx, Long userId) {
        if (request.personId() == null) {
            throw new IllegalArgumentException("personId is required for RECEIVED");
        }
        if (request.toWalletId() == null) {
            throw new IllegalArgumentException("toWalletId is required for RECEIVED");
        }
        if (request.fromWalletId() != null) {
            throw new IllegalArgumentException("fromWalletId must be null for RECEIVED");
        }

        Person person = findOwnedPerson(request.personId(), userId);
        Wallet toWallet = findOwnedWallet(request.toWalletId(), userId);

        tx.setPerson(person);
        tx.setToWallet(toWallet);

        BigDecimal newBalance = toWallet.getBalance().add(request.amount());
        toWallet.setBalance(newBalance);
    }

    private void handleGiven(CreateTransactionRequest request, Transaction tx, Long userId) {
        if (request.personId() == null) {
            throw new IllegalArgumentException("personId is required for GIVEN");
        }
        if (request.fromWalletId() == null) {
            throw new IllegalArgumentException("fromWalletId is required for GIVEN");
        }
        if (request.toWalletId() != null) {
            throw new IllegalArgumentException("toWalletId must be null for GIVEN");
        }

        Person person = findOwnedPerson(request.personId(), userId);
        Wallet fromWallet = findOwnedWallet(request.fromWalletId(), userId);

        tx.setPerson(person);
        tx.setFromWallet(fromWallet);

        BigDecimal newBalance = fromWallet.getBalance().subtract(request.amount());
        fromWallet.setBalance(newBalance);
    }

    private void handleExpense(CreateTransactionRequest request, Transaction tx, Long userId) {
        if (request.personId() != null) {
            throw new IllegalArgumentException("personId must be null for EXPENSE");
        }
        if (request.fromWalletId() == null) {
            throw new IllegalArgumentException("fromWalletId is required for EXPENSE");
        }
        if (request.toWalletId() != null) {
            throw new IllegalArgumentException("toWalletId must be null for EXPENSE");
        }

        Wallet fromWallet = findOwnedWallet(request.fromWalletId(), userId);
        tx.setFromWallet(fromWallet);

        BigDecimal newBalance = fromWallet.getBalance().subtract(request.amount());
        fromWallet.setBalance(newBalance);
    }

    private void handleIncome(CreateTransactionRequest request, Transaction tx, Long userId) {
        if (request.personId() != null) {
            throw new IllegalArgumentException("personId must be null for INCOME");
        }
        if (request.toWalletId() == null) {
            throw new IllegalArgumentException("toWalletId is required for INCOME");
        }
        if (request.fromWalletId() != null) {
            throw new IllegalArgumentException("fromWalletId must be null for INCOME");
        }

        Wallet toWallet = findOwnedWallet(request.toWalletId(), userId);
        tx.setToWallet(toWallet);

        BigDecimal newBalance = toWallet.getBalance().add(request.amount());
        toWallet.setBalance(newBalance);
    }

    private void handleTransfer(CreateTransactionRequest request, Transaction tx, Long userId) {
        if (request.personId() != null) {
            throw new IllegalArgumentException("personId must be null for TRANSFER");
        }
        if (request.fromWalletId() == null || request.toWalletId() == null) {
            throw new IllegalArgumentException("fromWalletId and toWalletId are required for TRANSFER");
        }
        if (request.fromWalletId().equals(request.toWalletId())) {
            throw new IllegalArgumentException("fromWalletId and toWalletId must be different for TRANSFER");
        }

        Wallet fromWallet = findOwnedWallet(request.fromWalletId(), userId);
        Wallet toWallet = findOwnedWallet(request.toWalletId(), userId);

        tx.setFromWallet(fromWallet);
        tx.setToWallet(toWallet);

        fromWallet.setBalance(fromWallet.getBalance().subtract(request.amount()));
        toWallet.setBalance(toWallet.getBalance().add(request.amount()));
    }

    private TransactionResponse toResponse(Transaction tx) {
        Long personId = tx.getPerson() != null ? tx.getPerson().getId() : null;
        Long fromWalletId = tx.getFromWallet() != null ? tx.getFromWallet().getId() : null;
        Long toWalletId = tx.getToWallet() != null ? tx.getToWallet().getId() : null;

        return new TransactionResponse(
                tx.getId(),
                personId,
                fromWalletId,
                toWalletId,
                tx.getAmount(),
                tx.getTransactionType(),
                tx.getDescription(),
                tx.getDate(),
                tx.getCreatedAt()
        );
    }
}

