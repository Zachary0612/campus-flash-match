@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo   校园闪配 - 构建并运行后端服务
echo ========================================
echo.
echo JAVA_HOME = %JAVA_HOME%
echo.
echo 正在检查Java版本...
java -version
echo.
echo 正在清理旧构建...
call mvnw.cmd clean
echo.
echo 正在编译和打包...
call mvnw.cmd package -DskipTests
echo.
echo 正在启动 Spring Boot 后端...
echo.

cd /d "%~dp0"

java -jar target/campus-flash-match-0.0.1-SNAPSHOT.jar

pause