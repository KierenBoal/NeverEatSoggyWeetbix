@echo off
setlocal

set "stage=build"
call "%~dp0gradlew.bat" build
set "result=%errorlevel%"
if not "%result%"=="0" goto :failed

set "stage=test"
call "%~dp0gradlew.bat" test
set "result=%errorlevel%"
if not "%result%"=="0" goto :failed

set "stage=run"
call "%~dp0gradlew.bat" run
set "result=%errorlevel%"
if not "%result%"=="0" goto :failed
exit /b 0

:failed
echo.
echo Gradle %stage% failed with exit code %result%.
pause
exit /b %result%
