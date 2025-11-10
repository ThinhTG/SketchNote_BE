package com.sketchnotes.learning.dto;

import lombok.Data;

@Data
public class UpdateProgressRequest {
    private Integer lastPosition;  // Vị trí cuối cùng xem đến (giây)
    private Integer timeSpent;     // Thời gian đã xem (giây)
    private boolean completed;     // Đã hoàn thành bài học chưa
}