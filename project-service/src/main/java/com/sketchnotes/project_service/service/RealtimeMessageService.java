package com.sketchnotes.project_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * =============================================================================
 * REALTIME MESSAGE SERVICE
 * =============================================================================
 * 
 * Handles async message broadcasting with:
 * - Per-project message queues
 * - Rate limiting per user
 * - Sequence number assignment
 * - Non-blocking async broadcast
 * 
 * This service decouples message receipt from broadcast, preventing
 * WebSocket blocking and enabling rate limiting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    
    // ==========================================================================
    // CONFIGURATION
    // ==========================================================================
    
    private static final int MAX_MESSAGES_PER_USER_PER_SECOND = 100;
    private static final int MAX_MESSAGES_PER_PROJECT_PER_SECOND = 500;
    private static final int QUEUE_CAPACITY = 1000;
    private static final int QUEUE_PROCESS_INTERVAL_MS = 10;
    private static final int STALE_MESSAGE_MS = 5000;
    private static final int BROADCAST_THREAD_POOL_SIZE = 4;
    
    // ==========================================================================
    // STATE
    // ==========================================================================
    
    // Sequence counter per project
    private final ConcurrentHashMap<Long, AtomicLong> projectSequences = new ConcurrentHashMap<>();
    
    // Version counter per project
    private final ConcurrentHashMap<Long, AtomicLong> projectVersions = new ConcurrentHashMap<>();
    
    // Rate limiters per user per project: "projectId_userId" -> RateLimiter
    private final ConcurrentHashMap<String, TokenBucketRateLimiter> userRateLimiters = new ConcurrentHashMap<>();
    
    // Rate limiters per project
    private final ConcurrentHashMap<Long, TokenBucketRateLimiter> projectRateLimiters = new ConcurrentHashMap<>();
    
    // Message queues per project
    private final ConcurrentHashMap<Long, LinkedBlockingQueue<QueuedMessage>> projectQueues = new ConcurrentHashMap<>();
    
    // Executor for async broadcasts
    private ExecutorService broadcastExecutor;
    
    // Scheduled executor for queue processing
    private ScheduledExecutorService queueProcessor;
    
    // Shutdown flag
    private volatile boolean isShutdown = false;
    
    // ==========================================================================
    // LIFECYCLE
    // ==========================================================================
    
    @PostConstruct
    public void init() {
        log.info("[RealtimeService] Initializing with {} broadcast threads", BROADCAST_THREAD_POOL_SIZE);
        
        broadcastExecutor = Executors.newFixedThreadPool(
            BROADCAST_THREAD_POOL_SIZE,
            r -> {
                Thread t = new Thread(r, "ws-broadcast");
                t.setDaemon(true);
                return t;
            }
        );
        
        queueProcessor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "queue-processor");
            t.setDaemon(true);
            return t;
        });
        
        // Start cleanup task
        queueProcessor.scheduleAtFixedRate(
            this::cleanupStaleData,
            60,
            60,
            TimeUnit.SECONDS
        );
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("[RealtimeService] Shutting down");
        isShutdown = true;
        
        if (queueProcessor != null) {
            queueProcessor.shutdownNow();
        }
        
        if (broadcastExecutor != null) {
            broadcastExecutor.shutdownNow();
        }
        
        projectQueues.clear();
        userRateLimiters.clear();
        projectRateLimiters.clear();
    }
    
    // ==========================================================================
    // PUBLIC API
    // ==========================================================================
    
    /**
     * Enqueue a message for async broadcast with rate limiting
     * 
     * @param projectId Project to broadcast to
     * @param userId User sending the message
     * @param message Message content (will be modified with seq)
     * @return true if message was queued, false if rate limited or queue full
     */
    public boolean enqueueMessage(Long projectId, Object userId, Map<String, Object> message) {
        if (isShutdown) {
            log.warn("[RealtimeService] Service is shutdown, rejecting message");
            return false;
        }
        
        if (projectId == null || userId == null || message == null) {
            log.warn("[RealtimeService] Invalid parameters: projectId={}, userId={}", projectId, userId);
            return false;
        }
        
        String userIdStr = String.valueOf(userId);
        
        // Check user rate limit
        String userRateLimitKey = projectId + "_" + userIdStr;
        TokenBucketRateLimiter userLimiter = userRateLimiters.computeIfAbsent(
            userRateLimitKey,
            k -> new TokenBucketRateLimiter(MAX_MESSAGES_PER_USER_PER_SECOND)
        );
        
        if (!userLimiter.tryAcquire()) {
            log.warn("[RealtimeService] Rate limit exceeded for user {} on project {}", userId, projectId);
            return false;
        }
        
        // Check project rate limit
        TokenBucketRateLimiter projectLimiter = projectRateLimiters.computeIfAbsent(
            projectId,
            k -> new TokenBucketRateLimiter(MAX_MESSAGES_PER_PROJECT_PER_SECOND)
        );
        
        if (!projectLimiter.tryAcquire()) {
            log.warn("[RealtimeService] Rate limit exceeded for project {}", projectId);
            return false;
        }
        
        // Assign sequence number
        long seq = getNextSequence(projectId);
        message.put("seq", seq);
        message.put("serverTimestamp", System.currentTimeMillis());
        
        // Get or create queue for project
        LinkedBlockingQueue<QueuedMessage> queue = projectQueues.computeIfAbsent(
            projectId,
            k -> {
                LinkedBlockingQueue<QueuedMessage> q = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
                startQueueProcessor(projectId, q);
                return q;
            }
        );
        
        // Enqueue (non-blocking)
        QueuedMessage qm = new QueuedMessage(message, System.currentTimeMillis());
        if (!queue.offer(qm)) {
            log.warn("[RealtimeService] Queue full for project {}, dropping message", projectId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Broadcast a message immediately (bypass queue)
     * Use for critical messages like sync responses
     */
    public void broadcastImmediate(Long projectId, Object message, String topicSuffix) {
        if (isShutdown) return;
        
        String destination = "/topic/project/" + projectId + "/" + topicSuffix;
        
        broadcastExecutor.submit(() -> {
            try {
                messagingTemplate.convertAndSend(destination, message);
            } catch (Exception e) {
                log.error("[RealtimeService] Failed to broadcast to {}", destination, e);
            }
        });
    }
    
    /**
     * Get next sequence number for a project
     */
    public long getNextSequence(Long projectId) {
        return projectSequences.computeIfAbsent(projectId, k -> new AtomicLong(0))
                               .incrementAndGet();
    }
    
    /**
     * Get current sequence number for a project (for sync responses)
     */
    public long getCurrentSequence(Long projectId) {
        return projectSequences.computeIfAbsent(projectId, k -> new AtomicLong(0)).get();
    }
    
    /**
     * Increment and get version for a project
     */
    public long incrementVersion(Long projectId) {
        return projectVersions.computeIfAbsent(projectId, k -> new AtomicLong(0))
                              .incrementAndGet();
    }
    
    /**
     * Get current version for a project
     */
    public long getCurrentVersion(Long projectId) {
        return projectVersions.computeIfAbsent(projectId, k -> new AtomicLong(0)).get();
    }
    
    // ==========================================================================
    // QUEUE PROCESSING
    // ==========================================================================
    
    private void startQueueProcessor(Long projectId, LinkedBlockingQueue<QueuedMessage> queue) {
        queueProcessor.scheduleAtFixedRate(() -> {
            if (isShutdown) return;
            
            try {
                processQueue(projectId, queue);
            } catch (Exception e) {
                log.error("[RealtimeService] Error processing queue for project {}", projectId, e);
            }
        }, 0, QUEUE_PROCESS_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    private void processQueue(Long projectId, LinkedBlockingQueue<QueuedMessage> queue) {
        if (queue.isEmpty()) return;
        
        // Batch process up to 20 messages
        int batchSize = Math.min(20, queue.size());
        List<QueuedMessage> batch = new ArrayList<>(batchSize);
        
        queue.drainTo(batch, batchSize);
        
        if (batch.isEmpty()) return;
        
        // Default destination for collaboration
        String defaultDestination = "/topic/project/" + projectId + "/collaboration";
        long now = System.currentTimeMillis();
        
        for (QueuedMessage qm : batch) {
            // Skip stale messages
            if (now - qm.timestamp > STALE_MESSAGE_MS) {
                log.warn("[RealtimeService] Dropping stale message for project {}", projectId);
                continue;
            }
            
            // Get destination from message or use default
            String destination = defaultDestination;
            if (qm.message instanceof Map) {
                Object destObj = ((Map<?, ?>) qm.message).get("_destination");
                if (destObj instanceof String) {
                    destination = (String) destObj;
                    // Remove internal field before sending
                    ((Map<?, ?>) qm.message).remove("_destination");
                }
            }
            
            final String finalDest = destination;
            
            // Async broadcast
            broadcastExecutor.submit(() -> {
                try {
                    messagingTemplate.convertAndSend(finalDest, qm.message);
                } catch (Exception e) {
                    log.error("[RealtimeService] Failed to broadcast to {}", finalDest, e);
                }
            });
        }
    }
    
    // ==========================================================================
    // CLEANUP
    // ==========================================================================
    
    private void cleanupStaleData() {
        try {
            // Cleanup empty queues
            projectQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            
            // Cleanup old rate limiters (inactive for > 5 minutes)
            // Note: In production, use a more sophisticated eviction strategy
            
            log.debug("[RealtimeService] Cleanup complete. Active projects: {}", projectQueues.size());
        } catch (Exception e) {
            log.error("[RealtimeService] Error during cleanup", e);
        }
    }
    
    // ==========================================================================
    // INNER CLASSES
    // ==========================================================================
    
    /**
     * Queued message with timestamp for staleness check
     */
    private static class QueuedMessage {
        final Map<String, Object> message;
        final long timestamp;
        
        QueuedMessage(Map<String, Object> message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Token bucket rate limiter
     * Allows burst up to bucket size, then limits to rate per second
     */
    private static class TokenBucketRateLimiter {
        private final int maxTokens;
        private final long refillIntervalNanos;
        private long availableTokens;
        private long lastRefillTime;
        
        TokenBucketRateLimiter(int tokensPerSecond) {
            this.maxTokens = tokensPerSecond;
            this.refillIntervalNanos = 1_000_000_000L / tokensPerSecond; // Nano per token
            this.availableTokens = tokensPerSecond;
            this.lastRefillTime = System.nanoTime();
        }
        
        synchronized boolean tryAcquire() {
            refill();
            
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            
            return false;
        }
        
        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillTime;
            
            if (elapsed > refillIntervalNanos) {
                long tokensToAdd = elapsed / refillIntervalNanos;
                availableTokens = Math.min(maxTokens, availableTokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
