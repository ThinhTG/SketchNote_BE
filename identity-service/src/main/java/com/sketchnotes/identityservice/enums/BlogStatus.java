package com.sketchnotes.identityservice.enums;

public enum BlogStatus {
    DRAFT,           // Blog is being drafted
    PENDING_REVIEW,  // Blog is pending moderation (after 15 minutes of posting)
    PUBLISHED,
    AI_REJECTED,     // Blog was rejected by AI due to content violations
    REJECTED,        // Blog was rejected due to content violations
    ARCHIVED         // Blog has been archived
}
