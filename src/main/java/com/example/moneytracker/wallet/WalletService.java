package com.example.moneytracker.wallet;

import com.example.moneytracker.model.WalletType;
import com.example.moneytracker.security.CurrentUser;
import com.example.moneytracker.user.User;
import com.example.moneytracker.user.UserRepository;
import com.example.moneytracker.wallet.dto.BalanceResponse;
import com.example.moneytracker.wallet.dto.CreateWalletRequest;
import com.example.moneytracker.wallet.dto.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public WalletService(WalletRepository walletRepository,
                         UserRepository userRepository,
                         CurrentUser currentUser) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    private User getCurrentUserEntity() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            throw new NoSuchElementException("No authenticated user");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        User user = getCurrentUserEntity();
        WalletType type = request.type();

        if (walletRepository.existsByUser_IdAndType(user.getId(), type)) {
            throw new IllegalStateException("Wallet of type " + type + " already exists for this user");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setType(type);

        Wallet saved = walletRepository.save(wallet);
        log.info("Created wallet {} for user {} of type {}", saved.getId(), user.getId(), type);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> listWallets() {
        User user = getCurrentUserEntity();
        return walletRepository.findAllByUser_Id(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance() {
        User user = getCurrentUserEntity();
        List<Wallet> wallets = walletRepository.findAllByUser_Id(user.getId());

        BigDecimal cash = wallets.stream()
                .filter(w -> w.getType() == WalletType.CASH)
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal online = wallets.stream()
                .filter(w -> w.getType() == WalletType.ONLINE)
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = cash.add(online);

        return new BalanceResponse(cash, online, total);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getType(),
                wallet.getBalance()
        );
    }
}

