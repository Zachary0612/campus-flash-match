@echo off
rem 检查必要的服务是否运行
netstat -an | findstr "5432" > nul
if %errorlevel% neq 0 (
    echo 警告: PostgreSQL服务未启动（端口5432）
    pause
)

netstat -an | findstr "6379" > nul
if %errorlevel% neq 0 (
    echo 警告: Redis服务未启动（端口6379）
    pause
)

netstat -an | findstr "5672" > nul
if %errorlevel% neq 0 (
    echo 警告: RabbitMQ服务未启动（端口5672）
    pause
)

rem 由于直接运行类文件依赖复杂，建议使用IDE或正确配置的Maven/Gradle运行
rem 这里提供一个提示信息，指导用户如何正确运行项目
echo 请使用以下方式之一运行项目：
echo 1. 在IntelliJ IDEA中运行CampusFlashMatchApplication类
echo 2. 配置正确的JAVA_HOME后运行：mvnw.cmd spring-boot:run
echo 3. 或：gradlew.bat bootRun

pause