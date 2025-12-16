package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {
    private final IUserRepository userRepository;
    private final ITransactionRepository transactionRepository;

    public Map<String, Long> getUserStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("customers", userRepository.countByRole(Role.CUSTOMER));
        stats.put("designers", userRepository.countByRole(Role.DESIGNER));
        return stats;
    }

    public List<Map<String, Object>> getCourseRevenue(LocalDateTime start, LocalDateTime end, String groupBy) {
        List<Object[]> rows;
        String type = TransactionType.COURSE_FEE.name();
        
        if ("month".equalsIgnoreCase(groupBy)) {
            rows = transactionRepository.sumAmountByMonth(type, start, end);
        } else if ("year".equalsIgnoreCase(groupBy)) {
            rows = transactionRepository.sumAmountByYear(type, start, end);
        } else {
            rows = transactionRepository.sumAmountByDay(type, start, end);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("period", row[0]);
            item.put("revenue", row[1]);
            result.add(item);
        }
        return result;
    }
}
