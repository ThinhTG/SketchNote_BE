package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IVerifyTokenRepository extends JpaRepository<VerifyToken, Long> {
    List<VerifyToken> findByUserAndUsedFalse(User user);
    Optional<VerifyToken> findByToken(String token);
}
