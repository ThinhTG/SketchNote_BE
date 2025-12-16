package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.model.VerifyToken;
import com.sketchnotes.identityservice.repository.IVerifyTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulingTasksApplication {
   private final IVerifyTokenRepository verifyTokenRepository;

    @Scheduled(cron = "0 * * * * ?")
    public void cleanUpExpiredTokens() {
        List<VerifyToken> listToken =  verifyTokenRepository.findVerifyTokensByUsedFalse();
        for (VerifyToken token : listToken) {
            if (token.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
                token.setUsed(true);
            }
        }
        verifyTokenRepository.saveAll(listToken);
    }
}
