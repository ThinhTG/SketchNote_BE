package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImp implements IWalletService {

    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;

    @Override
    public Wallet createWallet(Long userId) {
        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);
        if (existingWallet.isPresent()) {
            throw new IllegalStateException("User already has a wallet, cannot create another one.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency("VND");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
    }

    @Override
    @Transactional
    public Transaction deposit(Long walletId, BigDecimal amount) {
        Wallet wallet = getWallet(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balance(wallet.getBalance())
                .type(TransactionType.DEPOSIT)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction withdraw(Long walletId, BigDecimal amount) {
        Wallet wallet = getWallet(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balance(wallet.getBalance())
                .type(TransactionType.WITHDRAW)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction chargeCourse(Long walletId, BigDecimal amount) {
        Wallet wallet = getWallet(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balance(wallet.getBalance())
                .type(TransactionType.COURSE_FEE)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }


    @Override
    @Transactional
    public Transaction pay(Long walletId, BigDecimal amount) {
        Wallet wallet = getWallet(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balance(wallet.getBalance())
                .type(TransactionType.PAYMENT)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction payWithType(Long walletId, BigDecimal amount, TransactionType type, String description) {
        Wallet wallet = getWallet(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balance(wallet.getBalance())
                .type(type)
                .status(PaymentStatus.SUCCESS)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactions(Long walletId) {
        return transactionRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("wallet").get("id"), walletId)
        );
    }
}
