@echo off
echo ========================================
echo Starting Interview Prep Frontend
echo ========================================
echo.

cd frontend

echo Installing dependencies (if needed)...
call npm install

echo.
echo Starting Vite development server...
call npm run dev

pause
