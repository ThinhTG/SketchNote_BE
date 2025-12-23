# Testing Summary - Password Storage & Google OAuth

## Test Cases

### 1. Register with Email/Password ✅
**Endpoint**: `POST /api/auth/register`

**Request**:
```json
{
  "email": "test@example.com",
  "password": "MyPassword123",
  "firstName": "Test",
  "lastName": "User"
}
```

**Expected Behavior**:
- ✅ User created in Keycloak with plain password
- ✅ User created in DB with encrypted password
- ✅ Verification email sent
- ✅ Password in DB is encrypted (not readable)

**Database Check**:
```sql
SELECT email, password, verified FROM users WHERE email = 'test@example.com';
-- password should be encrypted string, not plain text
```

---

### 2. Login with Email/Password ✅
**Endpoint**: `POST /api/auth/login`

**Request**:
```json
{
  "email": "test@example.com",
  "password": "MyPassword123"
}
```

**Expected Behavior**:
- ✅ Login successful via Keycloak
- ✅ Returns access_token and refresh_token
- ✅ Password verification works correctly

---

### 3. Google OAuth Login - New User ✅
**Endpoint**: `POST /api/auth/login-google-mobile`

**Request**:
```json
{
  "idToken": "valid_google_id_token"
}
```

**Expected Behavior**:
- ✅ Verify Google ID token
- ✅ Extract email from token
- ✅ User NOT found in DB
- ✅ Create new user in Keycloak with random password
- ✅ Create new user in DB with encrypted random password
- ✅ Create wallet for user
- ✅ Login to Keycloak with random password
- ✅ Returns access_token and refresh_token

**Database Check**:
```sql
SELECT email, password, verified, keycloak_id FROM users WHERE email = 'google_user@gmail.com';
-- password should be encrypted random string
-- verified should be true (Google verified)
```

---

### 4. Google OAuth Login - Existing User (Registered via Email) ✅
**Scenario**: User đã đăng ký bằng email/password, sau đó login bằng Google

**Step 1**: Register with email/password
```json
POST /api/auth/register
{
  "email": "existing@example.com",
  "password": "MyPassword123",
  "firstName": "Existing",
  "lastName": "User"
}
```

**Step 2**: Login with Google (same email)
```json
POST /api/auth/login-google-mobile
{
  "idToken": "google_token_with_existing@example.com"
}
```

**Expected Behavior**:
- ✅ Verify Google ID token
- ✅ Extract email from token
- ✅ User FOUND in DB
- ✅ Decrypt stored password from DB
- ✅ Login to Keycloak with decrypted password
- ✅ Returns access_token and refresh_token
- ✅ NO new user created

---

### 5. Google OAuth Login - Existing Google User ✅
**Scenario**: User đã đăng nhập bằng Google trước đó, login lại bằng Google

**Step 1**: First Google login (creates user)
```json
POST /api/auth/login-google-mobile
{
  "idToken": "google_token_1"
}
```

**Step 2**: Second Google login (same user)
```json
POST /api/auth/login-google-mobile
{
  "idToken": "google_token_2"
}
```

**Expected Behavior**:
- ✅ Verify Google ID token
- ✅ User FOUND in DB (from first login)
- ✅ Decrypt stored random password
- ✅ Login to Keycloak with decrypted password
- ✅ Returns access_token and refresh_token
- ✅ NO new user created

---

## Security Tests

### 6. Password Encryption ✅
**Test**: Verify password is encrypted in database

```sql
-- Check that password is NOT plain text
SELECT email, password FROM users WHERE email = 'test@example.com';
-- password should NOT equal 'MyPassword123'
-- password should be base64 encoded string
```

### 7. Password Decryption ✅
**Test**: Verify password can be decrypted correctly

```java
String encrypted = passwordEncryptionUtil.encrypt("MyPassword123");
String decrypted = passwordEncryptionUtil.decrypt(encrypted);
assertEquals("MyPassword123", decrypted);
```

### 8. Password Not Exposed in API ✅
**Test**: Verify password is not returned in API responses

```json
GET /api/users/me
{
  "id": 1,
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User"
  // password should NOT be here
}
```

---

## Error Cases

### 9. Invalid Google Token ❌
```json
POST /api/auth/login-google-mobile
{
  "idToken": "invalid_token"
}
```
**Expected**: `ErrorCode.INVALID_TOKEN`

### 10. Inactive User ❌
**Setup**: Set user.isActive = false in DB

```json
POST /api/auth/login-google-mobile
{
  "idToken": "valid_token_for_inactive_user"
}
```
**Expected**: `ErrorCode.USER_INACTIVE`

### 11. Email Already Exists ❌
```json
POST /api/auth/register
{
  "email": "existing@example.com",
  "password": "password"
}
```
**Expected**: `ErrorCode.EMAIL_EXISTED`

---

## Performance Tests

### 12. Encryption Performance
- Encrypt 1000 passwords
- Measure average time
- Should be < 10ms per encryption

### 13. Decryption Performance
- Decrypt 1000 passwords
- Measure average time
- Should be < 10ms per decryption

---

## Integration Tests

### 14. Full Registration → Login Flow
1. Register with email/password
2. Verify email
3. Login with email/password
4. ✅ Should succeed

### 15. Full Google OAuth Flow
1. Login with Google (new user)
2. Logout
3. Login with Google again (existing user)
4. ✅ Should succeed both times

### 16. Mixed Flow
1. Register with email/password
2. Login with Google (same email)
3. Login with email/password again
4. ✅ All should succeed

---

## Manual Testing Checklist

- [ ] Register new user with email/password
- [ ] Verify password is encrypted in DB
- [ ] Login with email/password
- [ ] Login with Google (new user)
- [ ] Login with Google (existing email/password user)
- [ ] Login with Google (existing Google user)
- [ ] Check password not exposed in API responses
- [ ] Test with invalid Google token
- [ ] Test with inactive user
- [ ] Test encryption/decryption utility

---

## Database Verification Queries

```sql
-- Check all users with passwords
SELECT id, email, 
       CASE 
         WHEN password IS NULL THEN 'No Password'
         ELSE 'Has Password'
       END as password_status,
       verified, is_active
FROM users;

-- Check password encryption
SELECT email, 
       LENGTH(password) as password_length,
       SUBSTRING(password, 1, 20) as password_preview
FROM users 
WHERE password IS NOT NULL;

-- Find users registered via Google
SELECT email, verified, create_at
FROM users
WHERE verified = true 
  AND password IS NOT NULL
ORDER BY create_at DESC;
```

---

## Troubleshooting Commands

```bash
# Check application logs
tail -f logs/identity-service.log | grep -i "password\|google\|encrypt"

# Test encryption key
curl -X POST http://localhost:8089/api/test/encrypt \
  -H "Content-Type: application/json" \
  -d '{"password": "test123"}'

# Verify Keycloak connection
curl http://34.177.90.19:8090/realms/sketchnote/.well-known/openid-configuration
```
