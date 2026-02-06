package com.example.moneytracker.wallet;

import com.example.moneytracker.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByUser_Id(Long userId);

    Optional<Wallet> findByUser_IdAndType(Long userId, WalletType type);

    boolean existsByUser_IdAndType(Long userId, WalletType type);
}

