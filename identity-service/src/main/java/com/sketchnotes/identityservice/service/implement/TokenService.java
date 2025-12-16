package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.VerifyToken;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IVerifyTokenRepository;
import com.sketchnotes.identityservice.service.interfaces.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
   private final IVerifyTokenRepository verifyTokenRepository;
   private final IUserRepository userRepo;
    @Transactional
    public String generateNewVerifyToken(User user) {

        // 1. Invalidate all old tokens
        List<VerifyToken> oldTokens =
                verifyTokenRepository.findByUserAndUsedFalse(user);

        for (VerifyToken token : oldTokens) {
            token.setUsed(true);
        }
        verifyTokenRepository.saveAll(oldTokens);

        // 2. Create new token
        String newToken = UUID.randomUUID().toString();

        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken(newToken);
        verifyToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        verifyToken.setUsed(false);

        verifyTokenRepository.save(verifyToken);

        // 3. RETURN token mới
        return newToken;
    }
    @Transactional
    public void verifyEmail(String token) {

        VerifyToken verifyToken = verifyTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verifyToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = verifyToken.getUser();
        user.setVerified(true);

        userRepo.save(user);
        verifyToken.setUsed(true);
        verifyTokenRepository.save(verifyToken);
    }
}
