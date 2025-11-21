@echo off
echo ========================================
echo   校园闪配 - 启动后端服务
echo ========================================
echo.
echo 正在启动 Spring Boot 后端...
echo.

cd /d "%~dp0"
call mvnw.cmd spring-boot:run

pause
