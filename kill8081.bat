@echo off
setlocal

set PORTS=8081

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| find ":%%P" ^| find "LISTENING"') do (
        echo Killing PID %%a on port %%P
        taskkill /F /PID %%a
    )
)

endlocal