@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo   校园闪配 - 启动后端服务 (网络访问)
echo ========================================
echo.
echo 正在启动 Spring Boot 后端...
echo.

cd /d "%~dp0"
call mvnw.cmd spring-boot:run

pause