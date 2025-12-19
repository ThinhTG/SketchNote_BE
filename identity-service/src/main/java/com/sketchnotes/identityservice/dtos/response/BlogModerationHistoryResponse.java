package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.BlogStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogModerationHistoryResponse {
    private Long id;
    private Long blogId;
    private BlogStatus previousStatus;
    private BlogStatus newStatus;
    private Boolean isSafe;
    private Integer safetyScore;
    private String reason;
    private LocalDateTime checkedAt;
}
