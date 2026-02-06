package com.example.moneytracker.transaction;

import com.example.moneytracker.model.TransactionType;
import com.example.moneytracker.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByPerson_Id(Long personId);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.user.id = :userId
              and t.person.id = :personId
              and t.transactionType = :type
            """)
    BigDecimal sumAmountByUserAndPersonAndType(
            @Param("userId") Long userId,
            @Param("personId") Long personId,
            @Param("type") TransactionType type
    );

    @Query("""
            select t
            from Transaction t
            left join t.fromWallet fw
            left join t.toWallet tw
            where t.user.id = :userId
              and (:type is null or t.transactionType = :type)
              and t.date >= :fromDate
              and t.date < :toDate
              and (
                :walletType is null
                or (fw is not null and fw.type = :walletType)
                or (tw is not null and tw.type = :walletType)
              )
            order by t.date desc, t.id desc
            """)
    List<Transaction> searchForUser(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("walletType") WalletType walletType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );
}

