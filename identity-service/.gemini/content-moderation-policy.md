# Content Moderation Policy - Updated

## 📋 Tổng quan

Hệ thống kiểm duyệt nội dung blog sử dụng **2 lớp AI**:
1. **Google Vision API SafeSearch** - Kiểm tra hình ảnh
2. **Gemini AI** - Phân tích tổng hợp (text + image reports)

## 🎯 Image Safety Policy (Google Vision API)

### Likelihood Levels

| Level | Ý nghĩa | Kết quả |
|-------|---------|---------|
| **VERY_UNLIKELY** | Rất không có khả năng vi phạm | ✅ **PASS** |
| **UNLIKELY** | Không có khả năng vi phạm | ✅ **PASS** |
| **POSSIBLE** | Có thể vi phạm | ❌ **UNSAFE** → Staff review |
| **LIKELY** | Có khả năng vi phạm | ❌ **UNSAFE** → Staff review |
| **VERY_LIKELY** | Rất có khả năng vi phạm | ❌ **UNSAFE** → Staff review |

### Categories Checked

1. **Adult**: Nội dung người lớn, pornography
2. **Violence**: Bạo lực, gore, terrorism
3. **Racy**: Nội dung gợi cảm, nhạy cảm
4. **Medical**: Nội dung y tế nhạy cảm

### Decision Logic

```java
// Ảnh được coi là SAFE khi TẤT CẢ categories đều VERY_UNLIKELY hoặc UNLIKELY
boolean isSafe = !isLikely(adult) &&      
                 !isLikely(violence) &&   
                 !isLikely(racy) &&       
                 !isLikely(medical);

// isLikely() returns true nếu level >= POSSIBLE
private boolean isLikely(Likelihood likelihood) {
    return likelihood == Likelihood.POSSIBLE || 
           likelihood == Likelihood.LIKELY || 
           likelihood == Likelihood.VERY_LIKELY;
}
```

### Examples

#### ✅ SAFE Image
```
Cover Image: SAFE
- Adult: VERY_UNLIKELY
- Violence: VERY_UNLIKELY
- Racy: UNLIKELY
- Medical: VERY_UNLIKELY
→ Result: PASS
```

#### ❌ UNSAFE Image (Requires Staff Review)
```
Cover Image: WARNING DETECTED [Adult: POSSIBLE, Violence: UNLIKELY, Racy: LIKELY, Medical: UNLIKELY]
→ Result: UNSAFE (có POSSIBLE và LIKELY)
→ Action: Blog status = AI_REJECTED, cần staff duyệt lại
```

## 🤖 Gemini AI Moderation Policy

### Image Analysis Rules

Gemini AI nhận được báo cáo từ Vision API và áp dụng **STRICT RULES**:

1. ✅ Nếu image report = **"SAFE"** → Image passed
2. ❌ Nếu image report = **"WARNING DETECTED"** với bất kỳ category nào ở mức **POSSIBLE/LIKELY/VERY_LIKELY**:
   - **MUST** flag as violation
   - Set `isSafe = false`
   - Add to `violations` array
   - Deduct points from `safetyScore`

3. **Even ONE category at POSSIBLE or higher = VIOLATION**

### Text Content Violations

Gemini AI cũng kiểm tra text cho các vi phạm:

1. Profanity, offensive language, vulgar words
2. Adult content, pornography, sexual content (18+)
3. Illegal drugs, controlled substances
4. Violence, gore, terrorism, threats
5. Fraud, scams, phishing attempts
6. Spam, excessive advertising
7. Hate speech, discrimination, harassment
8. Dangerous misinformation (health, safety)
9. Personal attacks, doxxing
10. Copyright infringement claims

### Safety Score Calculation

```
Base Score: 100 (completely safe)

Deductions:
- POSSIBLE: -20 to -30 points per violation
- LIKELY: -40 to -50 points per violation
- VERY_LIKELY: -60 to -80 points per violation

Final Score Range: 0-100
- 80-100: Safe
- 50-79: Moderate risk
- 0-49: High risk
```

### Analysis Approach

```
Step 1: Check ALL image reports
  ↓
Step 2: If ANY image has POSSIBLE/LIKELY/VERY_LIKELY
  → Mark as unsafe immediately
  ↓
Step 3: Analyze text content for violations
  ↓
Step 4: Combine image + text results
  ↓
Step 5: Generate final decision
```

## 📊 Workflow

### Blog Creation Flow

```
1. User creates blog with images
   ↓
2. Blog status = PENDING_REVIEW
   ↓
3. Wait 15 minutes (scheduled task)
   ↓
4. ContentModerationService.moderatePendingBlogs()
   ↓
5. For each image:
   - Call Vision API SafeSearch
   - Get likelihood levels
   - Determine SAFE or WARNING
   ↓
6. Build content with image reports
   ↓
7. Send to Gemini AI
   ↓
8. Gemini analyzes:
   - Image reports (STRICT policy)
   - Text content
   ↓
9. Decision:
   - isSafe = true → Blog status = PUBLISHED
   - isSafe = false → Blog status = AI_REJECTED
   ↓
10. If AI_REJECTED:
    - Save to BlogModerationHistory
    - Notify staff for manual review
```

## 🎯 Decision Matrix

| Image Status | Text Status | Final Decision | Blog Status |
|--------------|-------------|----------------|-------------|
| All VERY_UNLIKELY/UNLIKELY | Clean | ✅ SAFE | PUBLISHED |
| All VERY_UNLIKELY/UNLIKELY | Has violations | ❌ UNSAFE | AI_REJECTED |
| Has POSSIBLE/LIKELY/VERY_LIKELY | Clean | ❌ UNSAFE | AI_REJECTED |
| Has POSSIBLE/LIKELY/VERY_LIKELY | Has violations | ❌ UNSAFE | AI_REJECTED |

## 📝 Example Scenarios

### Scenario 1: Clean Blog
```
Images: All VERY_UNLIKELY/UNLIKELY
Text: No violations
→ isSafe = true
→ safetyScore = 95-100
→ Blog status = PUBLISHED
```

### Scenario 2: Suspicious Image
```
Cover Image: Adult = POSSIBLE
Text: Clean
→ isSafe = false
→ safetyScore = 70-80 (deduct 20-30)
→ violations = ["Potentially inappropriate image content"]
→ Blog status = AI_REJECTED
→ Action: Staff manual review required
```

### Scenario 3: Violent Content
```
Section 1 Image: Violence = LIKELY
Text: Contains violent language
→ isSafe = false
→ safetyScore = 40-60 (deduct 40-50 for image + text)
→ violations = ["Violent imagery", "Violent language"]
→ Blog status = AI_REJECTED
→ Action: Staff manual review required
```

### Scenario 4: Adult Content
```
Cover Image: Adult = VERY_LIKELY, Racy = LIKELY
Text: Contains adult references
→ isSafe = false
→ safetyScore = 0-20 (heavy deductions)
→ violations = ["Adult content in images", "Adult content in text"]
→ Blog status = AI_REJECTED
→ Action: Likely permanent rejection
```

## 🔧 Configuration

### Vision API Settings
```java
// ContentModerationService.java
private boolean isLikely(Likelihood likelihood) {
    return likelihood == Likelihood.POSSIBLE || 
           likelihood == Likelihood.LIKELY || 
           likelihood == Likelihood.VERY_LIKELY;
}
```

### Gemini AI Prompt
- Structured with clear sections
- Explicit policy definitions
- Step-by-step analysis approach
- Strict enforcement rules
- Detailed scoring guidelines

## 📈 Monitoring

### Metrics to Track
1. Total blogs moderated
2. Auto-approved rate (isSafe = true)
3. Auto-rejected rate (isSafe = false)
4. Staff review queue size
5. False positive rate (after staff review)
6. False negative rate (user reports)

### Logging
```java
log.info("Image safety check: {} - isSafe={}", imageUrl, isSafe);
log.info("Blog {} moderation: isSafe={}, score={}", blogId, isSafe, score);
```

## 🚨 Edge Cases

### Case 1: Vision API Failure
```
If Vision API fails for an image:
→ Return "FAILED to analyze"
→ Gemini AI treats as potential risk
→ Blog may be flagged for staff review
```

### Case 2: Gemini AI Parsing Error
```
If Gemini response cannot be parsed:
→ Default: isSafe = false, score = 50
→ reason = "Unable to parse AI response. Manual review required."
→ Blog status = AI_REJECTED
```

### Case 3: No Images
```
If blog has no images:
→ Skip Vision API calls
→ Only text moderation by Gemini
→ Decision based purely on text content
```

## ✅ Best Practices

1. **Conservative Approach**: When in doubt, flag for review
2. **Transparency**: Log all decisions with reasons
3. **Audit Trail**: Save moderation history
4. **Staff Override**: Allow manual approval/rejection
5. **Continuous Improvement**: Monitor false positives/negatives
6. **User Communication**: Notify users of rejections with clear reasons

## 🔄 Future Enhancements

1. Machine learning feedback loop from staff reviews
2. User appeal system
3. Category-specific thresholds
4. Context-aware moderation (e.g., medical blogs)
5. Multi-language support
6. Real-time moderation (not just scheduled)
