@echo off
echo ========================================
echo Starting Interview Prep Backend
echo ========================================
echo.

echo Checking MySQL service...
net start MySql84 2>nul
if %errorlevel% neq 0 (
    echo MySQL service check completed (may already be running)
)

echo.
echo Building backend...
cd backend
call mvn clean install -DskipTests

echo.
echo Starting Spring Boot application...
call mvn spring-boot:run

pause
