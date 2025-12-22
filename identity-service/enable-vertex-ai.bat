@echo off
echo ============================================
echo Enable Vertex AI API for Project
echo ============================================
echo.
echo Project ID: inductive-vista-479508-s4
echo.
echo Opening Google Cloud Console to enable Vertex AI API...
echo.
start https://console.cloud.google.com/apis/library/aiplatform.googleapis.com?project=inductive-vista-479508-s4
echo.
echo INSTRUCTIONS:
echo 1. Click "ENABLE" button on the page that just opened
echo 2. Wait for API to be enabled (may take 1-2 minutes)
echo 3. Come back and press any key to continue
echo.
pause
echo.
echo ============================================
echo Checking IAM Permissions...
echo ============================================
echo.
echo Opening IAM page to verify service account permissions...
echo.
start https://console.cloud.google.com/iam-admin/iam?project=inductive-vista-479508-s4
echo.
echo INSTRUCTIONS:
echo 1. Find your service account (email ending with @xxx.iam.gserviceaccount.com)
echo 2. Click the EDIT button (pencil icon)
echo 3. Add role: "Vertex AI User" or "Vertex AI Administrator"
echo 4. Click SAVE
echo.
echo After completing these steps, restart your application.
echo.
pause
