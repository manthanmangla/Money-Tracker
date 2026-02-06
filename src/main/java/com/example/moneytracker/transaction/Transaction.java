package com.example.moneytracker.transaction;

import com.example.moneytracker.model.TransactionType;
import com.example.moneytracker.person.Person;
import com.example.moneytracker.user.User;
import com.example.moneytracker.wallet.Wallet;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_user", columnList = "user_id"),
                @Index(name = "idx_transactions_person", columnList = "person_id"),
                @Index(name = "idx_transactions_date", columnList = "date")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transaction_type")
    private TransactionType transactionType;

    @Column(nullable = false)
    private Instant date;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_by_id")
    private Transaction reversedBy;

    @Column(nullable = false)
    private boolean isReversal = false;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (date == null) {
            date = createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public void setFromWallet(Wallet fromWallet) {
        this.fromWallet = fromWallet;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public void setToWallet(Wallet toWallet) {
        this.toWallet = toWallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Transaction getReversedBy() {
        return reversedBy;
    }

    public void setReversedBy(Transaction reversedBy) {
        this.reversedBy = reversedBy;
    }

    public boolean isReversal() {
        return isReversal;
    }

    public void setReversal(boolean reversal) {
        isReversal = reversal;
    }
}

