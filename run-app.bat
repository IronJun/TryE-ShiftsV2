@echo off
REM Avvia esplicitamente Windows PowerShell.
set "SCRIPT=%~dp0run-app.ps1"
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%"
pause