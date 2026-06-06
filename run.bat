@echo off
echo ==============================================================
echo 🛒 STARTING BARGAIN HERE PRICE COMPARISON SUITE
echo ==============================================================
echo.

:: Check if Python is installed
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH!
    echo Please install Python and try again.
    pause
    exit /b
)

:: Install dependencies silently
echo Installing required python packages from requirements.txt...
pip install -r requirements.txt --quiet
if %errorlevel% neq 0 (
    echo [WARNING] Some dependencies failed to install. Retrying in normal mode...
    pip install flask flask-cors requests beautifulsoup4 lxml
)

echo.
echo Initializing/Seeding SQLite Product Database...
python seed_db.py
if %errorlevel% neq 0 (
    echo [WARNING] SQLite Seeding failed! Proceeding anyway.
)

echo.
echo Starting Flask Backend server...
start "Bargain Here Backend Server" /B python choukasi_backend.py

echo.
echo Waiting for backend server to spin up...
timeout /t 3 /nobreak >nul

echo.
echo Launching Frontend Comparison Suite...
start "" "http://localhost:5000"

echo.
echo ==============================================================
echo ✅ Bargain Here is fully active!
echo    - Backend API: http://localhost:5000
echo    - Endpoints are ready. Keep this terminal open to log requests.
echo ==============================================================
echo.
