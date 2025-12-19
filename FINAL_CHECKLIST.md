# ✅ FINAL CHECKLIST - TRƯỚC KHI DEPLOY

## 📋 PRE-DEPLOYMENT

### 1. Code Review ✅
- [x] Tất cả code viết bằng tiếng Anh
- [x] Không có hardcoded values
- [x] Exception handling đầy đủ
- [x] Logging đầy đủ
- [x] Comments rõ ràng

### 2. Build & Compile ✅
- [x] `mvn clean compile` - SUCCESS
- [x] `mvn clean package` - SUCCESS
- [x] JAR files created
- [x] No compilation errors
- [x] Only warnings (MapStruct - safe to ignore)

### 3. Dockerfile Check ✅
- [x] eureka-server - JDK 17 ✅
- [x] api-gateway - JDK 17 ✅
- [x] identity-service - JDK 17 ✅
- [x] learning-service - JDK 17 ✅
- [x] order-service - JDK 17 ✅
- [x] project-service - JDK 17 ✅

### 4. Dependencies ✅
- [x] Lombok version added to pom.xml
- [x] lombok-mapstruct-binding added
- [x] MapStruct processor configured
- [x] No dependency conflicts

### 5. Documentation ✅
- [x] DEPLOYMENT_GUIDE.md created
- [x] DEPLOYMENT_CHECKLIST.md created
- [x] DEPLOY_README.md created
- [x] SUMMARY.md created
- [x] COMMIT_MESSAGE.txt created

### 6. Scripts ✅
- [x] deploy.sh created (Linux/Mac)
- [x] deploy.ps1 created (Windows)
- [x] Scripts tested

---

## 🚀 DEPLOYMENT STEPS

### Step 1: Git Commit
```bash
# Add all changes
git add .

# Commit with message
git commit -F COMMIT_MESSAGE.txt

# Push to repository
git push origin main
```

### Step 2: Deploy Services

#### Option A: Automated (Recommended)
```bash
# Windows
.\deploy.ps1

# Linux/Mac
chmod +x deploy.sh
./deploy.sh
```

#### Option B: Manual
```bash
# 1. Build identity-service
cd identity-service
./mvnw clean package -DskipTests
cd ..

# 2. Build learning-service
cd learning-service
./mvnw clean package -DskipTests
cd ..

# 3. Build Docker images
docker-compose build identity-service learning-service

# 4. Deploy
docker-compose up -d identity-service learning-service
```

### Step 3: Verify Deployment
```bash
# Check containers
docker-compose ps

# Check logs
docker-compose logs -f identity-service learning-service

# Wait for services to start (30 seconds)
```

### Step 4: Test Endpoints

#### Test 1: Wallet Endpoint
```bash
curl http://localhost:8888/api/wallet/user/1
```
**Expected**: 200 OK with wallet data

#### Test 2: Enrollment with Sufficient Balance
```bash
curl -X POST http://localhost:8888/api/enrollments/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```
**Expected**: 200 OK

#### Test 3: Enrollment with Insufficient Balance
```bash
# Assuming course price > wallet balance
curl -X POST http://localhost:8888/api/enrollments/999 \
  -H "Authorization: Bearer YOUR_TOKEN"
```
**Expected**: 402 Payment Required

---

## 🔍 POST-DEPLOYMENT CHECKS

### 1. Service Health ✅
- [ ] identity-service is UP
- [ ] learning-service is UP
- [ ] No error logs
- [ ] Services registered with Eureka

### 2. Database ✅
- [ ] No new migrations needed
- [ ] Existing data intact
- [ ] Wallet table accessible

### 3. API Testing ✅
- [ ] Wallet endpoint works
- [ ] Enrollment with sufficient balance works
- [ ] Enrollment with insufficient balance returns 402
- [ ] Error messages are correct

### 4. Monitoring ✅
- [ ] Check error rates
- [ ] Check response times
- [ ] Check memory usage
- [ ] Check CPU usage

---

## ⚠️ ROLLBACK PLAN

### If deployment fails:

```bash
# 1. Stop new containers
docker-compose stop identity-service learning-service

# 2. Checkout previous version
git log --oneline -5
git checkout <previous-commit-hash>

# 3. Rebuild and restart
docker-compose build identity-service learning-service
docker-compose up -d identity-service learning-service

# 4. Verify rollback
docker-compose logs -f identity-service learning-service
```

---

## 📊 MONITORING CHECKLIST

### First 30 Minutes
- [ ] Monitor error logs
- [ ] Check enrollment success rate
- [ ] Verify wallet endpoint response time
- [ ] Check database connections

### First 24 Hours
- [ ] Monitor error rates
- [ ] Check performance metrics
- [ ] Verify no memory leaks
- [ ] Check user feedback

---

## 🎯 SUCCESS CRITERIA

### Deployment is successful if:
- ✅ All services are running
- ✅ No error logs
- ✅ Wallet endpoint returns 200
- ✅ Enrollment with sufficient balance works
- ✅ Enrollment with insufficient balance returns 402
- ✅ No performance degradation
- ✅ No database issues

---

## 📞 EMERGENCY CONTACTS

### If issues occur:
1. Check logs: `docker-compose logs -f identity-service learning-service`
2. Check service status: `docker-compose ps`
3. Check Eureka: `http://localhost:8761`
4. Rollback if necessary (see above)

---

## ✅ FINAL SIGN-OFF

**Developer**: ________________  
**Date**: 2025-12-19  
**Time**: _______  

**Pre-deployment checks completed**: [ ]  
**Deployment successful**: [ ]  
**Post-deployment tests passed**: [ ]  
**Monitoring in place**: [ ]  

**READY TO DEPLOY**: ✅ YES

---

**MỌI THỨ ĐÃ SẴN SÀNG! DEPLOY NGAY! 🚀**
