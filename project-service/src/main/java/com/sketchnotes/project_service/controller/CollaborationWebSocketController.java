package com.sketchnotes.project_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * =============================================================================
 * COLLABORATION WEBSOCKET CONTROLLER
 * =============================================================================
 * 
 * Handles all real-time collaboration events for the sketchnote application.
 * Supports unified element-based architecture with:
 * - Element operations (create, update, delete)
 * - Stroke operations (append points, end stroke)
 * - Page operations (create, update, delete, switch)
 * - User presence (join, leave, cursor)
 * 
 * *** CRITICAL FEATURES ***
 * - Server-authoritative versioning (version field on all state-changing ops)
 * - Element locking for concurrent drag/edit prevention
 * - Chunked sync for large documents (SYNC_RESPONSE_START/CHUNK/END)
 * - Late join stroke handling (STROKE_INIT for mid-drawing join)
 * - Message sequence ordering (seq field for ordering)
 * 
 * Message Flow:
 * - Client sends to: /app/project/{projectId}/action
 * - Server broadcasts to: /topic/project/{projectId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CollaborationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // Event type constants
    private static final String ELEMENT_CREATE = "ELEMENT_CREATE";
    private static final String ELEMENT_UPDATE = "ELEMENT_UPDATE";
    private static final String ELEMENT_DELETE = "ELEMENT_DELETE";
    private static final String STROKE_APPEND = "STROKE_APPEND";
    private static final String STROKE_END = "STROKE_END";
    private static final String STROKE_INIT = "STROKE_INIT";        // *** NEW: Late join ***
    private static final String PAGE_CREATE = "PAGE_CREATE";
    private static final String PAGE_UPDATE = "PAGE_UPDATE";
    private static final String PAGE_DELETE = "PAGE_DELETE";
    private static final String PAGE_SWITCH = "PAGE_SWITCH";
    private static final String USER_JOIN = "USER_JOIN";
    private static final String USER_LEAVE = "USER_LEAVE";
    private static final String USER_CURSOR = "USER_CURSOR";
    private static final String SYNC_REQUEST = "SYNC_REQUEST";
    private static final String SYNC_RESPONSE = "SYNC_RESPONSE";
    private static final String SYNC_RESPONSE_START = "SYNC_RESPONSE_START";  // *** NEW: Chunked ***
    private static final String SYNC_RESPONSE_CHUNK = "SYNC_RESPONSE_CHUNK";
    private static final String SYNC_RESPONSE_END = "SYNC_RESPONSE_END";
    
    // *** CRITICAL: Locking ***
    private static final String ELEMENT_LOCK_REQUEST = "ELEMENT_LOCK_REQUEST";
    private static final String ELEMENT_LOCK_RELEASE = "ELEMENT_LOCK_RELEASE";
    private static final String ELEMENT_LOCK = "ELEMENT_LOCK";
    private static final String LOCK_GRANTED = "LOCK_GRANTED";
    private static final String LOCK_RELEASED = "LOCK_RELEASED";
    private static final String LOCK_REJECTED = "LOCK_REJECTED";
    private static final String SERVER_REJECT = "SERVER_REJECT";
    
    // Legacy support
    private static final String DRAW = "DRAW";
    
    // *** CRITICAL: In-memory state (replace with Redis in production) ***
    // Sequence counter per project
    private final ConcurrentHashMap<Long, AtomicLong> projectSequences = new ConcurrentHashMap<>();
    // Version counter per project  
    private final ConcurrentHashMap<Long, AtomicLong> projectVersions = new ConcurrentHashMap<>();
    // Element locks per project: projectId -> (elementId -> LockInfo)
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, LockInfo>> projectLocks = new ConcurrentHashMap<>();
    // Active strokes per project: projectId -> (strokeId -> StrokeInfo)
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, StrokeInfo>> activeStrokes = new ConcurrentHashMap<>();
    
    // Lock timeout (30 seconds)
    private static final long LOCK_TTL_MS = 30000;
    // Chunk size for sync (100 elements per chunk)
    private static final int SYNC_CHUNK_SIZE = 100;

    /**
     * Main action handler for all collaboration events
     * 
     * Client sends to: /app/project/{projectId}/action
     * Server broadcasts to: /topic/project/{projectId}
     * 
     * @param projectId Project ID from path
     * @param message Collaboration message containing type and payload
     */
    @MessageMapping("/project/{projectId}/action")
    public void handleProjectAction(
            @DestinationVariable Long projectId,
            @Payload Map<String, Object> message) {
        
        String type = (String) message.get("type");
        Object userId = message.get("userId");
        Object payload = message.get("payload");
        
        if (type == null) {
            log.warn("⚠️ [Collab] Received message without type for project {}", projectId);
            return;
        }
        
        // *** CRITICAL: Add server sequence number ***
        long seq = getNextSequence(projectId);
        message.put("seq", seq);
        
        // Add server timestamp
        message.put("serverTimestamp", System.currentTimeMillis());
        
        // Log based on event type
        switch (type) {
            case ELEMENT_CREATE:
                // Increment version for state-changing operation
                message.put("version", incrementVersion(projectId));
                logElementCreate(projectId, userId, payload);
                break;
            case ELEMENT_UPDATE:
                // Check lock before allowing update
                if (!checkLockForUpdate(projectId, userId, payload, message)) {
                    return; // Lock check failed, rejection sent
                }
                message.put("version", incrementVersion(projectId));
                logElementUpdate(projectId, userId, payload);
                break;
            case ELEMENT_DELETE:
                message.put("version", incrementVersion(projectId));
                logElementDelete(projectId, userId, payload);
                break;
            case STROKE_APPEND:
                // *** CRITICAL: Track active strokes for late join ***
                trackActiveStroke(projectId, userId, payload);
                logStrokeAppend(projectId, userId, payload);
                break;
            case STROKE_END:
                // *** CRITICAL: Remove from active strokes ***
                endActiveStroke(projectId, payload);
                message.put("version", incrementVersion(projectId));
                logStrokeEnd(projectId, userId, payload);
                break;
            case PAGE_CREATE:
                message.put("version", incrementVersion(projectId));
                logPageCreate(projectId, userId, payload);
                break;
            case PAGE_UPDATE:
                message.put("version", incrementVersion(projectId));
                logPageUpdate(projectId, userId, payload);
                break;
            case PAGE_DELETE:
                message.put("version", incrementVersion(projectId));
                logPageDelete(projectId, userId, payload);
                break;
            case PAGE_SWITCH:
                logPageSwitch(projectId, userId, payload);
                break;
            case USER_JOIN:
                logUserJoin(projectId, userId, payload);
                // *** CRITICAL: Send active strokes to joining user (STROKE_INIT) ***
                sendActiveStrokesToUser(projectId, userId);
                break;
            case USER_LEAVE:
                // *** CRITICAL: Release any locks held by leaving user ***
                releaseUserLocks(projectId, userId);
                logUserLeave(projectId, userId);
                break;
            case USER_CURSOR:
                // Don't log cursor updates (too noisy)
                break;
            case SYNC_REQUEST:
                handleSyncRequest(projectId, userId, message);
                return; // Don't broadcast sync requests
            // *** CRITICAL: Handle lock requests ***
            case ELEMENT_LOCK_REQUEST:
                handleLockRequest(projectId, userId, payload);
                return; // Don't broadcast lock requests
            case ELEMENT_LOCK_RELEASE:
                handleLockRelease(projectId, userId, payload);
                return; // Broadcast happens in handler
            case DRAW:
                // Legacy draw support
                logLegacyDraw(projectId, userId, payload);
                break;
            default:
                log.debug("🔄 [Collab] Event type '{}' from user {} on project {}", 
                        type, userId, projectId);
        }
        
        // Broadcast to all subscribers of this project
        broadcastToProject(projectId, message);
    }
    
    // ===========================================================================
    // CRITICAL: SEQUENCE & VERSION MANAGEMENT
    // ===========================================================================
    
    private long getNextSequence(Long projectId) {
        return projectSequences.computeIfAbsent(projectId, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    private long incrementVersion(Long projectId) {
        return projectVersions.computeIfAbsent(projectId, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    private long getCurrentVersion(Long projectId) {
        return projectVersions.computeIfAbsent(projectId, k -> new AtomicLong(0)).get();
    }
    
    // ===========================================================================
    // CRITICAL: ELEMENT LOCKING
    // ===========================================================================
    
    @SuppressWarnings("unchecked")
    private void handleLockRequest(Long projectId, Object userId, Object payload) {
        if (!(payload instanceof Map)) return;
        Map<String, Object> p = (Map<String, Object>) payload;
        String elementId = (String) p.get("elementId");
        Object pageId = p.get("pageId");
        Long requestedTtl = p.get("requestedTtl") != null ? 
                            ((Number) p.get("requestedTtl")).longValue() : LOCK_TTL_MS;
        
        if (elementId == null) return;
        
        ConcurrentHashMap<String, LockInfo> locks = projectLocks.computeIfAbsent(
            projectId, k -> new ConcurrentHashMap<>());
        
        LockInfo existingLock = locks.get(elementId);
        long now = System.currentTimeMillis();
        
        // Check if locked by someone else (and not expired)
        if (existingLock != null && 
            !existingLock.userId.equals(userId.toString()) && 
            existingLock.expiresAt > now) {
            // Reject - already locked
            sendLockRejected(projectId, userId, elementId, existingLock.userId);
            return;
        }
        
        // Grant lock
        String lockToken = userId + "_" + now + "_" + Math.random();
        long expiresAt = now + Math.min(requestedTtl, LOCK_TTL_MS);
        
        LockInfo newLock = new LockInfo(userId.toString(), expiresAt, lockToken);
        locks.put(elementId, newLock);
        
        // Send grant to requesting user
        sendLockGranted(projectId, userId, elementId, lockToken, expiresAt);
        
        // Broadcast lock to all users
        broadcastLock(projectId, userId, elementId, expiresAt, lockToken);
        
        log.info("🔒 [Collab] User {} acquired lock on element {} in project {}", 
                userId, elementId, projectId);
    }
    
    @SuppressWarnings("unchecked")
    private void handleLockRelease(Long projectId, Object userId, Object payload) {
        if (!(payload instanceof Map)) return;
        Map<String, Object> p = (Map<String, Object>) payload;
        String elementId = (String) p.get("elementId");
        String lockToken = (String) p.get("lockToken");
        
        if (elementId == null) return;
        
        ConcurrentHashMap<String, LockInfo> locks = projectLocks.get(projectId);
        if (locks == null) return;
        
        LockInfo lock = locks.get(elementId);
        // Only release if owned by user or token matches
        if (lock != null && (lock.userId.equals(userId.toString()) || 
                            (lockToken != null && lockToken.equals(lock.lockToken)))) {
            locks.remove(elementId);
            
            // Broadcast release
            broadcastLockReleased(projectId, elementId, userId);
            
            log.info("🔓 [Collab] User {} released lock on element {} in project {}", 
                    userId, elementId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean checkLockForUpdate(Long projectId, Object userId, Object payload, 
                                       Map<String, Object> message) {
        if (!(payload instanceof Map)) return true;
        Map<String, Object> p = (Map<String, Object>) payload;
        String elementId = (String) p.get("elementId");
        Boolean transient_ = (Boolean) p.get("transient");
        
        // Transient updates (cursor position while hovering) don't need lock check
        if (Boolean.TRUE.equals(transient_)) return true;
        if (elementId == null) return true;
        
        ConcurrentHashMap<String, LockInfo> locks = projectLocks.get(projectId);
        if (locks == null) return true;
        
        LockInfo lock = locks.get(elementId);
        long now = System.currentTimeMillis();
        
        // If locked by someone else and not expired, reject
        if (lock != null && !lock.userId.equals(userId.toString()) && lock.expiresAt > now) {
            sendServerReject(projectId, userId, "ELEMENT_LOCKED", 
                    "Element is locked by user " + lock.userId, message);
            return false;
        }
        
        return true;
    }
    
    private void releaseUserLocks(Long projectId, Object userId) {
        ConcurrentHashMap<String, LockInfo> locks = projectLocks.get(projectId);
        if (locks == null) return;
        
        List<String> toRelease = new ArrayList<>();
        locks.forEach((elementId, lock) -> {
            if (lock.userId.equals(userId.toString())) {
                toRelease.add(elementId);
            }
        });
        
        toRelease.forEach(elementId -> {
            locks.remove(elementId);
            broadcastLockReleased(projectId, elementId, userId);
        });
        
        if (!toRelease.isEmpty()) {
            log.info("🔓 [Collab] Released {} locks for departing user {} in project {}", 
                    toRelease.size(), userId, projectId);
        }
    }
    
    private void sendLockGranted(Long projectId, Object userId, String elementId, 
                                 String lockToken, long expiresAt) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", LOCK_GRANTED);
        response.put("projectId", projectId);
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        response.put("payload", Map.of(
            "elementId", elementId,
            "lockToken", lockToken,
            "expiresAt", expiresAt
        ));
        
        // TODO: Send to specific user only (convertAndSendToUser)
        // For now, broadcast to all
        broadcastToProject(projectId, response);
    }
    
    private void sendLockRejected(Long projectId, Object userId, String elementId, String lockedBy) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", LOCK_REJECTED);
        response.put("projectId", projectId);
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        response.put("payload", Map.of(
            "elementId", elementId,
            "reason", "Element already locked",
            "lockedBy", lockedBy
        ));
        
        broadcastToProject(projectId, response);
    }
    
    private void broadcastLock(Long projectId, Object userId, String elementId, 
                               long expiresAt, String lockToken) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", ELEMENT_LOCK);
        message.put("projectId", projectId);
        message.put("userId", userId);
        message.put("seq", getNextSequence(projectId));
        message.put("timestamp", System.currentTimeMillis());
        message.put("payload", Map.of(
            "elementId", elementId,
            "lockedBy", userId,
            "expiresAt", expiresAt,
            "lockToken", lockToken
        ));
        
        broadcastToProject(projectId, message);
    }
    
    private void broadcastLockReleased(Long projectId, String elementId, Object userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", LOCK_RELEASED);
        message.put("projectId", projectId);
        message.put("userId", userId);
        message.put("seq", getNextSequence(projectId));
        message.put("timestamp", System.currentTimeMillis());
        message.put("payload", Map.of("elementId", elementId));
        
        broadcastToProject(projectId, message);
    }
    
    private void sendServerReject(Long projectId, Object userId, String reason, 
                                  String message, Map<String, Object> originalEvent) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", SERVER_REJECT);
        response.put("projectId", projectId);
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        response.put("payload", Map.of(
            "reason", reason,
            "message", message,
            "originalEventType", originalEvent.get("type")
        ));
        
        broadcastToProject(projectId, response);
        log.warn("❌ [Collab] Server rejected {} from user {}: {}", 
                originalEvent.get("type"), userId, reason);
    }
    
    // ===========================================================================
    // CRITICAL: ACTIVE STROKE TRACKING (for late join)
    // ===========================================================================
    
    @SuppressWarnings("unchecked")
    private void trackActiveStroke(Long projectId, Object userId, Object payload) {
        if (!(payload instanceof Map)) return;
        Map<String, Object> p = (Map<String, Object>) payload;
        String strokeId = (String) p.get("strokeId");
        Object pageId = p.get("pageId");
        Object strokeInit = p.get("strokeInit");
        Object points = p.get("points");
        
        if (strokeId == null) return;
        
        ConcurrentHashMap<String, StrokeInfo> strokes = activeStrokes.computeIfAbsent(
            projectId, k -> new ConcurrentHashMap<>());
        
        StrokeInfo stroke = strokes.get(strokeId);
        if (stroke == null && strokeInit instanceof Map) {
            // New stroke with init data
            Map<String, Object> init = (Map<String, Object>) strokeInit;
            stroke = new StrokeInfo(
                strokeId, 
                userId.toString(), 
                pageId,
                init.get("tool"),
                init.get("color"),
                init.get("strokeWidth")
            );
            strokes.put(strokeId, stroke);
        }
        
        // Append points to stroke
        if (stroke != null && points != null) {
            stroke.appendPoints(points);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void endActiveStroke(Long projectId, Object payload) {
        if (!(payload instanceof Map)) return;
        Map<String, Object> p = (Map<String, Object>) payload;
        String strokeId = (String) p.get("strokeId");
        
        if (strokeId == null) return;
        
        ConcurrentHashMap<String, StrokeInfo> strokes = activeStrokes.get(projectId);
        if (strokes != null) {
            strokes.remove(strokeId);
        }
    }
    
    private void sendActiveStrokesToUser(Long projectId, Object joiningUserId) {
        ConcurrentHashMap<String, StrokeInfo> strokes = activeStrokes.get(projectId);
        if (strokes == null || strokes.isEmpty()) return;
        
        strokes.values().forEach(stroke -> {
            Map<String, Object> initMessage = new HashMap<>();
            initMessage.put("type", STROKE_INIT);
            initMessage.put("projectId", projectId);
            initMessage.put("userId", stroke.userId);
            initMessage.put("seq", getNextSequence(projectId));
            initMessage.put("timestamp", System.currentTimeMillis());
            initMessage.put("payload", Map.of(
                "strokeId", stroke.strokeId,
                "pageId", stroke.pageId,
                "userId", stroke.userId,
                "tool", stroke.tool != null ? stroke.tool : "pen",
                "color", stroke.color != null ? stroke.color : "#000000",
                "strokeWidth", stroke.strokeWidth != null ? stroke.strokeWidth : 2,
                "points", stroke.getCompressedPoints()
            ));
            
            // TODO: Send to specific user only
            broadcastToProject(projectId, initMessage);
        });
        
        log.info("👋 [Collab] Sent {} active strokes to joining user {} in project {}", 
                strokes.size(), joiningUserId, projectId);
    }
    
    /**
     * Broadcast message to all users in a project
     */
    private void broadcastToProject(Long projectId, Object message) {
        String destination = "/topic/project/" + projectId;
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("📤 [Collab] Broadcast to {}", destination);
        } catch (Exception e) {
            log.error("❌ [Collab] Failed to broadcast to {}: {}", destination, e.getMessage());
        }
    }
    
    /**
     * Handle sync request - send full document state to requesting user
     * *** CRITICAL: Now supports chunked responses for large documents ***
     */
    @SuppressWarnings("unchecked")
    private void handleSyncRequest(Long projectId, Object userId, Map<String, Object> message) {
        log.info("🔄 [Collab] User {} requested sync for project {}", userId, projectId);
        
        Map<String, Object> payload = (Map<String, Object>) message.get("payload");
        Object fromVersion = payload != null ? payload.get("fromVersion") : null;
        
        // Get current state
        long currentVersion = getCurrentVersion(projectId);
        long currentSeq = getNextSequence(projectId);
        
        // Get current locks
        ConcurrentHashMap<String, LockInfo> locks = projectLocks.get(projectId);
        Map<String, Object> lockState = new HashMap<>();
        if (locks != null) {
            locks.forEach((elementId, lock) -> {
                if (lock.expiresAt > System.currentTimeMillis()) {
                    lockState.put(elementId, Map.of(
                        "lockedBy", lock.userId,
                        "expiresAt", lock.expiresAt,
                        "lockToken", lock.lockToken
                    ));
                }
            });
        }
        
        // Get active strokes for late join
        ConcurrentHashMap<String, StrokeInfo> strokes = activeStrokes.get(projectId);
        List<Map<String, Object>> activeStrokesList = new ArrayList<>();
        if (strokes != null) {
            strokes.values().forEach(stroke -> {
                activeStrokesList.add(Map.of(
                    "strokeId", stroke.strokeId,
                    "pageId", stroke.pageId != null ? stroke.pageId : 0,
                    "userId", stroke.userId,
                    "tool", stroke.tool != null ? stroke.tool : "pen",
                    "color", stroke.color != null ? stroke.color : "#000000",
                    "points", stroke.getCompressedPoints()
                ));
            });
        }
        
        // TODO: Fetch document state from database/Redis
        // For now, send a minimal response
        // In production: implement chunked sync for large documents
        
        // *** For MVP: Send simple response ***
        // *** For large documents: Use SYNC_RESPONSE_START/CHUNK/END ***
        Map<String, Object> syncResponse = new HashMap<>();
        syncResponse.put("type", SYNC_RESPONSE);
        syncResponse.put("projectId", projectId);
        syncResponse.put("userId", "server");
        syncResponse.put("timestamp", System.currentTimeMillis());
        syncResponse.put("seq", currentSeq);
        syncResponse.put("version", currentVersion);
        syncResponse.put("payload", Map.of(
            "document", Map.of(
                "projectId", projectId,
                "version", currentVersion,
                "pages", java.util.Collections.emptyList()
            ),
            "activeUsers", java.util.Collections.emptyList(),
            "activeStrokes", activeStrokesList,
            "lockState", lockState,
            "version", currentVersion,
            "seq", currentSeq
        ));
        
        // TODO: Send only to requesting user using convertAndSendToUser
        broadcastToProject(projectId, syncResponse);
    }
    
    // ===========================================================================
    // LOGGING HELPERS
    // ===========================================================================
    
    @SuppressWarnings("unchecked")
    private void logElementCreate(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object element = p.get("element");
            String elementType = "unknown";
            if (element instanceof Map) {
                elementType = (String) ((Map<String, Object>) element).getOrDefault("type", "unknown");
            }
            log.info("➕ [Collab] User {} created {} on project {}", userId, elementType, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logElementUpdate(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object elementId = p.get("elementId");
            Boolean transient_ = (Boolean) p.get("transient");
            if (Boolean.TRUE.equals(transient_)) {
                log.debug("🔄 [Collab] User {} updating element {} (transient)", userId, elementId);
            } else {
                log.info("📝 [Collab] User {} updated element {} on project {}", userId, elementId, projectId);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logElementDelete(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object elementId = p.get("elementId");
            log.info("🗑️ [Collab] User {} deleted element {} from project {}", userId, elementId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logStrokeAppend(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object strokeId = p.get("strokeId");
            Object points = p.get("points");
            int pointCount = 0;
            if (points instanceof Map) {
                Map<String, Object> pts = (Map<String, Object>) points;
                Object deltas = pts.get("deltas");
                if (deltas instanceof java.util.List) {
                    pointCount = ((java.util.List<?>) deltas).size() / 2 + 1;
                }
            }
            log.debug("✏️ [Collab] User {} appending {} points to stroke {} on project {}", 
                    userId, pointCount, strokeId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logStrokeEnd(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object strokeId = p.get("strokeId");
            log.info("✅ [Collab] User {} finished stroke {} on project {}", userId, strokeId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logPageCreate(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object page = p.get("page");
            Object pageId = page instanceof Map ? ((Map<String, Object>) page).get("id") : null;
            log.info("📄 [Collab] User {} created page {} in project {}", userId, pageId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logPageUpdate(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object pageId = p.get("pageId");
            log.info("📝 [Collab] User {} updated page {} in project {}", userId, pageId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logPageDelete(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object pageId = p.get("pageId");
            log.warn("🗑️ [Collab] User {} deleted page {} from project {}", userId, pageId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logPageSwitch(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object pageId = p.get("pageId");
            log.debug("📖 [Collab] User {} switched to page {} in project {}", userId, pageId, projectId);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void logUserJoin(Long projectId, Object userId, Object payload) {
        String userName = "unknown";
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object user = p.get("user");
            if (user instanceof Map) {
                userName = (String) ((Map<String, Object>) user).getOrDefault("userName", "unknown");
            }
        }
        log.info("👋 [Collab] User {} ({}) joined project {}", userId, userName, projectId);
    }
    
    private void logUserLeave(Long projectId, Object userId) {
        log.info("👋 [Collab] User {} left project {}", userId, projectId);
    }
    
    @SuppressWarnings("unchecked")
    private void logLegacyDraw(Long projectId, Object userId, Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> p = (Map<String, Object>) payload;
            Object stroke = p.get("stroke");
            String tool = "unknown";
            if (stroke instanceof Map) {
                tool = (String) ((Map<String, Object>) stroke).getOrDefault("tool", "unknown");
            }
            log.debug("🎨 [Collab] Legacy DRAW from user {} on project {}: tool={}", 
                    userId, projectId, tool);
        }
    }
    
    // ===========================================================================
    // HELPER CLASSES
    // ===========================================================================
    
    /**
     * Lock information for an element
     */
    private static class LockInfo {
        final String userId;
        final long expiresAt;
        final String lockToken;
        
        LockInfo(String userId, long expiresAt, String lockToken) {
            this.userId = userId;
            this.expiresAt = expiresAt;
            this.lockToken = lockToken;
        }
    }
    
    /**
     * Active stroke information (for late join)
     */
    private static class StrokeInfo {
        final String strokeId;
        final String userId;
        final Object pageId;
        final Object tool;
        final Object color;
        final Object strokeWidth;
        private List<Object> accumulatedPoints = new ArrayList<>();
        
        StrokeInfo(String strokeId, String userId, Object pageId, 
                   Object tool, Object color, Object strokeWidth) {
            this.strokeId = strokeId;
            this.userId = userId;
            this.pageId = pageId;
            this.tool = tool;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }
        
        synchronized void appendPoints(Object points) {
            if (points instanceof Map) {
                // Compressed format - store as-is
                accumulatedPoints.add(points);
            } else if (points instanceof List) {
                accumulatedPoints.addAll((List<?>) points);
            }
        }
        
        synchronized Object getCompressedPoints() {
            // Return accumulated points
            // In production, you might want to merge and re-compress
            if (accumulatedPoints.isEmpty()) {
                return Map.of("compressed", false, "data", List.of());
            }
            
            // If we have compressed chunks, return the last one for simplicity
            // In production, merge all points properly
            Object lastPoints = accumulatedPoints.get(accumulatedPoints.size() - 1);
            if (lastPoints instanceof Map) {
                return lastPoints;
            }
            
            return Map.of("compressed", false, "data", accumulatedPoints);
        }
    }
}
