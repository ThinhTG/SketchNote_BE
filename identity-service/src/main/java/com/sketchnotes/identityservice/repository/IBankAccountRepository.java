package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBankAccountRepository extends JpaRepository<BankAccount, Long> {
    
    // Find all bank accounts by user ID
    List<BankAccount> findByUserIdAndIsActiveTrue(Long userId);
    
    // Find all bank accounts (including inactive) by user ID
    List<BankAccount> findByUserId(Long userId);
    
    // Find bank account by ID and user ID
    Optional<BankAccount> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);
    
    // Find default bank account for a user
    Optional<BankAccount> findByUserIdAndIsDefaultTrue(Long userId);
    
    // Check if account number exists for a user
    boolean existsByUserIdAndAccountNumberAndIsActiveTrue(Long userId, String accountNumber);
    
    // Count active bank accounts for a user
    long countByUserIdAndIsActiveTrue(Long userId);
    
    // Set all other accounts as non-default when setting a new default
    @Modifying
    @Query("UPDATE BankAccount b SET b.isDefault = false WHERE b.user.id = :userId AND b.id != :excludeId")
    void unsetDefaultForUser(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
    
    // Set all accounts as non-default for a user
    @Modifying
    @Query("UPDATE BankAccount b SET b.isDefault = false WHERE b.user.id = :userId")
    void unsetAllDefaultForUser(@Param("userId") Long userId);
}
