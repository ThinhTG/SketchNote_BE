package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IWalletRepository extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByUserId(Long userId);
}
