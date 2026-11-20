@echo off
setlocal
set "ROOT=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%setup.ps1"
if errorlevel 1 (
    echo Setup failed.
    exit /b 1
)
exit /b 0
