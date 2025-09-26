package com.sketchnotes.payment_service.service.implement;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.entity.enumeration.TransactionType;
import com.sketchnotes.payment_service.repository.TransactionRepository;
import com.sketchnotes.payment_service.repository.WalletRepository;
import com.sketchnotes.payment_service.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImp implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Wallet createWallet(Long userId) {
        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);
        if (existingWallet.isPresent()) {
            throw new IllegalStateException("User đã có ví, không thể tạo thêm.");
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
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
    @Transactional
    public Transaction deposit(Long walletId, BigDecimal amount) {
        Wallet wallet = getWallet(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
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
                .type(TransactionType.WITHDRAW)
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
                .type(TransactionType.PAYMENT)
                .status(PaymentStatus.SUCCESS)
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
