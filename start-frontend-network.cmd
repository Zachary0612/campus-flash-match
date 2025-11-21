@echo off
echo ========================================
echo   校园闪配 - 启动前端服务 (网络访问)
echo ========================================
echo.
echo 正在启动 Vue 3 前端...
echo.

cd /d "%~dp0\frontend"
call npm run dev

pause